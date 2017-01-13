package gov.ornl.stucco.relationprediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * The purpose of this class is to store information about the entities from which an instance was generated.
 * It includes the name of the file from which it came, the 
 */


public class InstanceID 
{

	private String filename;
	private int firsttokensentencenum;
	private int replacedfirsttokenindex;
	private int firsttokenstartindex;
	private int firsttokenendindex;
	private int secondtokensentencenum;
	private int replacedsecondtokenindex;
	private int secondtokenstartindex;
	private int secondtokenendindex;
	
	//private int heuristiclabel;
	
	InstanceID(String filename, int firsttokensentencenum, int replacedfirsttokenindex, int firsttokenstartindex, int firsttokenendindex, int secondtokensentencenum, int replacedsecondtokenindex, int secondtokenstartindex, int secondtokenendindex)
	{
		this.filename = filename;
		this.firsttokensentencenum = firsttokensentencenum;
		this.replacedfirsttokenindex = replacedfirsttokenindex;
		this.firsttokenstartindex = firsttokenstartindex;
		this.firsttokenendindex = firsttokenendindex;
		this.secondtokensentencenum = secondtokensentencenum;
		this.replacedsecondtokenindex = replacedsecondtokenindex;
		this.secondtokenstartindex = secondtokenstartindex;
		this.secondtokenendindex = secondtokenendindex;
	}
	
	InstanceID(String instanceidasstring)
	{
		filename = instanceidasstring.substring(0, instanceidasstring.lastIndexOf('-'));
		
		String indices = instanceidasstring.substring(instanceidasstring.lastIndexOf('-')+1);
		
		indices  = indices .replaceAll("\\)\\(", " ");
		indices  = indices .replaceAll("\\(", " ");
		indices  = indices .replaceAll("\\)", " ");
		indices  = indices .replaceAll(",", " ");
		indices = indices.trim();
		
		String[] indicesarray = indices.split(" ");
		firsttokensentencenum = Integer.parseInt(indicesarray[0]);
		replacedfirsttokenindex = Integer.parseInt(indicesarray[1]);
		firsttokenstartindex = Integer.parseInt(indicesarray[2]);
		firsttokenendindex = Integer.parseInt(indicesarray[3]);
		secondtokensentencenum = Integer.parseInt(indicesarray[4]);
		replacedsecondtokenindex = Integer.parseInt(indicesarray[5]);
		secondtokenstartindex = Integer.parseInt(indicesarray[6]);
		secondtokenendindex = Integer.parseInt(indicesarray[7]);
	}

	public String getFileName()
	{
		return filename;
	}
	public int getFirstTokenSentenceNum()
	{
		return firsttokensentencenum;
	}
	public int getReplacedFirstTokenIndex()
	{
		return replacedfirsttokenindex;
	}
	public int getFirstTokenStartIndex()
	{
		return firsttokenstartindex;
	}
	public int getFirstTokenEndIndex()
	{
		return firsttokenendindex;
	}
	public int getSecondTokenSentenceNum()
	{
		return secondtokensentencenum;
	}
	public int getReplacedSecondTokenIndex()
	{
		return replacedsecondtokenindex;
	}
	public int getSecondTokenStartIndex()
	{
		return secondtokenstartindex;
	}
	public int getSecondTokenEndIndex()
	{
		return secondtokenendindex;
	}
	
	/*
	public void setHeuristicLabel(int heuristiclabel)
	{
		this.heuristiclabel = heuristiclabel;
	}
	
	public int getHeuristicLabel()
	{
		return heuristiclabel;
	}
	*/

	/*
	public static HashMap<Integer,ArrayList<InstanceID>> readRelationTypeToInstanceIDOrder(String entityextractedfilename, boolean training)
	{
		HashMap<Integer,ArrayList<InstanceID>> relationtypeToinstanceidorder = new HashMap<Integer,ArrayList<InstanceID>>();
		
		try
		{
			for(Integer i : GenericCyberEntityTextRelationship.getAllRelationshipTypesSet())
			{
				ArrayList<InstanceID> instanceidorder = new ArrayList<InstanceID>();
				relationtypeToinstanceidorder.put(i,  instanceidorder);
				
				File f = ProducedFileGetter.getRelationshipSVMInstancesOrderFile(entityextractedfilename, i, training);
				
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line;
				while((line = in.readLine()) != null)
				{
					String[] splitline = line.split("\t");
					InstanceID instanceid = new InstanceID(splitline[1]);
					instanceid.setHeuristicLabel(Integer.parseInt(splitline[0]));
					instanceidorder.add(instanceid);
				}
				in.close();
			}
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
		
		return relationtypeToinstanceidorder;
	}
	*/
	
	
	public String toString()
	{
		return filename + "-(" + firsttokensentencenum + "," + replacedfirsttokenindex + "," + firsttokenstartindex + "," + firsttokenendindex + ")" + 
				"(" + secondtokensentencenum + "," + replacedsecondtokenindex + ","+ secondtokenstartindex + "," + secondtokenendindex + ")";
	}


}
