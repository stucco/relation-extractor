/*
 * This program looks at all the .ser.gz files in the directory ProducedFileGetter.getEntityExtractedSerializedDirectory()
 * and produces several text files from them with one sentence per line.  The text files use the .ser.gz file supplied
 * tokenization, lemmatization, and entity annotations to produce three text files.  
 * One contains a tokenized, lemmatized
 * version of the text wherein the entities have been replaced with a token representing their predicted entity types.  
 * Another contains a tokenized, lemmatized version wherein the entities have been replaced with a token that includes information about
 * their predicted type and their original text. 
 * The third contains a tokenized, lemmatized version wherein we try to match the entity-labeled text segments with their canonical,
 * Freebase names by matching the entity text against Freebase aliases.  The entities are replaced with a token indicating 
 * their predicted entity type and canonical name.
 * There is one final file that is tokenized but not lemmatized called unlemmatized.  It is used solely because we might sometimes
 * want to know what the original text looked like.
 */

package gov.ornl.stucco.relationprediction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import gov.ornl.stucco.entity.EntityLabeler;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberAnnotation;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberConfidenceAnnotation;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberLabelsAnnotation;
import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator;
import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator.CyberHeuristicAnnotation;
import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator.CyberHeuristicMethodAnnotation;
import gov.ornl.stucco.entity.models.CyberEntityType;

