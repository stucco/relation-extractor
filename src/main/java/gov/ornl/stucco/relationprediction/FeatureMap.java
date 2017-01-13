package gov.ornl.stucco.relationprediction;
//This class stores a map of feature names to IDs (hence, why it extends HashMap<String,Integer>).  It allows the construction
//of such a map through its getIndex(), which returns the ID (Integer) associated with that feature if it is already
//in the map.  If it is not already in the map, it adds the feature to the map and returns the ID assigned to it.
//
//There are a lot of String constants at the top.  Each one is associated with one feature type I have created.  
//When one of the command line arguments calls for a feature type or a list of feature types, these strings are what
//should be given to it.  The first step in adding a new feature type is to assign the new feature type a String 
//constant like those in this class.  The second step is to add an if statement to WriteRelationInstanceFiles to 
//direct the program’s flow to a new method (which you will create) for writing the instances to a new file alongside your 
//new feature type.
//Of course, the details of how to create features of your new type will vary greatly depending on what the feature is 
//meant to encode.  See WriteRelationInstance.writeParseTreePathFile for an example of a method that writes features using 
//the .ser.gz (entity-extracted) file associated with each instance, or see WriteRelationInstanceFiles.writeContextFile 
//for an example of a method that writes features using the preprocessed file written by PrintPreprocessedDocuments.



import java.util.*;

import edu.stanford.nlp.io.EncodingPrintWriter.out;

import java.io.*;

public class FeatureMap extends HashMap<String,Integer>
{
	//In the presentation, abcd was the baseline feature set, and abcdefgmnopqr was the complete feature set.
	
	public static final String WORDEMBEDDINGBEFORECONTEXT = "a";	//Context before the first entity as encoded via averaging the words' word2vec vectors.  Also one feature counting the tokens before the first entity.
	public static final String WORDEMBEDDINGBETWEENCONTEXT = "b";	//Context between entities as encoded via averaging the words' word2vec vectors.  Also one feature counting the tokens between the entities.
	public static final String WORDEMBEDDINGAFTERCONTEXT = "c";	//Context after second entity as encoded via averaging the words' word2vec vectors.  Also one feature counting the tokens after the last entity.
	//Note that it probably makes more sense to separate the context lengths into a separate feature type, if you need some busy work.
	
	public static final String SYNTACTICPARSETREEPATH = "d";	//String of syntactic parse tree node labels between entities.  Entities are represented by their last word.
	
	public static final String DEPENDENCYPARSETREEEDGEPATH = "e";	//String of dependency tree edge labels between entities.  Directions are included in the label (e.g. an nsubj edge that goes left to right is encoded like >nsubj, while an nsubj edge going the opposite direction is encoded like <nsubj.  Entities are represented by their last word.
	public static final String DEPENDENCYPARSETREENODEPATH = "f";	//String of dependency tree node lemmas between entities.  Entities are represented by their last word.
	public static final String DEPENDENCYPARSETREEEDGENODEPATH = "g";	//String of directed dependency tree edge labels and node lemmas between entities.  The string alternates between edges (grammatical relationships) and nodes (word lemmas).  Entities are represented by their last word.
	
	public static final String DEPENDENCYPARSETREENODECONTEXTS = "h";	//Collect the node lemmas in the dependency path between the cyber entities, then make an embedding feature from them just as with the word embedding context features.
	
	
	//A path is a sequence of items.  To generate these subpath features, we simply take the corresponding path feature from above
	//and build features out of all possible contiguous subsequences (of length up to 5) of items from the list.
	public static final String SYNTACTICPARSETREESUBPATHS = "i";
	
	public static final String DEPENDENCYPARSETREEEDGESUBPATHS = "j";	
	public static final String DEPENDENCYPARSETREENODESUBPATHS = "k";	
	public static final String DEPENDENCYPARSETREEEDGENODESUBPATHS = "l";	
	
	//Counts of entities of all types appearing in different parts of the text.  There is a separate count feature for each entity type.
	//Note that the entities participating in the candidate relationship are not included in any of these.
	public static final String ENTITYBEFORECOUNTS = "m";
	public static final String ENTITYBETWEENCOUNTS = "n";
	public static final String ENTITYAFTERCOUNTS = "o";
	
	//(lemmatized) word n-grams of length 1, 2, and 3 appearing before, between, and after the entities.  These are binary features rather than counts.  These are intended as baseline features.
	public static final String WORDNGRAMSBEFORE = "p";
	public static final String WORDNGRAMSBETWEEN = "q";
	public static final String WORDNGRAMSAFTER = "r";
	
	
	
	
	public static final String ALWAYSPREDICTPOSITIVECODE = "z";	//Warning.  This is not a code that ever gets used in a command line arguments.  It is merely used internally to determine which file to write positive prediction results to.
	
	
	
	
	public static final String FEATUREMAPPREFIX = "FeatureMap";
	
	
	private int featurecounter;
	private HashMap<Integer,String> index2feature;
	private HashMap<Integer,String> index2type;
	
	FeatureMap()
	{
		featurecounter = 1;
		index2feature = new HashMap<Integer,String>();
		index2type = new HashMap<Integer,String>();
	}
	
