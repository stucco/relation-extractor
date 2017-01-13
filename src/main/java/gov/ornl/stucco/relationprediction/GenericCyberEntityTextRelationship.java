package gov.ornl.stucco.relationprediction;
//This class is stores generic version of a relationship between two cyber entities.  It can be created between any
//pair of cyber entities, even those that do not fit into any relationship we care about.  The reason for this is that
//we use it as a tool for determining if the entities' types fit some relationship we care about.  It can be initialized
//with two CyberEntityTexts, and from that it will determine which, if any, relationship type could apply between the
//two entity types.  The constants defined at the top define all relationship types (though if the entities appear
//in the text in the reverse order of the order implied by the order in the constant's name, the relationship's ID is just
//-1 times the normal relationship type ID.
//
//The most important methods here are loadAllKnownRelationships(), which reads stuff in from a few files, mostly
//from the NVD dump, and determines what entitities are actually related (just storing this information in memory).
//After calling that method, it is possible for an instance of this class to call isKnownRelationship() to determine
//whether the CyberEntityTexts it was initialized with should be related or not according to the NVD dump and other 
//sources.



import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator;
import gov.ornl.stucco.entity.models.CyberEntityType;

public class GenericCyberEntityTextRelationship 
{
	//This is the directory containing the nvd xml dumps.
	//private static File nvdxmldir = new File("src/nvdxml/");	//Location where xml dumps from NVD can be found.  Downloaded from https://nvd.nist.gov/download.cfm
	
	
	//Each of the 24 relationshp types is associated with an int constant here.
	public static final int RT_SWVENDOR_SWPRODUCT = 1;	//sw.vendor & sw.product --> same software
	public static final int RT_SWVERSION_SWPRODUCT = 2;	//sw.version & sw.product --> same software
	public static final int RT_VUDESCRIPTION_VUNAME = 3;	//vuln.description & vuln.name --> same vulnerability
	public static final int RT_VUMS_VUNAME = 4;	//vuln.ms & vuln.name --> same vulnerability
	public static final int RT_VUCVE_VUNAME = 5;	//vuln.cve & vuln.name --> same vulnerability
	public static final int RT_VUDESCRIPTION_VUMS = 6;	//vuln.description & vuln.ms --> same vulnerability
	public static final int RT_VUDESCRIPTION_VUCVE = 7;	//vuln.description & vuln.cve --> same vulnerability
	public static final int RT_VUCVE_VUMS = 8;	//vuln.cve & vuln.ms --> same vulnerability
	
	//The commented out relationship types are relationships we are interested in, but cannot be found directly in the text.
	//public static final int RT_VU_SW = 9;	//vuln.* & sw.* --> ExploitTargetRelatedObservable
	//public static final int RT_SW_FILENAME = 10;	//sw.* & file.name --> Sub-Observable
	//public static final int RT_SW_FUNCTIONNAME= 11;	//sw.* & function.name --> Sub-Observable
	//public static final int RT_VU_FILENAME = 12;	//vuln.* & file.name --> ExploitTargetRelatedObservable
	//public static final int RT_VU_FUNCTIONNAME = 13;	//vuln.* & function.name --> ExploitTargetRelatedObservable
	
	//The following relationship types are not directly ones we are interested in, but are necessary for finding the ones above that cannot be found directly in the text.
	//The next group are needed for vuln.* & sw.* --> ExploitTargetRelatedObservable
	public static final int RT_SWPRODUCT_VUNAME = 14;	//sw.product & vuln.name
	public static final int RT_SWPRODUCT_VUMS = 15;	//sw.product & vuln.ms
	public static final int RT_SWPRODUCT_VUCVE = 16;	//sw.product & vuln.cve
	public static final int RT_SWVERSION_VUNAME = 17;	//sw.version & vuln.name
	public static final int RT_SWVERSION_VUMS = 18;	//sw.version & vuln.ms
	public static final int RT_SWVERSION_VUCVE = 19;	//sw.version & vuln.cve
	
	//The next group are needed for sw.* & file.name --> Sub-Observable
	public static final int RT_SWPRODUCT_FINAME = 20;	//sw.product & file.name
	public static final int RT_SWVERSION_FINAME = 21;	//sw.version & file.name
	
	//The next group are needed for sw.* & function.name --> Sub-Observable
	public static final int RT_SWPRODUCT_FUNAME = 22;	//sw.product & function.name
	public static final int RT_SWVERSION_FUNAME = 23;	//sw.version & function.name
	
	//The next group are needed for vuln.* & file.name --> ExploitTargetRelatedObservable
	public static final int RT_VUNAME_FINAME = 24;	//vuln.name & file.name
	public static final int RT_VUCVE_FINAME = 25;	//vuln.cve & file.name
	public static final int RT_VUMS_FINAME = 26;	//vuln.ms & file.name
	
	//The next group are needed for vuln.* & function.name --> ExploitTargetRelatedObservable
	public static final int RT_VUNAME_FUNAME = 27;	//vuln.name & function.name
	public static final int RT_VUCVE_FUNAME = 28;	//vuln.cve & function.name
	public static final int RT_VUMS_FUNAME = 29;	//vuln.ms & function.name

	public static final String SAME_VERTEX = "PropertiesOfSameVertex";
	