public class PrintPreprocessedDocuments 
{
	//Any token having this high a probability of belonging to any non-O entity type will be assumed to belong to that entity type,
	//unless some other non-O entity type has a higher probability.  Otherwise, the token will be assigned to the type
	//having the highest predicted probability.
	public static double arbitraryprobabilitythreshold = 0.3; 
	
	
	public static void preprocessDocs(Annotation doc, String source, String title) throws IOException {
		//TODO Modify this to append annotations to Annotation document's tokens (i.e. aliasSubAnnotation, replacedNameAnnotation, etc)	
		
		//Open printwriters for each of the three text file types mentioned at the beginning of this .java file.  We get all
		//files from ProducedFileGetter to help maintain consistency between locations used by different programs.

		//Since there may be issues with sharing files as big as these, we are zipping them instead of just writing them in plain text format.
		ZipOutputStream aliassubstitutednamesout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(ProducedFileGetter.getEntityExtractedText("aliasreplaced"))));
		aliassubstitutednamesout.putNextEntry(new ZipEntry("data"));

		ZipOutputStream completelyreplacednamesout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(ProducedFileGetter.getEntityExtractedText("entityreplaced"))));
		completelyreplacednamesout.putNextEntry(new ZipEntry("data"));

		ZipOutputStream originalnamesout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(ProducedFileGetter.getEntityExtractedText("original"))));
		originalnamesout.putNextEntry(new ZipEntry("data"));

		ZipOutputStream unlemmatizednamesout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(ProducedFileGetter.getEntityExtractedText("unlemmatized"))));
		unlemmatizednamesout.putNextEntry(new ZipEntry("data"));
		
		StringBuilder filenameBuilder = new StringBuilder();
		filenameBuilder.append(source);
		filenameBuilder.append("__");
		filenameBuilder.append(title);
		String filename = filenameBuilder.toString().replaceAll(" ", "_");
		
		aliassubstitutednamesout.write( ("###" + filename + "###\n").getBytes());
		completelyreplacednamesout.write( ("###" + filename + "###\n").getBytes());
		originalnamesout.write( ("###" + filename + "###\n").getBytes());
		unlemmatizednamesout.write( ("###" + filename + "###\n").getBytes());
		
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for (int sentencenum = 0; sentencenum < sentences.size(); sentencenum++) 
		{
			CoreMap sentence = sentences.get(sentencenum);
			
			List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
			
			CyberEntityType currententitystate = CyberHeuristicAnnotator.O;
			int indexoffirsttokenhavingcurrentstate = 0;
		 	for (int i = 0; i < labels.size(); i++) 
		 	{
		 		CoreLabel token = labels.get(i);
		 		CyberEntityType entityfinaltype = null;
		 		
		 		
		 		//If the token was labeled by the heuristic annotator or was labeled with a high enough confidence
		 		//(see the note above arbitraryprobabilitythreshold for details), assign it the appropriate label.
		 		if (token.containsKey(CyberHeuristicMethodAnnotation.class))
		 		{
		 			entityfinaltype = token.get(CyberHeuristicAnnotation.class);
		 		}
		 		else if(token.containsKey(CyberAnnotation.class)) 
		 		{
		 			entityfinaltype = token.get(CyberAnnotation.class);
		 			
		 			if ((entityfinaltype.equals(CyberHeuristicAnnotator.O)) && (token.containsKey(CyberConfidenceAnnotation.class))) {
		 				double[] probabilities = token.get(CyberConfidenceAnnotation.class);
		 				String[] probLabels = doc.get(CyberLabelsAnnotation.class);
		 				double secondHighestProb = 0;
		 				int secProbIndex = 0;
		 				int maxProbIndex = 0;
		 				for (int j=0; j<probLabels.length; j++) {
		 					if (probLabels[j].equalsIgnoreCase(entityfinaltype.toString())) {
		 						maxProbIndex = j;
		 						break;
		 					}
		 				}
		 				for (int j=0; j<probabilities.length; j++) {
		 					if ((j != maxProbIndex) && (secondHighestProb < probabilities[j])) {
		 						secondHighestProb = probabilities[j];
		 						secProbIndex = j;
		 					}
		 				}
		 				if (secondHighestProb >= arbitraryprobabilitythreshold) {
		 					String secondType = probLabels[secProbIndex];
		 					if (secondType.contains(".")) {
								int index = secondType.indexOf(".");
								String type = secondType.substring(0, index);
								String subType = secondType.substring(index + 1);
								entityfinaltype = new CyberEntityType(type, subType);
							}
		 				}
		 			}
		 		}
		 		
		 		
		 		//Sometimes, due to the probability threshold I set, the automatic entity labeler makes dumb, correctable decisions. 
		 		//This method heuristically corrects some of these errors.
		 		entityfinaltype = resetEntityFinalTypeHeuristically(entityfinaltype, token);
		 		
		 		
		 		//If the current token has a different label than the previous token, write the last entity out.
		 		if(!entityfinaltype.equals(currententitystate))
		 		{
			 		if(!currententitystate.equals(CyberHeuristicAnnotator.O))
			 		{
			 			String entitytypestring = currententitystate.toString();
			 			
			 			//Below are the places where we write the three different versions of the text file.
			 			//Print the "original" version of the text (described above)
			 			String originalentitystring = "";
			 			String aliasreplacedentitystring = "";
			 			for(int j = indexoffirsttokenhavingcurrentstate; j < i; j++)
			 			{
			 				String word = labels.get(j).get(TextAnnotation.class);
			 				word = replaceWordSpecialCharacters(word);
			 				originalentitystring += " " + word;
			 				aliasreplacedentitystring += " " + word;
			 			}
			 			originalentitystring = originalentitystring.trim();
			 			originalentitystring = "[" + entitytypestring + "_" + originalentitystring.replaceAll(" ", "_") + "]";
			 			originalnamesout.write((originalentitystring + " ").getBytes());
			 			
			 			unlemmatizednamesout.write((originalentitystring + " ").getBytes());
			 			
			 			aliasreplacedentitystring = aliasreplacedentitystring.trim();
			 			aliasreplacedentitystring = CyberEntity.getCanonicalName(aliasreplacedentitystring, currententitystate);
			 			aliasreplacedentitystring = "[" + entitytypestring + "_" + aliasreplacedentitystring.replaceAll(" ", "_") + "]";
			 			aliassubstitutednamesout.write((aliasreplacedentitystring + " ").getBytes());
			 			
			 			//Print the alias replaced version of the text as described above.
			 			
			 			for(int j = indexoffirsttokenhavingcurrentstate; j < i; j++)
			 			{
			 				String lemma = labels.get(j).get(LemmaAnnotation.class);
			 				lemma = replaceWordSpecialCharacters(lemma).toLowerCase();
			 			}
			 			
			 			//Replace the entity's text with its type.
			 			completelyreplacednamesout.write(("[" + entitytypestring + "] ").getBytes());
			 		}
		 			
		 			currententitystate = entityfinaltype;
		 			indexoffirsttokenhavingcurrentstate = i;
		 		}
		 		
		 		//If the current token was not labeled as any kind of cyber entity, just print it ('s lemma) out.
		 		if(entityfinaltype.equals(CyberHeuristicAnnotator.O))
		 		{
		 			aliassubstitutednamesout.write( (((String)labels.get(i).get(LemmaAnnotation.class)).toLowerCase() + " ").getBytes());
		 			completelyreplacednamesout.write( (((String)labels.get(i).get(LemmaAnnotation.class)).toLowerCase() + " ").getBytes());
		 			originalnamesout.write( (((String)labels.get(i).get(LemmaAnnotation.class)).toLowerCase() + " ").getBytes());
		 			unlemmatizednamesout.write( (((String)labels.get(i).get(TextAnnotation.class)) + " ").getBytes());
		 		}
		 	}
		 	
		 	//If the sentence ends on an entity, print the entity.  This code does the same things as in the above loop,
		 	//only the loop does not process an entity if it comes at the end of a sentence.  So handle that situation.
	 		if(!currententitystate.equals(CyberHeuristicAnnotator.O))
	 		{
	 			String entitytypestring = currententitystate.toString();
	 			
	 			//Only adjust the entity's formatting.
	 			String originalentitystring = "";
	 			String aliasreplacedentitystring = "";
	 			for(int j = indexoffirsttokenhavingcurrentstate; j < labels.size(); j++)
	 			{
	 				String word = labels.get(j).get(TextAnnotation.class);
	 				word = replaceWordSpecialCharacters(word);
	 				originalentitystring += " " + word;
	 				aliasreplacedentitystring += " " + word;
	 			}
	 			originalentitystring = originalentitystring.trim();
	 			originalentitystring = "[" + entitytypestring + "_" + originalentitystring.replaceAll(" ", "_") + "]";
	 			originalnamesout.write((originalentitystring + " ").getBytes());

	 			unlemmatizednamesout.write((originalentitystring + " ").getBytes());
	 			
	 			aliasreplacedentitystring = aliasreplacedentitystring.trim();
	 			aliasreplacedentitystring = CyberEntity.getCanonicalName(aliasreplacedentitystring, currententitystate);
	 			aliasreplacedentitystring = "[" + entitytypestring + "_" + aliasreplacedentitystring.replaceAll(" ", "_") + "]";
	 			aliassubstitutednamesout.write((aliasreplacedentitystring + " ").getBytes());
	 			
	 			//Replace the entity with its main alias if available.
	 			
	 			for(int j = indexoffirsttokenhavingcurrentstate; j < labels.size(); j++)
	 			{
	 				String lemma = labels.get(j).get(LemmaAnnotation.class);
	 				lemma = replaceWordSpecialCharacters(lemma).toLowerCase();
	 			}
	 				 			
	 			//Completely replace the entity with the name of its type.
	 			completelyreplacednamesout.write(("[" + entitytypestring + "] ").getBytes());
	 		}
	 		
	 		aliassubstitutednamesout.write("\n".getBytes());
	 		completelyreplacednamesout.write("\n".getBytes());
	 		originalnamesout.write("\n".getBytes());
	 		unlemmatizednamesout.write("\n".getBytes());
		}
		
		aliassubstitutednamesout.write("\n".getBytes());
		completelyreplacednamesout.write("\n".getBytes());
		originalnamesout.write("\n".getBytes());
		unlemmatizednamesout.write("\n".getBytes());

		aliassubstitutednamesout.closeEntry();
		aliassubstitutednamesout.close();
		completelyreplacednamesout.closeEntry();
		completelyreplacednamesout.close();
		originalnamesout.closeEntry();
		originalnamesout.close();
		unlemmatizednamesout.closeEntry();
		unlemmatizednamesout.close();
	}

	
	private static CyberEntityType resetEntityFinalTypeHeuristically(CyberEntityType predictedtype, CoreLabel token)
	{
		CyberEntityType result = predictedtype;
		
		if(predictedtype.equals(CyberHeuristicAnnotator.SW_VERSION))
		{
			String lemma = token.get(LemmaAnnotation.class);
			if(lemma.equals(".") || lemma.equals("...") || lemma.equals("version"))
				result = CyberHeuristicAnnotator.O;
		}
		
		return result;
	}
	
	private static String replaceWordSpecialCharacters(String word)
	{
		word = word.replaceAll("_", "~");
		word = word.replaceAll("\\[", "(");
		word = word.replaceAll("\\]", ")");
		
		return word;
	}
	
	
	public static int[] getOriginalTokenIndexesFromPreprocessedLine(String[] preprocessedline)
	{
		int[] result = new int[preprocessedline.length+1];
		
		int positioncounter = 0;
		for(int i = 0; i < preprocessedline.length; i++)
		{
			result[i] = positioncounter;
			
			String originaltext = CyberEntity.getEntitySpacedText(preprocessedline[i]);
			String[] originaltokens = originaltext.split(" ");
			
			positioncounter += originaltokens.length;
		}
		result[preprocessedline.length] = positioncounter;
		
		return result;
	}
	
	
	public static boolean isFileNameLine(String line)
	{
		return line.startsWith("###") && line.endsWith("###");
	}
	
	
	public static boolean isLineBetweenDocuments(String line)
	{
		return line.equals("");
	}

}
