package gov.ornl.stucco.relationprediction;

/*
 * The purpose of this program is to find all instances of all relationships we are interested
 * in using (from either the training or test data, depending on which command line arguments
 * are given), and write them to a file.  Then, when we write the instances to a file 
 * (in WriteRelationInstances), we can ensure that all features will write their instances
 * in the same order, which means we can read all of these instance files for all features
 * we are interested in at once when running RunRelationSVMs, rather than keeping some information
 * in memory and trying to align the files after the fact.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FindAndOrderAllInstances 
{

	
	public static void orderAllInstances(String preprocessType) 
	{
		if( !((preprocessType.equals("original") || preprocessType.equals("entityreplaced") || preprocessType.equals("aliasreplaced"))) )
		{
			System.err.println("ERROR: FindAndOrderAllInstances - invalid preprocessType - must be 'original', 'entityreplaced', or 'aliasreplaced'.");
			System.exit(3);
		}
		
		HashMap<Integer,PrintWriter> relationToprintwriter = initializePrintWriters(preprocessType);
		
		try
		{
			//We are switching to using zip files for these because they could potentially be very big.
			ZipFile zipfile = new ZipFile(ProducedFileGetter.getEETextResources(preprocessType));
			ZipEntry entry = zipfile.entries().nextElement();
		    BufferedReader in = new BufferedReader(new InputStreamReader(zipfile.getInputStream(entry)));

		    //We are switching to using zip files for these because they could potentially be very big.
			ZipFile zipfile2 = new ZipFile(ProducedFileGetter.getEETextResources("aliasreplaced"));
			ZipEntry entry2 = zipfile2.entries().nextElement();
		    BufferedReader aliasreplacedalignedin = new BufferedReader(new InputStreamReader(zipfile2.getInputStream(entry2)));
			
		    //We are switching to using zip files for these because they could potentially be very big.
			ZipFile zipfile3 = new ZipFile(ProducedFileGetter.getEETextResources("unlemmatized"));
			ZipEntry entry3 = zipfile3.entries().nextElement();
			BufferedReader unlemmatizedalignedin = new BufferedReader(new InputStreamReader(zipfile3.getInputStream(entry3)));
			
			
			//int linecounter = 0;	//Keep track of the number of the line we are on in the file.  We'll make a note of what line our instances come from in this way.
			int sentencecounter = 0;
			
			String currentfilename = null;
			
			//Each line in this file is a single sentence.  Relationships can only be extracted if both 
			//participating entities appear in the same sentence.  We do not do coreference resolution,
			//so each entity must be mentioned by name (it cannot be replaced with a pronoun).
			String line;

			while((line = in.readLine()) != null)
			{
				//Read the corresponding line from the alias replaced text file.
				String aliasedline = aliasreplacedalignedin.readLine();
				String unlemmatizedline = unlemmatizedalignedin.readLine();
				
				//linecounter++;
				
				
				//The text files contain a blank line between documents.  Just ignore them.
				if(line.length() == 0)
					continue;
				
				if(line.startsWith("###") && line.endsWith("###"))
				{
					currentfilename = line.substring(3, line.length()-3);
					sentencecounter = 0;
					continue;
				}
				
				
				//These are the indices of the tokens in the original document (before we 
				//replaced entities with representative tokens).
				int[] originalindices = PrintPreprocessedDocuments.getOriginalTokenIndexesFromPreprocessedLine(unlemmatizedline.split(" "));
				
	
				//Recall that, in the input file, a cyber entity is compressed into one token,
				//even though in the source text, it may have been several tokens long.
				String[] tokens = line.split(" ");
				
				
				//And keep track of the alias replaced tokens the normal tokens correspond to.
				String[] aliasreplacedtokens = aliasedline.split(" ");
				String[] unlemmatizedtokens = unlemmatizedline.split(" ");
				
				
				//The text file we are reading is already annotated with cyber entities.
				//Here, we are constructing an array of CyberEntityTexts parallel to 
				//the tokens array that tells us the token's Cyber entity label (if it has one).
				CyberEntity[] cyberentitytexts = new CyberEntity[tokens.length];
				CyberEntity[] aliasedcyberentitytexts = new CyberEntity[tokens.length];
				CyberEntity[] unlemmatizedcyberentitytexts = new CyberEntity[tokens.length];
				for(int i = 0; i < tokens.length; i++)
				{
					cyberentitytexts[i] = CyberEntity.getCyberEntityFromToken(tokens[i]);
					aliasedcyberentitytexts[i] = CyberEntity.getCyberEntityFromToken(aliasreplacedtokens[i]);
					unlemmatizedcyberentitytexts[i] = CyberEntity.getCyberEntityFromToken(unlemmatizedtokens[i]);
				}
				
				//Now, we scan through each pair of tokens.  If both are cyber entities...
				for(int i = 0; i < tokens.length; i++)
				{
					if(cyberentitytexts[i] != null)
					{
						for(int j = i+1; j < tokens.length; j++)
						{
							if(cyberentitytexts[j] != null)
							{
								//...we construct a relationship instance from them.
//								GenericCyberEntityTextRelationship relationship = new GenericCyberEntityTextRelationship(cyberentitytexts[i], cyberentitytexts[j]);
								GenericCyberEntityTextRelationship aliasedrelationship = new GenericCyberEntityTextRelationship(aliasedcyberentitytexts[i], aliasedcyberentitytexts[j]);
//								GenericCyberEntityTextRelationship unlemmatizedrelationship = new GenericCyberEntityTextRelationship(unlemmatizedcyberentitytexts[i], unlemmatizedcyberentitytexts[j]);
								
								
								//Any pair of entities of any types can be used to construct a relationship instance.
								//But we only care about a subset of all possible relationship types.  In particular, 
								//we care about the relationships ennumerated at the top of GenericCyberEntityTextRelationship
								//(or their reverses, where the entities appear in the reverse of the normal order
								//in the text).  So check if this entity pair's relationship type is one of the 
								//types we care about.  Since we only built print writers for the types we cared about 
								//earlier in the program, we check for this condition by checking if we constructed a 
								//PrintWriter for this relationship.
								Integer relationtype = aliasedrelationship.getRelationType();
								
							
								if(relationtype != null)
								{
									//If we do care about this relationship type, check whether
									//we can label this relationship confidently enough to
									//use it as a training instance.
									//(There is a comment above GenericCyberEntityTextRelationship.isKnownRelationship()
									//explaining our rules for making this determination.)
									//Use the aliased version of the relationship to check for this because
									//its contents are easiest to align with known entities.
									//Boolean isknownrelationship = relationship.isKnownRelationship();
									
									Boolean isknownrelationship = aliasedrelationship.isKnownRelationship();
									if(isknownrelationship != null)	//Do not bother to write instances with 0 labels.  isknownrelationship == null if the label would be 0 (we don't know the label).
									{
											InstanceID instanceid = new InstanceID(currentfilename, sentencecounter, i, originalindices[i], originalindices[i+1], sentencecounter, j, originalindices[j], originalindices[j+1]);
										
											int replacedfirsttokenindex = instanceid.getReplacedFirstTokenIndex();
											int replacedsecondtokenindex = instanceid.getReplacedSecondTokenIndex();
										
											//String[] firstsentence = filelines.get(firsttokensentencenum);
											String[] firstsentence = tokens;
											//String[] firsttokenunlemmatizedsentence = unlemmatizedfilelines.get(firsttokensentencenum);
											String[] firsttokenunlemmatizedsentence = unlemmatizedtokens;
											//String[] secondsentence = filelines.get(secondtokensentencenum);
											String[] secondsentence = tokens;
											//String[] secondtokenunlemmatizedsentence = unlemmatizedfilelines.get(secondtokensentencenum);
											String[] secondtokenunlemmatizedsentence = unlemmatizedtokens;
											
											String label;
											if(isknownrelationship)
												label = "1";
											else
												label = "-1";
											
											relationToprintwriter.get(relationtype).println(label + " # " + instanceid + " " + firsttokenunlemmatizedsentence[replacedfirsttokenindex] + " " + firstsentence[replacedfirsttokenindex] + " " + secondtokenunlemmatizedsentence[replacedsecondtokenindex] + " " + secondsentence[replacedsecondtokenindex] + " " + unlemmatizedline.trim());
									}
								}
							}
						}
					}
				}
				sentencecounter++;
			}
			in.close();
			aliasreplacedalignedin.close();
			unlemmatizedalignedin.close();
			zipfile.close();
			zipfile2.close();
			zipfile3.close();
			for(PrintWriter pw : relationToprintwriter.values())
				pw.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	//Arguments: 
	//1. extractedfilename (This is the name of the file written by PrintPreprocessedDocuments.  
	//Valid known values for this argument are "original", "entityreplaced", and "aliasreplaced")
	//2. training (optional).  If you include the word "training" in your command line arguments
	//after the first two (required) feature types, training files (based on the .ser.gz contents
	//of Training DataFiles directory) will be written.  Else testing files (based on the .ser.gz
	//contents of Testing DataFiles directory) will be written.
//	private static void readArgs(String[] args)
//	{
//		preprocessType = args[0];
//	}
	
	

	private static HashMap<Integer,PrintWriter> initializePrintWriters(String preprocessType)
	{
		HashMap<Integer,PrintWriter> relationtypeToprintwriter = new HashMap<Integer,PrintWriter>();
		
		try
		{
			for(Integer i : GenericCyberEntityTextRelationship.getAllRelationshipTypesSet())
			{
				File f = ProducedFileGetter.getRelationshipSVMInstancesOrderFile(preprocessType, i);
				relationtypeToprintwriter.put(i, new PrintWriter(new FileWriter(f)));
			}
		}catch(IOException e)
		{
			System.err.println(e);
			e.printStackTrace();
			System.exit(3);
		}
		
		return relationtypeToprintwriter;
	}
	
	
	public static String getCommentFromLine(String line)
	{
		return line.split(" # ")[1];
	}

}