	public static HashMap<Integer,String> relationshipidTorelationshipname = new HashMap<Integer,String>();
	static
	{
		relationshipidTorelationshipname.put(RT_SWVENDOR_SWPRODUCT, SAME_VERTEX);
		relationshipidTorelationshipname.put(RT_SWVERSION_SWPRODUCT, SAME_VERTEX);
		relationshipidTorelationshipname.put(RT_VUDESCRIPTION_VUNAME, SAME_VERTEX);
		relationshipidTorelationshipname.put(RT_VUMS_VUNAME, SAME_VERTEX);
		relationshipidTorelationshipname.put(RT_VUCVE_VUNAME, SAME_VERTEX);
		relationshipidTorelationshipname.put(RT_VUDESCRIPTION_VUMS, SAME_VERTEX);
		relationshipidTorelationshipname.put(RT_VUDESCRIPTION_VUCVE, SAME_VERTEX);
		relationshipidTorelationshipname.put(RT_VUCVE_VUMS, SAME_VERTEX);
		relationshipidTorelationshipname.put(RT_SWPRODUCT_VUNAME, "ExploitTargetRelatedObservable");
		relationshipidTorelationshipname.put(RT_SWPRODUCT_VUMS, "ExploitTargetRelatedObservable");
		relationshipidTorelationshipname.put(RT_SWPRODUCT_VUCVE, "ExploitTargetRelatedObservable");
		relationshipidTorelationshipname.put(RT_SWVERSION_VUNAME, "ExploitTargetRelatedObservable");
		relationshipidTorelationshipname.put(RT_SWVERSION_VUMS, "ExploitTargetRelatedObservable");
		relationshipidTorelationshipname.put(RT_SWVERSION_VUCVE, "ExploitTargetRelatedObservable");
		relationshipidTorelationshipname.put(RT_SWPRODUCT_FINAME, "Sub-Observable");
		relationshipidTorelationshipname.put(RT_SWVERSION_FINAME, "Sub-Observable");
		relationshipidTorelationshipname.put(RT_SWPRODUCT_FUNAME, "Sub-Observable");
		relationshipidTorelationshipname.put(RT_SWVERSION_FUNAME, "Sub-Observable");
		relationshipidTorelationshipname.put(RT_VUNAME_FINAME, "ExploitTargetRelatedObservable");
		relationshipidTorelationshipname.put(RT_VUCVE_FINAME, "ExploitTargetRelatedObservable");
		relationshipidTorelationshipname.put(RT_VUMS_FINAME, "ExploitTargetRelatedObservable");
		relationshipidTorelationshipname.put(RT_VUNAME_FUNAME, "ExploitTargetRelatedObservable");
		relationshipidTorelationshipname.put(RT_VUCVE_FUNAME, "ExploitTargetRelatedObservable");
		relationshipidTorelationshipname.put(RT_VUMS_FUNAME, "ExploitTargetRelatedObservable");
	}
	