	FeatureMap(String entityextractedfilename, String featuretypes, int relationtype)
	{
		try
		{
			File featuremapfile = ProducedFileGetter.getFeatureMapFile(entityextractedfilename, featuretypes, relationtype);
		
			index2feature = new HashMap<Integer,String>();
			index2type = new HashMap<Integer,String>();
		
			BufferedReader in = new BufferedReader(new FileReader(featuremapfile));
			String line;
			while((line = in.readLine()) != null)
			{
				String[] indexAndtypecolonname = line.split("\t");
				String typecolonname = indexAndtypecolonname[1];
			
				int index = Integer.parseInt(indexAndtypecolonname[0]);
				String type = typecolonname.substring(0, typecolonname.indexOf(':'));
				//String type;
				//if(beforecolon.contains("."))
				//	type = beforecolon.substring(0, beforecolon.indexOf('.'));
				//else
				//	type = beforecolon;
				String name = typecolonname;
			
				index2feature.put(index, name);
				index2type.put(index, type);
			}
			in.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	public int getIndex(String featurename, String featuretype)
	{
		Integer result = get(featurename);
		if(result == null)
		{
			result = featurecounter++;
			put(featurename, result);
			index2feature.put(result, featurename);
			index2type.put(result, featuretype);
		}
		
		return result;
	}
	
	public String getFeature(int index)
	{
		return index2feature.get(index);
	}

	public String getCode(int index)
	{
		return index2type.get(index);
	}

	public HashMap<Integer,String> getIndex2Type()
	{
		return index2type;
	}
	
	public String getType(Integer index)
	{
		return index2type.get(index);
	}

	public static String getOrderedFeatureTypes(String featuretypes)
	{
		HashSet<String> featuretypeset = new HashSet<String>();
		for(int i = 0; i < featuretypes.length(); i++)
			featuretypeset.add(featuretypes.charAt(i) + "");
		ArrayList<String> featuretypelist = new ArrayList<String>(featuretypeset);
		Collections.sort(featuretypelist);
		
		String result = "";
		for(String featuretype : featuretypelist)
			result += featuretype;
		
		return result;
	}
	public static String getCommaSeparatedOrderedFeatureTypes(String featuretypes)
	{
		ArrayList<String> featuretypelist = new ArrayList<String>();
		String[] featuretypesarray = featuretypes.split(",");
		for(String featuretype : featuretypesarray)
			featuretypelist.add(featuretype);
		Collections.sort(featuretypelist);
		
		String result = "";
		for(String featuretype : featuretypelist)
			result += " " + featuretype;
		result = result.trim();
		result = result.replaceAll(" ", ",");
		
		return result;
	}

	//This method reads all the training instance files associated with a particular entityextracted type and each feature type
	//and maps each feature to a unique integer in a new FeatureMap.
	//Warning: It is very important that the same training instances are written to entityextracted when the feature map
	//is first constructed (in Run RelationSVMs
	public static FeatureMap constructFeatureMap(String entityextractedfilename, String featuretypes, int relationtype)
	{
		FeatureMap featuremap = new FeatureMap();
		
		for(int i = 0; i < featuretypes.length(); i++)
		{
			String featuretype = featuretypes.charAt(i) + "";
			File relationinstancesfile = ProducedFileGetter.getRelationshipSVMInstancesFile(entityextractedfilename, featuretype, relationtype);
			
			try
			{
				BufferedReader in = new BufferedReader(new FileReader(relationinstancesfile));
				String instanceline;
				while((instanceline = in.readLine()) != null)
				{
					if(PrintPreprocessedDocuments.isLineBetweenDocuments(instanceline) || PrintPreprocessedDocuments.isFileNameLine(instanceline))
						continue;
					
					
					String[] splitline = instanceline.split("#");
					String[] classAndfeatures = splitline[0].trim().split(" ");
					for(int j = 1; j < classAndfeatures.length; j++)
					{
						String featurenameAndvalue = classAndfeatures[j];
						String featurename = featurenameAndvalue.substring(0, featurenameAndvalue.lastIndexOf(':'));
						//double value = Double.parseDouble(featurenameAndvalue.substring(featurenameAndvalue.lastIndexOf(':')+1));
						featurename = featuretype + ":" + featurename;
						
						featuremap.getIndex(featurename, featuretype);	//Just by getting the index, we add the feature to the map.  So don't need to do anything with it.
					}
					
				}
				in.close();
			}catch(IOException e)
			{
				System.out.println(e);
				e.printStackTrace();
				System.exit(3);
			}
		}
		
		return featuremap;
	}

	
	public void writeAsFile(String entityextractedfilename, String featuretypes, int relationtype)
	{
		try
		{
			File f = ProducedFileGetter.getFeatureMapFile(entityextractedfilename, featuretypes, relationtype);
			PrintWriter out = new PrintWriter(new FileWriter(f));
		
			ArrayList<Integer> allfeatureids = new ArrayList<Integer>(values());
			Collections.sort(allfeatureids);
			for(Integer featureid : allfeatureids)
				out.println(featureid + "\t" + index2feature.get(featureid));
			out.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
}