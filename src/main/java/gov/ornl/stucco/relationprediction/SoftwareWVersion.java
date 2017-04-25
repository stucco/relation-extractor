//Instances of this class store information about a particular software product version.  So it includes the product's
//name, its vendor, and its version number.
package gov.ornl.stucco.relationprediction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator;
import gov.ornl.stucco.entity.models.CyberEntityType;
import gov.ornl.stucco.heurstics.utils.FreebaseEntry;
import gov.ornl.stucco.heurstics.utils.FreebaseList;
import gov.ornl.stucco.heurstics.utils.ListLoader;


public class SoftwareWVersion 
{
	private static String softwarevendorsfilelocation = "dictionaries/software_developers.json";
	private static String softwareinfofilelocation = "dictionaries/software_info.json";
	private static String operatingsystemsfilelocation = "dictionaries/operating_systems.json";
	
	
	
	private String softwareid;
	private String vendor;
	private String name;
	private String version;
	
	private ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships;
	
	private static FreebaseList softwareToAliases;
	private static FreebaseList vendorToAliases;
	private static HashSet<String> allknownversions;

	private static HashMap<String,SoftwareWVersion> softwareidTosoftwarewversion = new HashMap<String,SoftwareWVersion>();
	
	private static HashMap<String,HashSet<String>> softwarealiasTosoftwarewversionids = new HashMap<String,HashSet<String>>();
	
	
	private SoftwareWVersion(String softwareid, String vendor, String name, String version)
	{
		this.softwareid = softwareid;
		this.vendor = vendor;
		this.name = name;
		this.version = version;
	}
	
	
	public String getSoftwareID()
	{
		return softwareid;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getVendor()
	{
		return vendor;
	}
	
	public String getVersion()
	{
		return version;
	}
	
	public ArrayList<String> getSoftwareAliases()
	{
		ArrayList<String> aliases = new ArrayList<String>();
		if (getSoftwareToAliases() != null) {
			for (FreebaseEntry entry : getSoftwareToAliases().getEntries()) {
				if (entry.contains(name)) {
					aliases.add(entry.getName());
					aliases.addAll(entry.getAliases());
				}
			}
			for (FreebaseEntry entry : getSoftwareToAliases().getEntries()) {
				if (entry.contains(vendor + " " + name)) {
					aliases = new ArrayList<String>();
					aliases.add(entry.getName());
					aliases.addAll(entry.getAliases());
				}
			}
		}
		
		return aliases;
	}
	
	public ArrayList<String> getVendorAliases()
	{
		ArrayList<String> aliases = new ArrayList<String>();
		if (getVendorToAliases() != null) {
			for (FreebaseEntry entry : getVendorToAliases().getEntries()) {
				if (entry.contains(vendor)) {
					aliases.add(entry.getName());
					aliases.addAll(entry.getAliases());
				}
			}
		}
		return aliases;
	}
	
	public void addRelationship(VulnerabilityToSoftwareWVersionRelationship relationship)
	{
		if(relationships == null)
			relationships = new ArrayList<VulnerabilityToSoftwareWVersionRelationship>();
		
		relationships.add(relationship);
	}
	
	public ArrayList<VulnerabilityToSoftwareWVersionRelationship> getRelationships()
	{
		return relationships;
	}
	
	
	//If we have already created a SoftwareWVersion instance having this software id, get it from the softwareidTosoftwarewversion map and return it.  Else create it, add it to the map, then return it.
	public static SoftwareWVersion getSoftwareFromSoftwareID(String softwareid)
	{
		if(softwareidTosoftwarewversion == null)
			softwareidTosoftwarewversion = new HashMap<String,SoftwareWVersion>();
		
		SoftwareWVersion result = softwareidTosoftwarewversion.get(softwareid);
		
		if(result == null)
		{
			String[] splitsoftwareid = softwareid.split(":");
			String vendor = null;
			String name = null;
			String version = null;

			if(splitsoftwareid.length >= 3)
				vendor = splitsoftwareid[2].replaceAll("_", " ");
			if(splitsoftwareid.length >= 4)
				name = splitsoftwareid[3].replaceAll("_", " ");
			if(splitsoftwareid.length >= 5)
				version = splitsoftwareid[4];
			
			result = new SoftwareWVersion(softwareid, vendor, name, version);
			
			softwareidTosoftwarewversion.put(softwareid, result);
			
			if(version != null)
			{
				if(allknownversions == null)
					allknownversions = new HashSet<String>();
				
				allknownversions.add(version);
			}
		}
		
		return result;
	}

	public static Collection<SoftwareWVersion> getAllSoftwareWVersions()
	{
		if(softwareidTosoftwarewversion == null)
			GenericCyberEntityTextRelationship.loadAllKnownRelationships();
		
		if (softwareidTosoftwarewversion == null) {
			softwareidTosoftwarewversion = new HashMap<String,SoftwareWVersion>();
		}
		
		return softwareidTosoftwarewversion.values();
	}


	public static void setAllAliases()
	{	
		if (vendorToAliases == null)
		{
			vendorToAliases = ListLoader.loadFreebaseList(softwarevendorsfilelocation, CyberHeuristicAnnotator.SW_VENDOR.toString());
		}
		
		if (softwarealiasTosoftwarewversionids == null)
			softwarealiasTosoftwarewversionids = new HashMap<String,HashSet<String>>();
		if (softwareToAliases == null)
		{
			softwareToAliases = readSoftwaresAliases();
		}
		
		if ((softwareToAliases != null) && (softwarealiasTosoftwarewversionids.isEmpty())) {
			for(SoftwareWVersion swv : SoftwareWVersion.getAllSoftwareWVersions())
			{
				ArrayList<String> softwarealiases = swv.getSoftwareAliases();
				if((softwarealiases.isEmpty()) && (swv.getName() != null))
				{
					softwarealiases.add(swv.getName());
				}
			
				for(String alias : softwarealiases)
				{
					HashSet<String> softwarewversionids = softwarealiasTosoftwarewversionids.get(alias);
					if(softwarewversionids == null)
					{
						softwarewversionids = new HashSet<String>();
						softwarealiasTosoftwarewversionids.put(alias, softwarewversionids);
					}
					softwarewversionids.add(swv.getSoftwareID());
				}
			}
		}
	}
	
	private static FreebaseList readSoftwaresAliases()
	{
        softwareToAliases = ListLoader.loadFreebaseList(softwareinfofilelocation, (new CyberEntityType("sw", "info")).toString());
        
        FreebaseList operatingsystemslist = ListLoader.loadFreebaseList(operatingsystemsfilelocation, CyberHeuristicAnnotator.SW_PRODUCT.toString());

        softwareToAliases.addEntries(operatingsystemslist);
        
        return softwareToAliases;
	}
	

	public static ArrayList<SoftwareWVersion> getAllSoftwaresWithAlias(String alias)
	{
		if(softwarealiasTosoftwarewversionids == null)
			setAllAliases();
		if(softwareidTosoftwarewversion == null)
			GenericCyberEntityTextRelationship.loadAllKnownRelationships();
		
		ArrayList<SoftwareWVersion> result = new ArrayList<SoftwareWVersion>();
		
		HashSet<String> versionids = softwarealiasTosoftwarewversionids.get(alias);
		if(versionids != null)
		{
			for(String versionid : versionids)
			{
				SoftwareWVersion swv = softwareidTosoftwarewversion.get(versionid);
				if(swv != null)
					result.add(swv);
			}
		}
		
		return result;
	}
	
	public static String getCanonicalVendorAlias(String alias)
	{
		if (getVendorToAliases() != null) {
			for (FreebaseEntry entry : getVendorToAliases().getEntries()) {
				if (entry.contains(alias)) {
					return entry.getName();
				}
			}
		}
		
		return null;
	}
	
	public static String getCanonicalSoftwareAlias(String alias)
	{
		if (getSoftwareToAliases() != null) {
			for (FreebaseEntry entry : getSoftwareToAliases().getEntries()) {
				if ((entry.getAliases().contains(alias)) || ((entry.getName() != null) && (entry.getName().equalsIgnoreCase(alias)))) {
					return entry.getName();
				}
			}
		}
		
		return null;
	}

	
	public static boolean isKnownVendor(String vendor) {
		if (getVendorToAliases() != null) {
			if (getVendorToAliases().contains(vendor)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public static boolean isKnownProduct(String product) {
		if (getSoftwareToAliases() != null) {
			if (getSoftwareToAliases().contains(product)) {
				return true;
			}
		}
		
		return false;		
	}
	
	
	public static Set<String> getAllVersions()
	{
		if(allknownversions == null)
			GenericCyberEntityTextRelationship.loadAllKnownRelationships();
		
		if (allknownversions == null) {
			allknownversions = new HashSet<String>();
		}
		
		return allknownversions;
	}
	

	private static FreebaseList getSoftwareToAliases()
	{
		if(softwareToAliases == null)
			setAllAliases();
		
		if (softwareToAliases == null) {
			softwareToAliases = new FreebaseList();
		}
		
		return softwareToAliases;
	}
	
	private static FreebaseList getVendorToAliases()
	{
		if(vendorToAliases == null) {
			setAllAliases();
		}
		
		if(vendorToAliases == null) {
			vendorToAliases = new FreebaseList();
		}
		
		return vendorToAliases;
	}
	
}