	public static final Map<CyberEntityType, Map<CyberEntityType,Integer>> entitiesToRelationMap = new HashMap<CyberEntityType, Map<CyberEntityType,Integer>>();
	static {
		Map<CyberEntityType,Integer> entityToRelationMap = new HashMap<CyberEntityType,Integer>();
		entityToRelationMap.put(CyberHeuristicAnnotator.SW_PRODUCT, RT_SWVENDOR_SWPRODUCT);
		entitiesToRelationMap.put(CyberHeuristicAnnotator.SW_VENDOR, entityToRelationMap);
		
		entityToRelationMap = new HashMap<CyberEntityType,Integer>();
		entityToRelationMap.put(CyberHeuristicAnnotator.SW_PRODUCT, RT_SWVERSION_SWPRODUCT);
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_NAME, RT_SWVERSION_VUNAME);
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_MS, RT_SWVERSION_VUMS);
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_CVE, RT_SWVERSION_VUCVE);
		entityToRelationMap.put(CyberHeuristicAnnotator.FILE_NAME, RT_SWVERSION_FINAME);
		entityToRelationMap.put(CyberHeuristicAnnotator.FUNCTION_NAME, RT_SWVERSION_FUNAME);
		entitiesToRelationMap.put(CyberHeuristicAnnotator.SW_VERSION, entityToRelationMap);
		
		entityToRelationMap = new HashMap<CyberEntityType,Integer>();
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_NAME, RT_SWPRODUCT_VUNAME);
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_MS, RT_SWPRODUCT_VUMS);
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_CVE, RT_SWPRODUCT_VUCVE);
		entityToRelationMap.put(CyberHeuristicAnnotator.FILE_NAME, RT_SWPRODUCT_FINAME);
		entityToRelationMap.put(CyberHeuristicAnnotator.FUNCTION_NAME, RT_SWPRODUCT_FUNAME);
		entitiesToRelationMap.put(CyberHeuristicAnnotator.SW_PRODUCT, entityToRelationMap);
		
		entityToRelationMap = new HashMap<CyberEntityType,Integer>();
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_NAME, RT_VUDESCRIPTION_VUNAME);
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_MS, RT_VUDESCRIPTION_VUMS);
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_CVE, RT_VUDESCRIPTION_VUCVE);
		entitiesToRelationMap.put(CyberHeuristicAnnotator.VULN_DESC, entityToRelationMap);
		
		entityToRelationMap = new HashMap<CyberEntityType,Integer>();
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_NAME, RT_VUMS_VUNAME);
		entityToRelationMap.put(CyberHeuristicAnnotator.FILE_NAME, RT_VUMS_FINAME);
		entityToRelationMap.put(CyberHeuristicAnnotator.FUNCTION_NAME, RT_VUMS_FUNAME);
		entitiesToRelationMap.put(CyberHeuristicAnnotator.VULN_MS, entityToRelationMap);
		
		entityToRelationMap = new HashMap<CyberEntityType,Integer>();
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_NAME, RT_VUCVE_VUNAME);
		entityToRelationMap.put(CyberHeuristicAnnotator.VULN_MS, RT_VUCVE_VUMS);
		entityToRelationMap.put(CyberHeuristicAnnotator.FILE_NAME, RT_VUCVE_FINAME);
		entityToRelationMap.put(CyberHeuristicAnnotator.FUNCTION_NAME, RT_VUCVE_FUNAME);
		entitiesToRelationMap.put(CyberHeuristicAnnotator.VULN_CVE, entityToRelationMap);
		
		entityToRelationMap = new HashMap<CyberEntityType,Integer>();
		entityToRelationMap.put(CyberHeuristicAnnotator.FILE_NAME, RT_VUNAME_FINAME);
		entityToRelationMap.put(CyberHeuristicAnnotator.FUNCTION_NAME, RT_VUNAME_FUNAME);
		entitiesToRelationMap.put(CyberHeuristicAnnotator.VULN_NAME, entityToRelationMap);
	}
	
	
	//Each entity type is associated with an int constant in CyberEntity, and each relationship type
	//is associated with an int constant above.  Given the ints associated with a pair of entities, this
	//array stores the int associated with their relationship type.  Entries should be null if there
	//is no relationship type that fits with the two entities.  Order matters, and an entry
	//in this array is positive if the entities appear in the standard order, and negative
	//if the order is reversed.  The "standard" order is arbitrary, and is defined only in this array.
	public static final HashSet<Integer> allrelationshiptypesset = new HashSet<Integer>();
	public static final HashSet<Integer> allpositiverelationshiptypesset = new HashSet<Integer>();
	static
	{
		for (CyberEntityType entityType : entitiesToRelationMap.keySet()) {
			Map<CyberEntityType,Integer> relationMap = entitiesToRelationMap.get(entityType);
			for (Integer relID : relationMap.values()) {
				allrelationshiptypesset.add(relID);
				allpositiverelationshiptypesset.add(relID);
				allrelationshiptypesset.add(-relID);
			}
		}
	}
	
	
	//This class contains methods for determining if a relationship candidate is a known relationship or not.
	//In order to determine this, it needs to load a bunch of relationships into memory.  But it doesn't make
	//sense to load the relationships into memory unless they're needed.  So this variable keeps track
	//of whether the relationships have been loaded into memory.
	private static boolean loadedrelationships = false;
	
	
	private CyberEntity entity1;
	private CyberEntity entity2;

	public GenericCyberEntityTextRelationship(CyberEntity entity1, CyberEntity entity2)
	{
		this.entity1 = entity1;
		this.entity2 = entity2;
	}

	public Integer getRelationType()
	{
		boolean foundEntry = false;
		Integer relationID = null;
		if (entitiesToRelationMap.containsKey(entity1.getCyberType())) {
			Map<CyberEntityType,Integer> relationIDMap = entitiesToRelationMap.get(entity1.getCyberType());
			if (relationIDMap.containsKey(entity2.getCyberType())) {
				relationID = relationIDMap.get(entity2.getCyberType());
				foundEntry = true;
			}
		}
		
		if ((!foundEntry) && (entitiesToRelationMap.containsKey(entity2.getCyberType()))) {
			Map<CyberEntityType,Integer> relationIDMap = entitiesToRelationMap.get(entity2.getCyberType());
			if (relationIDMap.containsKey(entity1.getCyberType())) {
				relationID = -relationIDMap.get(entity1.getCyberType());
				foundEntry = true;
			}
		}
		return relationID;
	}
	
	public String toString()
	{
		return entity1 + "\t" + entity2 + "\t" + getRelationType();
	}

	//Reads all known entities in from Database.  The information gets stored in various different places.
	public static void loadAllKnownRelationships()
	{
		Vulnerability.setAllRelevantTerms();
		
		loadRelationshipsFromNVD();
		
		SoftwareWVersion.setAllAliases();
		
		loadedrelationships = true;
	}
	
	//Loads entities from NVD xml data.  The code is ugly because I have never done anything with xml files before.
	private static void loadRelationshipsFromNVD()
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
		
			for(File file : ProducedFileGetter.getNVDXMLDir().listFiles())
			{
				if(!file.getName().endsWith(".zip"))
					continue;
				
				ZipFile zipfile = new ZipFile(file);
			    Enumeration<? extends ZipEntry> entries = zipfile.entries();
			    while(entries.hasMoreElements())
			    {
			        ZipEntry entry = entries.nextElement();
			        InputStream stream = zipfile.getInputStream(entry);
				
			        org.w3c.dom.Document document = db.parse(stream);
			        org.w3c.dom.Element root = document.getDocumentElement();
			        org.w3c.dom.NodeList nodelist = root.getChildNodes();
			        for(int i = 0; i < nodelist.getLength(); i++)  //Each node should be associated with a vulnerability.
			        {
			        	org.w3c.dom.Node node = nodelist.item(i);
					
			        	//Skip any nodes that are not vulnerability entries, because some nodes are not vulnerability information.
			        	if(!node.getNodeName().equals("entry"))
			        		continue;
					
					
			        	//These are holder variables for all the stuff that is going to end up
			        	//being related to each other.  We set the values of these things as
			        	//we progress through the subnodes.
			        	String cveid = null;
						String msid = null;
						ArrayList<SoftwareWVersion> vulnerablesoftwares = new ArrayList<SoftwareWVersion>();
						ArrayList<String> description = null;
						String name = null;
						HashSet<String> functionnames = new HashSet<String>();
						HashSet<String> filenames = new HashSet<String>();
					
					
						org.w3c.dom.NodeList nodelist2 = node.getChildNodes();
						for(int j = 0; j < nodelist2.getLength(); j++)
						{
							org.w3c.dom.Node node2 = nodelist2.item(j);
						
							
							//Look into each of these if/else if statements to see where we find entities and how we set them from what we find there.
							if(node2.getNodeName().equals("vuln:cve-id"))
								cveid = node2.getTextContent();
							else if(node2.getNodeName().equals("vuln:references"))
							{
								org.w3c.dom.NodeList nodelist3 = node2.getChildNodes();
								for(int k = 0; k < nodelist3.getLength(); k++)
								{
									org.w3c.dom.Node node3 = nodelist3.item(k);
									if(node3.getNodeName().equals("vuln:reference"))
									{
										String possiblemsid = node3.getTextContent();
										if(possiblemsid.startsWith("MS") && possiblemsid.length() == 8 && possiblemsid.charAt(4) == '-')
											msid = possiblemsid;
									}
								}
							}
							else if(node2.getNodeName().equals("vuln:vulnerable-software-list"))
							{
								org.w3c.dom.NodeList nodelist3 = node2.getChildNodes();
								for(int k = 0; k < nodelist3.getLength(); k++)
								{
									org.w3c.dom.Node node3 = nodelist3.item(k);
									if(node3.getNodeName().equals("vuln:product"))
									{
										String vulnerablesoftwareid = node3.getTextContent();
					
										//All the stuff we want to know about a software is encoded in the software id, so we can build the Software object when we extract the id.
										SoftwareWVersion vulnerablesoftware = SoftwareWVersion.getSoftwareFromSoftwareID(vulnerablesoftwareid);
								
										vulnerablesoftwares.add(vulnerablesoftware);
									}
								}
							}
							else if(node2.getNodeName().equals("vuln:summary"))
							{
								String summary = node2.getTextContent().toLowerCase();
							
							
								//A relevant term counts as a description if it appears in the list of relevant terms at https://github.com/stucco/entity-extractor/blob/master/src/main/resources/dictionaries/relevant_terms.txt
								description = Vulnerability.getRelevantDescriptionsFromText(summary);
							
							
								//We say the vulnerability's name starts after ", aka " and ends at whichever occurs first: 1) the next period, 2) the next comma, or 3) the end of the summary.
								int startposition = summary.lastIndexOf(", aka ");
								if(startposition > -1)
								{
									startposition = startposition + 6;
								
									int nextperiodposition = summary.indexOf('.', startposition);
									if(nextperiodposition == -1)
										nextperiodposition = Integer.MAX_VALUE;
									int nextcommaposition = summary.indexOf(',', startposition);
									if(nextcommaposition == -1)
										nextcommaposition = Integer.MAX_VALUE;
									int endofstring = summary.length()-1;
								
									int endposition = Math.min(nextperiodposition, Math.min(nextcommaposition, endofstring));
								
									name = summary.substring(startposition, endposition).replaceAll("\"", "");
								}
							
							
								//Summaries contain function and file names sometimes
								String[] splitdescription = summary.split(" ");
								for(int k = 0; k < splitdescription.length; k++)
								{
									if(splitdescription[k].startsWith("function"))
									{
										String possiblefunctionthing = splitdescription[k];
										if(possiblefunctionthing.charAt(possiblefunctionthing.length()-1) == ',' || possiblefunctionthing.charAt(possiblefunctionthing.length()-1) == ';' || possiblefunctionthing.charAt(possiblefunctionthing.length()-1) == ')')
											possiblefunctionthing = possiblefunctionthing.substring(0, possiblefunctionthing.length()-1);
										if((possiblefunctionthing.equals("function") || possiblefunctionthing.equals("functions")) && k > 0)
											functionnames.add(splitdescription[k-1]);
									}
									
									RelevantFile f = RelevantFile.getRelevantFileOfCommonType(splitdescription[k]);
									if(f != null)
										filenames.add(f.getFileName());
								}
							}	
						}
					
					
						//We wait to create the vulnerability object until here because Vulnerability's fields need to be populated from information from different places in the xml entity (so we need to finish the last loop before constructing a vulnerability instance).
						Vulnerability vulnerability = Vulnerability.getVulnerabilityFromCVEID(cveid, msid, name, description);
						
					
						//We want to associate functions and file names with the vulnerability, software, and 
						//relationship, but we do not have a really good way for figuring out when they are 
						//associated with the right software if there are multiple softwares for this vulnerability.
						//So we associate a file name or vulnerability with these things only if 
						//all the vulnerable softwares are versions of the same software.  So, for example, we
						//would not associate any functions or filenames with anything if the vulnerable
						//softwares included Flash and Firefox, but we would create the association if 
						//the vulnerability is associated with Firefox version 1.3 and Firefox version 1.4.
						boolean founddifferentvulnerablesoftwares = false;
						if(vulnerablesoftwares.size() > 0)
						{
							String thesoftwarename = vulnerablesoftwares.get(0).getName();
							for(int j = 1; j < vulnerablesoftwares.size(); j++)
								if(!thesoftwarename.equals(vulnerablesoftwares.get(j).getName()))
									founddifferentvulnerablesoftwares = true;
						}
					
					
						//Relate all softwares mentioned with the vulnerability.
						for(SoftwareWVersion software : vulnerablesoftwares)
						{
							VulnerabilityToSoftwareWVersionRelationship r = VulnerabilityToSoftwareWVersionRelationship.getRelationship(software, vulnerability);
						
							if(!founddifferentvulnerablesoftwares)
							{
								r.setFunctionNames(functionnames);
								r.setFileNames(filenames);
							}
						}
			        }
				}
			    zipfile.close();
			}
		}catch(ParserConfigurationException | IOException | SAXException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	

	
	//Return true if the entities are in a known relationship.  Return false if both entities are known, but they are not in a known relationship.  Return null if either entity is not known.
	public Boolean isKnownRelationship()
	{
		//We can't tell if a relationship is known unless we have loaded our known relationships into memory.  So check
		//if we've done that.  And if not, do it.
		if(!loadedrelationships) {
			loadAllKnownRelationships();
		}
		
		switch (getRelationType()) 
		{
			//Choose a function depending on the types of the entities involved.
        	case RT_SWVENDOR_SWPRODUCT:  return isKnownRelationship_SWVendor_SWProduct();
        	case RT_SWVERSION_SWPRODUCT:  return isKnownRelationship_SWVersion_SWProduct();
        	case RT_VUDESCRIPTION_VUNAME:  return isKnownRelationship_VUDescription_VUName();
        	case RT_VUMS_VUNAME:  return isKnownRelationship_VUMS_VUName();
        	case RT_VUCVE_VUNAME:  return isKnownRelationship_VUCVE_VUName();
        	case RT_VUDESCRIPTION_VUMS:  return isKnownRelationship_VUDescription_VUMS();
        	case RT_VUDESCRIPTION_VUCVE:  return isKnownRelationship_VUDescription_VUCVE();
        	case RT_VUCVE_VUMS:  return isKnownRelationship_VUCVE_VUMS();
        	case RT_SWPRODUCT_VUNAME:	return isKnownRelationship_SWProduct_VUName();
        	case RT_SWPRODUCT_VUMS:	return isKnownRelationship_SWProduct_VUMS();
        	case RT_SWPRODUCT_VUCVE:	return isKnownRelationship_SWProduct_VUCVE();
        	case RT_SWVERSION_VUNAME:	return isKnownRelationship_SWVersion_VUName();
        	case RT_SWVERSION_VUMS:	return isKnownRelationship_SWVersion_VUMS();
        	case RT_SWVERSION_VUCVE:	return isKnownRelationship_SWVersion_VUCVE();
        	case RT_SWPRODUCT_FINAME:	return isKnownRelationship_SWProduct_FIName();
        	case RT_SWVERSION_FINAME:	return isKnownRelationship_SWVersion_FIName();
        	case RT_SWPRODUCT_FUNAME:	return isKnownRelationship_SWProduct_FUName();
        	case RT_SWVERSION_FUNAME:	return isKnownRelationship_SWVersion_FUName();
        	case RT_VUNAME_FINAME:	return isKnownRelationship_VUName_FIName();
        	case RT_VUCVE_FINAME:	return isKnownRelationship_VUCVE_FIName();
        	case RT_VUMS_FINAME:	return isKnownRelationship_VUMS_FIName();
        	case RT_VUNAME_FUNAME:	return isKnownRelationship_VUName_FUName();
        	case RT_VUCVE_FUNAME:	return isKnownRelationship_VUCVE_FUName();
        	case RT_VUMS_FUNAME:	return isKnownRelationship_VUMS_FUName();

        	//Same as above, but for when the order of entities is reversed (and thus the relationship type number is negated)
        	case -RT_SWVENDOR_SWPRODUCT:  return isKnownRelationship_SWVendor_SWProduct();
        	case -RT_SWVERSION_SWPRODUCT:  return isKnownRelationship_SWVersion_SWProduct();
        	case -RT_VUDESCRIPTION_VUNAME:  return isKnownRelationship_VUDescription_VUName();
        	case -RT_VUMS_VUNAME:  return isKnownRelationship_VUMS_VUName();
        	case -RT_VUCVE_VUNAME:  return isKnownRelationship_VUCVE_VUName();
        	case -RT_VUDESCRIPTION_VUMS:  return isKnownRelationship_VUDescription_VUMS();
        	case -RT_VUDESCRIPTION_VUCVE:  return isKnownRelationship_VUDescription_VUCVE();
        	case -RT_VUCVE_VUMS:  return isKnownRelationship_VUCVE_VUMS();
        	case -RT_SWPRODUCT_VUNAME:	return isKnownRelationship_SWProduct_VUName();
        	case -RT_SWPRODUCT_VUMS:	return isKnownRelationship_SWProduct_VUMS();
        	case -RT_SWPRODUCT_VUCVE:	return isKnownRelationship_SWProduct_VUCVE();
        	case -RT_SWVERSION_VUNAME:	return isKnownRelationship_SWVersion_VUName();
        	case -RT_SWVERSION_VUMS:	return isKnownRelationship_SWVersion_VUMS();
        	case -RT_SWVERSION_VUCVE:	return isKnownRelationship_SWVersion_VUCVE();
        	case -RT_SWPRODUCT_FINAME:	return isKnownRelationship_SWProduct_FIName();
        	case -RT_SWVERSION_FINAME:	return isKnownRelationship_SWVersion_FIName();
        	case -RT_SWPRODUCT_FUNAME:	return isKnownRelationship_SWProduct_FUName();
        	case -RT_SWVERSION_FUNAME:	return isKnownRelationship_SWVersion_FUName();
        	case -RT_VUNAME_FINAME:	return isKnownRelationship_VUName_FIName();
        	case -RT_VUCVE_FINAME:	return isKnownRelationship_VUCVE_FIName();
        	case -RT_VUMS_FINAME:	return isKnownRelationship_VUMS_FIName();
        	case -RT_VUNAME_FUNAME:	return isKnownRelationship_VUName_FUName();
        	case -RT_VUCVE_FUNAME:	return isKnownRelationship_VUCVE_FUName();
        	case -RT_VUMS_FUNAME:	return isKnownRelationship_VUMS_FUName();
        	
        	default: return null;
		}
	}
	
	
	public Boolean isKnownRelationship_SWVendor_SWProduct()
	{
		String vendoralias = "";
		String productalias = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_VENDOR)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.SW_PRODUCT))) {
			vendoralias = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			productalias = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			vendoralias = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			productalias = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<SoftwareWVersion> softwares = SoftwareWVersion.getAllSoftwaresWithAlias(productalias);
		if (softwares != null) {
			for(SoftwareWVersion swv : softwares)
			{
				for(String swvvendoralias : swv.getVendorAliases()) {
					if(vendoralias.equalsIgnoreCase(swvvendoralias)) {
						return true;
					}
				}
			}
		}
		
		if(SoftwareWVersion.isKnownProduct(productalias) && SoftwareWVersion.isKnownVendor(vendoralias)) {
				return false;
		}
		
		return null;
	}
	
	public Boolean isKnownRelationship_SWVersion_SWProduct()
	{
		String version = "";
		String productalias = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_VERSION)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.SW_PRODUCT))) {
			version = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			productalias = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			version = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			productalias = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<SoftwareWVersion> softwares = SoftwareWVersion.getAllSoftwaresWithAlias(productalias);
		if (softwares != null) {
			for(SoftwareWVersion swv : softwares)
			{
				if(version.equals(swv.getVersion()))
					return true;
			}
		}
		
		if ((SoftwareWVersion.isKnownProduct(productalias)) && (SoftwareWVersion.getAllVersions() != null) && (SoftwareWVersion.getAllVersions().contains(version))) {
				return false;
		}
		
		return null;
	}
	
	public Boolean isKnownRelationship_VUDescription_VUName()
	{
		String description = "";
		String vulnerabilityname = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_DESC)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_NAME))) {
			description = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			vulnerabilityname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			description = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			vulnerabilityname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		if (Vulnerability.getVulnerabilitiesWithName(vulnerabilityname) != null) {
			for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithName(vulnerabilityname))
			{
				if(vulnerability.getDescription() != null && vulnerability.getDescription().contains(description))
					return true;
			}
		}
		
		if ((Vulnerability.getAllRelevantTerms() != null) && (Vulnerability.getAllNames() != null)) {
			if(Vulnerability.getAllRelevantTerms().contains(description) && Vulnerability.getAllNames().contains(vulnerabilityname))
				return false;
		}
			
		return null;
	}
	
	public Boolean isKnownRelationship_VUMS_VUName()
	{
		String ms = "";
		String vulnerabilityname = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_MS)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_NAME))) {
			ms = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			vulnerabilityname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			ms = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			vulnerabilityname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		if (Vulnerability.getVulnerabilitiesWithName(vulnerabilityname) != null) {
			for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithName(vulnerabilityname))
			{
				if ((vulnerability.getMSID() != null) && (vulnerability.getMSID().equals(ms)))
					return true;
			}
		}
		
		if ((Vulnerability.getAllMSs() != null) && (Vulnerability.getAllNames() != null)) {
			if(Vulnerability.getAllMSs().contains(ms) && Vulnerability.getAllNames().contains(vulnerabilityname))
				return false;
		}
		
		return null;
	}
	
	public Boolean isKnownRelationship_VUCVE_VUName()
	{
		String cve = "";
		String vulnerabilityname = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_CVE)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_NAME))) {
			cve = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			vulnerabilityname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			cve = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			vulnerabilityname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		Vulnerability vulnerability = Vulnerability.getVulnerabilityFromCVEID(cve);
		if((vulnerability != null) && (vulnerability.getName() != null) && (vulnerability.getName().equals(vulnerabilityname))) {
			return true;
		}
		
		if ((Vulnerability.getAllCVEs() != null) && (Vulnerability.getAllNames() != null)) {
			if(Vulnerability.getAllCVEs().contains(cve) && Vulnerability.getAllNames().contains(vulnerabilityname))
				return false;
		}
		
		return null;
	}
	
	public Boolean isKnownRelationship_VUDescription_VUMS()
	{
		String description = "";
		String ms = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_DESC)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_MS))) {
			description = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			ms = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			description = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			ms = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		if (Vulnerability.getVulnerabilitiesWithMSid(ms) != null) {
			for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithMSid(ms))
			{
				if(vulnerability.getDescription() != null && vulnerability.getDescription().contains(description))
					return true;
			}
		}
		
		if ((Vulnerability.getAllRelevantTerms() != null) && (Vulnerability.getAllMSs() != null)) {
			if(Vulnerability.getAllRelevantTerms().contains(description) && Vulnerability.getAllMSs().contains(ms))
				return false;
		}
		
		return null;
	}
	
	public Boolean isKnownRelationship_VUDescription_VUCVE()
	{
		String description = "";
		String cve = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_DESC)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_CVE))) {
			description = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			cve = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			description = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			cve = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		Vulnerability v = Vulnerability.getVulnerabilityFromCVEID(cve);
		if(v != null && v.getDescription() != null && v.getDescription().contains(description))
			return true;
		
		if ((Vulnerability.getAllRelevantTerms() != null) && (Vulnerability.getAllCVEs() != null)) {
			if(Vulnerability.getAllRelevantTerms().contains(description) && Vulnerability.getAllCVEs().contains(cve))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_VUCVE_VUMS()
	{
		String cve = "";
		String ms = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_CVE)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_MS))) {
			cve = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			ms = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			cve = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			ms = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		Vulnerability v = Vulnerability.getVulnerabilityFromCVEID(cve);
		if((v != null) && (v.getMSID() != null) && (v.getMSID().equals(ms)))
			return true;
		
		if ((Vulnerability.getAllMSs() != null) && (Vulnerability.getAllCVEs() != null)) {
			if(Vulnerability.getAllMSs().contains(ms) && Vulnerability.getAllCVEs().contains(cve))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_SWProduct_VUName()
	{
		String swproduct = "";
		String vuname = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_PRODUCT)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_NAME))) {
			swproduct = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			vuname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			swproduct = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			vuname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		if (Vulnerability.getVulnerabilitiesWithName(vuname) != null) {
			for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithName(vuname))
			{
				if (vulnerability.getRelationships() != null) {
					for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
					{
						if((relationship.getSoftwareWVersion() != null) && (relationship.getSoftwareWVersion().getSoftwareAliases().contains(swproduct)))
							return true;
					}
				}
			}
		}
		
		if (Vulnerability.getAllNames() != null) {
			if(SoftwareWVersion.isKnownProduct(swproduct) && Vulnerability.getAllNames().contains(vuname))
				return false;
		}
		
		return null;
	}
	
	public Boolean isKnownRelationship_SWProduct_VUMS()
	{
		String swproduct = "";
		String ms = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_PRODUCT)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_MS))) {
			swproduct = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			ms = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			swproduct = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			ms = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		if (Vulnerability.getVulnerabilitiesWithMSid(ms) != null) {
			for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithMSid(ms))
			{
				if (vulnerability.getRelationships() != null) {
					for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
					{
						if((relationship.getSoftwareWVersion() != null) && (relationship.getSoftwareWVersion().getSoftwareAliases().contains(swproduct)))
							return true;
					}
				}
			}
		}
		
		if (Vulnerability.getAllMSs() != null) {
			if(SoftwareWVersion.isKnownProduct(swproduct) && Vulnerability.getAllMSs().contains(ms))
				return false;
		}
		
		return null;
	}
	
	public Boolean isKnownRelationship_SWProduct_VUCVE()
	{
		String swproduct = "";
		String cve = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_PRODUCT)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_CVE))) {
			swproduct = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			cve = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			swproduct = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			cve = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		Vulnerability vulnerability = Vulnerability.getVulnerabilityFromCVEID(cve);
		if(vulnerability != null)
		{
			if (vulnerability.getRelationships() != null) {
				for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
				{
					if((relationship.getSoftwareWVersion() != null) && (relationship.getSoftwareWVersion().getSoftwareAliases().contains(swproduct)))
						return true;
				}
			}
		}
		
		if (Vulnerability.getAllCVEs() != null) {
			if(SoftwareWVersion.isKnownProduct(swproduct) && Vulnerability.getAllCVEs().contains(cve))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_SWVersion_VUName()
	{
		String swversion = "";
		String vuname = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_VERSION)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_NAME))) {
			swversion = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			vuname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			swversion = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			vuname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		if (Vulnerability.getVulnerabilitiesWithName(vuname) != null) {
			for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithName(vuname))
			{
				if (vulnerability.getRelationships() != null) {
					for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
					{
						if((relationship.getSoftwareWVersion() != null) && (relationship.getSoftwareWVersion().getVersion() != null) && (relationship.getSoftwareWVersion().getVersion().equals(swversion)))
							return true;
					}
				}
			}
		}
		
		if ((SoftwareWVersion.getAllVersions() != null) && (Vulnerability.getAllNames() != null)) {
			if(SoftwareWVersion.getAllVersions().contains(swversion) && Vulnerability.getAllNames().contains(vuname))
				return false;
		}
		
		return null;
	}
	
	public Boolean isKnownRelationship_SWVersion_VUMS()
	{
		String swversion = "";
		String ms = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_VERSION)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_MS))) {
			swversion = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			ms = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			swversion = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			ms = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		if (Vulnerability.getVulnerabilitiesWithMSid(ms) != null) {
			for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithMSid(ms))
			{
				if (vulnerability.getRelationships() != null) {
					for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
					{
						if((relationship.getSoftwareWVersion() != null) && (relationship.getSoftwareWVersion().getVersion() != null) && (relationship.getSoftwareWVersion().getVersion().equals(swversion)))
							return true;
					}
				}
			}
		}
		
		if ((SoftwareWVersion.getAllVersions() != null) && (Vulnerability.getAllMSs() != null)) {
			if(SoftwareWVersion.getAllVersions().contains(swversion) && Vulnerability.getAllMSs().contains(ms))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_SWVersion_VUCVE()
	{
		String swversion = "";
		String cve = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_VERSION)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.VULN_CVE))) {
			swversion = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			cve = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			swversion = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			cve = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		Vulnerability vulnerability = Vulnerability.getVulnerabilityFromCVEID(cve);
		if((vulnerability != null) && (vulnerability.getRelationships() != null))
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
			{
				if((relationship.getSoftwareWVersion() != null) && (relationship.getSoftwareWVersion().getVersion() != null) && (relationship.getSoftwareWVersion().getVersion().equals(swversion)))
					return true;
			}
		}
		
		if ((SoftwareWVersion.getAllVersions() != null) && (Vulnerability.getAllCVEs() != null)) {
			if(SoftwareWVersion.getAllVersions().contains(swversion) && Vulnerability.getAllCVEs().contains(cve))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_SWProduct_FIName()
	{
		String swproduct = "";
		String filename = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_PRODUCT)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.FILE_NAME))) {
			swproduct = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			filename = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			swproduct = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			filename = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFileName(filename);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if((relationship.getSoftwareWVersion() != null) && (relationship.getSoftwareWVersion().getSoftwareAliases().contains(swproduct)))
					return true;
			}
		}
		
		if (VulnerabilityToSoftwareWVersionRelationship.getAllFileNames() != null) {
			if(SoftwareWVersion.isKnownProduct(swproduct) && VulnerabilityToSoftwareWVersionRelationship.getAllFileNames().contains(filename))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_SWVersion_FIName()
	{
		String swversion = "";
		String filename = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_VERSION)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.FILE_NAME))) {
			swversion = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			filename = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			swversion = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			filename = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFileName(filename);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if((relationship.getSoftwareWVersion() != null) && (relationship.getSoftwareWVersion().getVersion() != null) && (relationship.getSoftwareWVersion().getVersion().equals(swversion)))
					return true;
			}
		}
		
		if ((SoftwareWVersion.getAllVersions() != null) && (VulnerabilityToSoftwareWVersionRelationship.getAllFileNames() != null)) {
			if(SoftwareWVersion.getAllVersions().contains(swversion) && VulnerabilityToSoftwareWVersionRelationship.getAllFileNames().contains(filename))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_SWProduct_FUName()
	{
		String swproduct = "";
		String functionname = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_PRODUCT)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.FUNCTION_NAME))) {
			swproduct = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			functionname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			swproduct = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			functionname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFunctionName(functionname);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if((relationship.getSoftwareWVersion() != null) && (relationship.getSoftwareWVersion().getSoftwareAliases().contains(swproduct)))
					return true;
			}
		}
		
		if (VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames() != null) {
			if(SoftwareWVersion.isKnownProduct(swproduct) && VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames().contains(functionname))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_SWVersion_FUName()
	{
		String swversion = "";
		String functionname = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.SW_VERSION)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.FUNCTION_NAME))) {
			swversion = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			functionname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			swversion = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			functionname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFunctionName(functionname);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if((relationship.getSoftwareWVersion() != null) && (relationship.getSoftwareWVersion().getVersion() != null) && (relationship.getSoftwareWVersion().getVersion().equals(swversion)))
					return true;
			}
		}
		
		if ((SoftwareWVersion.getAllVersions() != null) && (VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames() != null)) {
			if(SoftwareWVersion.getAllVersions().contains(swversion) && VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames().contains(functionname))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_VUName_FIName()
	{
		String vulnerabilityname = "";
		String filename = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_NAME)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.FILE_NAME))) {
			vulnerabilityname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			filename = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			vulnerabilityname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			filename = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFileName(filename);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if((relationship.getVulnerability() != null) && (relationship.getVulnerability().getName() != null) && (relationship.getVulnerability().getName().equals(vulnerabilityname)))
					return true;
			}
		}
		
		if ((Vulnerability.getAllNames() != null) && (VulnerabilityToSoftwareWVersionRelationship.getAllFileNames() != null)) {
			if(Vulnerability.getAllNames().contains(vulnerabilityname) && VulnerabilityToSoftwareWVersionRelationship.getAllFileNames().contains(filename))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_VUCVE_FIName()
	{
		String cve = "";
		String filename = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_CVE)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.FILE_NAME))) {
			cve = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			filename = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			cve = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			filename = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFileName(filename);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if((relationship.getVulnerability() != null) && (relationship.getVulnerability().getCVEID() != null) && (relationship.getVulnerability().getCVEID().equals(cve)))
					return true;
			}
		}
		
		if ((Vulnerability.getAllCVEs() != null) && (VulnerabilityToSoftwareWVersionRelationship.getAllFileNames() != null)) {
			if(Vulnerability.getAllCVEs().contains(cve) && VulnerabilityToSoftwareWVersionRelationship.getAllFileNames().contains(filename))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_VUMS_FIName()
	{
		String ms = "";
		String filename = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_MS)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.FILE_NAME))) {
			ms = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			filename = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			ms = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			filename = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFileName(filename);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if((relationship.getVulnerability() != null) && (relationship.getVulnerability().getMSID() != null) && (relationship.getVulnerability().getMSID().equals(ms)))
					return true;
			}
		}
		
		if ((Vulnerability.getAllMSs() != null) && (VulnerabilityToSoftwareWVersionRelationship.getAllFileNames() != null)) {
			if(Vulnerability.getAllMSs().contains(ms) && VulnerabilityToSoftwareWVersionRelationship.getAllFileNames().contains(filename))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_VUName_FUName()
	{
		String vulnerabilityname = "";
		String functionname = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_NAME)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.FUNCTION_NAME))) {
			vulnerabilityname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			functionname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			vulnerabilityname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			functionname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFunctionName(functionname);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if((relationship.getVulnerability() != null) && (relationship.getVulnerability().getName() != null) && (relationship.getVulnerability().getName().equals(vulnerabilityname)))
					return true;
			}
		}
		
		if ((Vulnerability.getAllNames() != null) && (VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames() != null)) {
			if(Vulnerability.getAllNames().contains(vulnerabilityname) && VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames().contains(functionname))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_VUCVE_FUName()
	{
		String cve = "";
		String functionname = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_CVE)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.FUNCTION_NAME))) {
			cve = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			functionname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			cve = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			functionname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFunctionName(functionname);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if((relationship.getVulnerability() != null) && (relationship.getVulnerability().getCVEID() != null) && (relationship.getVulnerability().getCVEID().equals(cve)))
					return true;
			}
		}
		
		if ((Vulnerability.getAllCVEs() != null) && (VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames() != null)) {
			if(Vulnerability.getAllCVEs().contains(cve) && VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames().contains(functionname))
				return false;
		}
		
		return null;
	}

	public Boolean isKnownRelationship_VUMS_FUName()
	{
		String ms = "";
		String functionname = "";
		if ((entity1.getCyberType().equals(CyberHeuristicAnnotator.VULN_MS)) && (entity2.getCyberType().equals(CyberHeuristicAnnotator.FUNCTION_NAME))) {
			ms = CyberEntity.getEntitySpacedText(entity1.getCyberText());
			functionname = CyberEntity.getEntitySpacedText(entity2.getCyberText());
		}
		else {
			ms = CyberEntity.getEntitySpacedText(entity2.getCyberText());
			functionname = CyberEntity.getEntitySpacedText(entity1.getCyberText());
		}
		
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFunctionName(functionname);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if((relationship.getVulnerability() != null) && (relationship.getVulnerability().getMSID() != null) && (relationship.getVulnerability().getMSID().equals(ms)))
					return true;
			}
		}
		
		if ((Vulnerability.getAllMSs() != null) && (VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames() != null)) {
			if(Vulnerability.getAllMSs().contains(ms) && VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames().contains(functionname))
				return false;
		}
		
		return null;
	}
	
	
	//Just for testing, loads all known relationships and aliaes from xml and json files and prints statistics about them.
	public static void main(String[] args)
	{
		loadAllKnownRelationships();
		
		printRelationStatistics();
	}
	
	private static void printRelationStatistics()
	{
		Collection<SoftwareWVersion> softwares = SoftwareWVersion.getAllSoftwareWVersions();
		
		
		//·         sw.vendor & sw.product --> same software
		HashSet<String> relationholder = new HashSet<String>();
		for(SoftwareWVersion software : softwares)
			if(software.getVendor() != null && software.getName() != null)
				relationholder.add(software.getVendor() + "\t" + software.getName());
		System.out.println("sw.vendor & sw.product:\t" + relationholder.size());
		
		//·         sw.version & sw.product --> same software
		relationholder = new HashSet<String>();
		for(SoftwareWVersion software : softwares)
			if(software.getName() != null && software.getVersion() != null)
				relationholder.add(software.getName() + "\t" + software.getVersion());
		System.out.println("sw.version & sw.product:\t" + relationholder.size());
		
		//·         vuln.description & vuln.name --> same vulnerability
		relationholder = new HashSet<String>();
		HashMap<String,Vulnerability> vulnerabilitynameTovulnerability = Vulnerability.getCveIdToVulnerability();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getName() != null && vulnerability.getDescription() != null)
				relationholder.add(vulnerability.getName() + "\t" + vulnerability.getDescription());
		System.out.println("vuln.description & vuln.name:\t" + relationholder.size());
		
		//·         vuln.ms & vuln.name --> same vulnerability
		relationholder = new HashSet<String>();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getMSID() != null && vulnerability.getName() != null)
				relationholder.add(vulnerability.getMSID() + "\t" + vulnerability.getName());
		System.out.println("vuln.ms & vuln.name:\t" + relationholder.size());
		
		//·         vuln.cve & vuln.name --> same vulnerability
		relationholder = new HashSet<String>();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getName() != null && vulnerability.getCVEID() != null)
				relationholder.add(vulnerability.getCVEID() + "\t" + vulnerability.getName());
		System.out.println("vuln.cve & vuln.name:\t" + relationholder.size());
		
		//·         vuln.description & vuln.ms --> same vulnerability
		relationholder = new HashSet<String>();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getDescription() != null && vulnerability.getMSID() != null)
				relationholder.add(vulnerability.getDescription() + "\t" + vulnerability.getMSID());
		System.out.println("vuln.description & vuln.ms:\t" + relationholder.size());
		
		//·         vuln.description & vuln.cve --> same vulnerability		
		relationholder = new HashSet<String>();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getDescription() != null && vulnerability.getCVEID() != null)
				relationholder.add(vulnerability.getDescription() + "\t" + vulnerability.getCVEID());
		System.out.println("vuln.description & vuln.cve:\t" + relationholder.size());
		
		//·         vuln.cve & vuln.ms --> same vulnerability
		relationholder = new HashSet<String>();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getCVEID() != null && vulnerability.getMSID() != null)
				relationholder.add(vulnerability.getCVEID() + "\t" + vulnerability.getMSID());
		System.out.println("vuln.cve & vuln.ms:\t" + relationholder.size());
		
		//·         vuln.* & sw.* --> ExploitTargetRelatedObservable
		relationholder = new HashSet<String>();
		for(VulnerabilityToSoftwareWVersionRelationship relationship : VulnerabilityToSoftwareWVersionRelationship.getPrimaryKeyToRelationship().values())
			if(relationship.getVulnerability().getCVEID() != null && relationship.getSoftwareWVersion().getName() != null)
				relationholder.add(relationship.getVulnerability().getCVEID() + "\t" + relationship.getSoftwareWVersion().getName());
		System.out.println("vuln.* & sw.*:\t" + relationholder.size() + "\t(note that different versions do not count as different sw.*s here or below)");
		
		//·         sw.* & file.name --> Sub-Observable
		relationholder = new HashSet<String>();
		for(VulnerabilityToSoftwareWVersionRelationship relationship : VulnerabilityToSoftwareWVersionRelationship.getPrimaryKeyToRelationship().values())
		{
			HashSet<String> filenames = relationship.getFileNames();
			if(filenames != null)
			{
				for(String filename : filenames)
					if(relationship.getSoftwareWVersion().getName() != null && filename != null)
						relationholder.add(relationship.getSoftwareWVersion().getName() + "\t" + filename);
			}
		}
		System.out.println("sw.* & file.name:\t" + relationholder.size());
		
		//·         sw.* & function.name --> Sub-Observable
		relationholder = new HashSet<String>();
		for(VulnerabilityToSoftwareWVersionRelationship relationship : VulnerabilityToSoftwareWVersionRelationship.getPrimaryKeyToRelationship().values())
		{
			HashSet<String> functionnames = relationship.getFunctionNames();
			if(functionnames != null)
			{
				for(String functionname : functionnames)
					if(relationship.getSoftwareWVersion().getName() != null && functionname != null)
						relationholder.add(relationship.getSoftwareWVersion().getName() + "\t" + functionname);
			}
		}
		System.out.println("sw.* & function.name:\t" + relationholder.size());
		
		//·         vuln.* & file.name --> ExploitTargetRelatedObservable
		relationholder = new HashSet<String>();
		for(VulnerabilityToSoftwareWVersionRelationship relationship : VulnerabilityToSoftwareWVersionRelationship.getPrimaryKeyToRelationship().values())
		{
			HashSet<String> filenames = relationship.getFileNames();
			if(filenames != null)
			{
				for(String filename : filenames)
					if(relationship.getVulnerability().getCVEID() != null && filename != null)
						relationholder.add(relationship.getVulnerability().getCVEID() + "\t" + filename);
			}
		}
		System.out.println("vuln.* & file.name:\t" + relationholder.size());
		
		//·         vuln.* & function.name --> ExploitTargetRelatedObservable
		relationholder = new HashSet<String>();
		for(VulnerabilityToSoftwareWVersionRelationship relationship : VulnerabilityToSoftwareWVersionRelationship.getPrimaryKeyToRelationship().values())
		{
			HashSet<String> functionnames = relationship.getFunctionNames();
			if(functionnames != null)
			{
				for(String functionname : functionnames)
					if(relationship.getVulnerability().getCVEID() != null && functionname != null)
						relationholder.add(relationship.getVulnerability().getCVEID() + "\t" + functionname);
			}
		}
		System.out.println("vuln.* & function.name:\t" + relationholder.size());
	}
	
	public static HashSet<Integer> getAllRelationshipTypesSet()
	{
		return allrelationshiptypesset;
	}

	//A reverse relationship just has entities coming in the opposite order in the text.  We just gave these
	//reverse relationships IDs that are -1 times the original relationship's id.
//	public static int getReverseRelationshipType(int relationshiptype)
//	{
//		return -relationshiptype;
//	}
	
	
	
}
