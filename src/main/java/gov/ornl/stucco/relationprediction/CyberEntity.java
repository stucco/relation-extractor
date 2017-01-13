package gov.ornl.stucco.relationprediction;
//This class stores information about the text of some entity that has been identified.  It stores the text and the predicted
//entity type.  There are a bunch of static maps here that map things like the entity type constants near the top to
//string versions of them.  I also manually figured out what order the entity type probabilities are outputted in, so there
//is a map for converting those indices to one of the entity type integer constants at the top.


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator;
import gov.ornl.stucco.entity.models.CyberEntityType;


public class CyberEntity 
{
	public static Set<CyberEntityType> entityTypeSet = new HashSet<CyberEntityType>();
	static {
		entityTypeSet.add(CyberHeuristicAnnotator.O);
		entityTypeSet.add(CyberHeuristicAnnotator.SW_VENDOR);
		entityTypeSet.add(CyberHeuristicAnnotator.SW_PRODUCT);
		entityTypeSet.add(CyberHeuristicAnnotator.SW_VERSION);
		entityTypeSet.add(CyberHeuristicAnnotator.FILE_NAME);
		entityTypeSet.add(CyberHeuristicAnnotator.FUNCTION_NAME);
		entityTypeSet.add(CyberHeuristicAnnotator.VULN_CVE);
		entityTypeSet.add(CyberHeuristicAnnotator.VULN_MS);
		entityTypeSet.add(CyberHeuristicAnnotator.VULN_NAME);
		entityTypeSet.add(CyberHeuristicAnnotator.VULN_DESC);
	}
	
	private String text;
	private CyberEntityType type;
	
	public CyberEntity(String cyberText, CyberEntityType cyberType) {
		this.text = cyberText;
		if (cyberType != null) {
			this.type = cyberType;
		}
		else {
			this.type = CyberHeuristicAnnotator.O;
		}
	}
	
	public String getCyberText() {
		return this.text;
	}
	
	public CyberEntityType getCyberType() {
		return this.type;
	}
	
	//If the token looks like a cyber entity token, return a CyberEntityText.  Otherwise, return null.
	public static CyberEntity getCyberEntityFromToken(String token)
	{
		CyberEntityType entitytype = getEntityTypeFromToken(token);
		
		if ((entitytype == null) || (entitytype.equals(CyberHeuristicAnnotator.O))) {
			return null;
		}
		else {
			return new CyberEntity(token, entitytype);
		}
	}
	
	public static String getEntitySpacedText(String entitytext)
	{
		String result = entitytext;
		if(entitytext.startsWith("["))
		{
			result = entitytext;
			if(result.contains("_"))
			{
				result = result.substring(result.indexOf('_')+1, result.length()-1);
				result = result.replaceAll("_", " ");
				result = result.trim();
			}
		}
		return result;
	}
		

	//Tokens from preprocessed documents are formatted in a certain way.  Particularly, if the token 
	//is a cyber entity (as detected by the cyber entity detector), it will start with 
	//[entity.type_word1_word2...].  If the token is not a cyber entity, there is no special formatting.
	//This method returns the constant integer associated with the given entity's type, provided it is
	//a cyber entity.  Otherwise, if it is not formatted like a cyber entity, it returns type O (the 
	//non-entity type).
	public static CyberEntityType getEntityTypeFromToken(String token)
	{
		CyberEntityType result = null;
		if(token.charAt(0) != '[')
			return null;
		
		int indexoffirstspace = token.indexOf('_');
		if(indexoffirstspace == -1 && token.charAt(token.length()-1) == ']')
			indexoffirstspace = token.length()-1;
		
		if(indexoffirstspace <= 0)
			return null;
		
		String tokenlabel = token.substring(1, indexoffirstspace);
		if (tokenlabel.contains(".")) {
			int index = tokenlabel.indexOf(".");
			String type = tokenlabel.substring(0, index);
			String subType = tokenlabel.substring(index + 1);
			result = new CyberEntityType(type, subType);
		}
		
		return result;
	}
	
	
	public static String getCanonicalName(String name, CyberEntityType entitytype)
	{
		ArrayList<Vulnerability> vulnerabilities;
		String result = name;
		String holder;
		
		if (entitytype.equals(CyberHeuristicAnnotator.VULN_MS)) {
			vulnerabilities = Vulnerability.getVulnerabilitiesWithMSid(name);
	  		if(vulnerabilities.size() == 1) {
	  			result = vulnerabilities.get(0).getCanonicalName();
	  		}
		}
		else if (entitytype.equals(CyberHeuristicAnnotator.VULN_NAME)) {
			vulnerabilities = Vulnerability.getVulnerabilitiesWithName(name);
	  		if(vulnerabilities.size() == 1) {
	  			result = vulnerabilities.get(0).getCanonicalName();
	  		}
		}
		else if (entitytype.equals(CyberHeuristicAnnotator.SW_PRODUCT)) {
			holder = SoftwareWVersion.getCanonicalSoftwareAlias(name);
	  		if(holder != null) {
	  			result = holder;
	  		}
		}
		else if (entitytype.equals(CyberHeuristicAnnotator.SW_VENDOR)) {
			holder = SoftwareWVersion.getCanonicalVendorAlias(name);
	  		if(holder != null) {
	  			result = holder;
	  		}
		}
		  
		return result;
	}


}
