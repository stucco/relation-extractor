/*
 * The purpose of this class is to be a central place for getting the path to a desired file given
 * information about the file.  I want a central class for this purpose because this Java project
 * needs to share some files with a related Python project, and I thought it would be easiest to
 * keep the file names consistent between the two projects if the Java portion retrieved all file
 * locations from the same class.
 * 
 * It is very important to note that these methods assume that ProducedFileGetter's class file
 * must be located somewhere within a directory called "relation-bootstrap".  It can be nested
 * arbitrarily deeply, or even packaged in a jar file as long as it is there.  See the static 
 * block near the top where I set the project directories for how I do this.
 */

package gov.ornl.stucco.relationprediction;

import java.io.File;
import java.net.URISyntaxException;


public class ProducedFileGetter 
{
	
	private static int tempfilecounter = 0;
	
	
	//The file containing a vector for each word in our vocabulary.  This file is written by the Python program TrainModel.py.
	//trainingtype is one of the three entity extracted text types mentioned above getEntityExtractedText(String filename).
	public static File getWordVectorsFile(String trainingtype)
	{
		File dir = new File("src/main/resources/word2vecModels/");
		dir.mkdirs();
		
		return new File(dir, "wordvectors." + trainingtype);
	}
	
	//Known possible filenames are "original", "entityreplaced", "aliasreplaced", and "unlemmatized"
	public static File getEntityExtractedText(String filename)
	{
		File dir = new File("entityExtractedText/");
		dir.mkdirs();
		
		return new File(dir, filename + ".zip");
	}
	
	//SVM training files
	//entityextractedfilename is the name of the text file that has entities replaced with tokens.  
	//getEntityExtractedText(String filename) will previously have been used to get this file for writing.
	//feature is a 1-letter code indicating the feature type.
	public static File getRelationshipSVMInstancesFile(String entityextractedfilename, String feature, int relationshiptype)
	{
		File dir = new File("instanceFiles/");
		dir.mkdirs();
		
		String filename = "RelationInstances." + entityextractedfilename + "." + feature + "." + relationshiptype;
		
		return new File(dir, filename);
	}
	
	//In order to keep all instances in the same order in their instance files (so that the files
	//for individual features will correspond well enough that we do not have to keep much in 
	//memory), make a file that tells the order instances must be listed in.
	public static File getRelationshipSVMInstancesOrderFile(String entityextractedfilename, int relationshiptype)
	{
		File dir = new File("instanceFiles/");
		dir.mkdirs();
		
		String filename = "RelationInstancesOrder." + entityextractedfilename + "." + relationshiptype;
		
		return new File(dir, filename);
	}
	
	
	//This file contains predictions made by an SVM.
	public static File getPredictionsFile(String entityextractedfilename, String featuretypes, int relationshiptype)
	{
		File dir = new File("predictionsFiles/");
		dir.mkdirs();
		
		String filename = "Predictions." + entityextractedfilename + "." + featuretypes + "." + relationshiptype;
		
		return new File(dir, filename);
	}

	
	public static File getSVMModelFile(String kerneltype, String entityextractedfilename, String contexts, int relationshiptype, Integer excludedfold1, Integer excludedfold2, double c, double gamma)
	{
		File dir = new File("src/main/resources/svmModelFiles/");
		dir.mkdirs();
		
		//Replace the decimals in c and gamma with commas because periods are used as separators in the file name.
		String cstring = ("" + c).replaceAll("\\.", ",");
		String gammastring = ("" + gamma).replaceAll("\\.", ",");
		
		String filename = "SVMModel." + kerneltype + "." + entityextractedfilename + "." + contexts + "." + relationshiptype + "." + excludedfold1 + "." + excludedfold2 + "." + cstring + "." + gammastring;
		
		return new File(dir, filename);
	}

	
	public static File getLibSVMJarFile()
	{
		return new File("src/main/resources/lib/libsvm.jar");
	}
	
	
	public static File getNVDXMLDir()
	{
		File result = new File("src/main/resources/nvdxml/");
		
		return result;
	}
	
	
	public static File getTemporaryFile(String tempfilename)
	{
		File result = new File("temp/" + tempfilename + "." + tempfilecounter++);
		result.getParentFile().mkdirs();
		
		return result;
	}
	
	
	public static File getFeatureMapFile(String entityextractedfilename, String featuretypes, int relationtype)
	{
		File dir = new File("featureMapFiles/");
		dir.mkdirs();
		
		String filename = "FeatureMap." + entityextractedfilename + "." + featuretypes + "." + relationtype;
		
		return new File(dir, filename);
	}
	
	
	//Purely for testing
	public static void main(String[] args)
	{
		
	}
}
