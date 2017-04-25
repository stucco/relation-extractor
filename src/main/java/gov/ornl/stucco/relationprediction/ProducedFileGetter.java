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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;


public class ProducedFileGetter 
{
	
	//The file containing a vector for each word in our vocabulary.  This file is written by the Python program TrainModel.py.
	//trainingtype is one of the three entity extracted text types mentioned above getEntityExtractedText(String filename).
	public static BZip2CompressorInputStream getWordVectorsStream(String trainingtype) {
		InputStream inputStream = ProducedFileGetter.class.getClassLoader().getResourceAsStream("word2vecModels/wordvectors." + trainingtype + ".bz2");
		BZip2CompressorInputStream word2VecStream = null;
		try {
			word2VecStream = new BZip2CompressorInputStream(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return word2VecStream;
	}
	
	//Known possible filenames are "original", "entityreplaced", "aliasreplaced", and "unlemmatized"
	public static File getEETextResources(String filename)
	{
		File eeTextFile = null;
		try {
			URL eeTextDir = ProducedFileGetter.class.getClassLoader().getResource("entityExtractedText/");
			URI eeTextURI = eeTextDir.toURI();
			eeTextFile = new File(eeTextURI.getPath() + filename + ".zip");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return eeTextFile;
	}

	
	//SVM training files
	//entityextractedfilename is the name of the text file that has entities replaced with tokens.  
	//getEntityExtractedText(String filename) will previously have been used to get this file for writing.
	//feature is a 1-letter code indicating the feature type.
	public static File getRelationshipSVMInstancesFile(String entityextractedfilename, String feature, int relationshiptype)
	{
		File instancesFile = null;
		try {
			URL instanceDir = ProducedFileGetter.class.getClassLoader().getResource("instanceFiles/");
			URI instancesURI = instanceDir.toURI();
			instancesFile = new File(instancesURI.getPath() + "RelationInstances." + entityextractedfilename + "." + feature + "." + relationshiptype);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return instancesFile;
	}

	
	//In order to keep all instances in the same order in their instance files (so that the files
	//for individual features will correspond well enough that we do not have to keep much in 
	//memory), make a file that tells the order instances must be listed in.
	public static File getRelationshipSVMInstancesOrderFile(String entityextractedfilename, int relationshiptype)
	{
		File instancesFile = null;
		try {
			URL instanceDir = ProducedFileGetter.class.getClassLoader().getResource("instanceFiles/");
			URI instancesURI = instanceDir.toURI();
			instancesFile = new File(instancesURI.getPath() + "RelationInstancesOrder." + entityextractedfilename + "." + relationshiptype);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return instancesFile;
	}
		
	
	//This file contains predictions made by an SVM.
	public static File getPredictionsFile(String entityextractedfilename, String featuretypes, int relationshiptype)
	{
		File predictionsFile = null;
		try {
			URL predictionsDir = ProducedFileGetter.class.getClassLoader().getResource("predictionsFiles/");
			URI predictionDirURI = predictionsDir.toURI();
			predictionsFile = new File(predictionDirURI.getPath() + "Predictions." + entityextractedfilename + "." + featuretypes + "." + relationshiptype);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return predictionsFile;
	}

	
	public static String getSVMModelFilePath(String kerneltype, String entityextractedfilename, String contexts, int relationshiptype, Integer excludedfold1, Integer excludedfold2, double c, double gamma)
	{
		//Replace the decimals in c and gamma with commas because periods are used as separators in the file name.
		String cstring = ("" + c).replaceAll("\\.", ",");
		String gammastring = ("" + gamma).replaceAll("\\.", ",");
				
		String filename = "SVMModel." + kerneltype + "." + entityextractedfilename + "." + contexts + "." + relationshiptype + "." + excludedfold1 + "." + excludedfold2 + "." + cstring + "." + gammastring;
				
		String predictionsFilePath = null;
		try {
			URL predictions = ProducedFileGetter.class.getClassLoader().getResource("svmModelFiles/" + filename);
			URI predictionDirURI = predictions.toURI();
			predictionsFilePath = predictionDirURI.getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return predictionsFilePath;
	}

	
	public static String getLibSVMJarPath()
	{
		String path = "lib/libsvm.jar";
		URL libsvmURL = ProducedFileGetter.class.getClassLoader().getResource("lib/libsvm.jar");
		try {
			path = libsvmURL.toURI().getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return path;
	}
	
	
	public static File getNVDDir() {
		File dir = null;
		try {
			URL nvdDir = ProducedFileGetter.class.getClassLoader().getResource("nvdxml/");
			if (nvdDir != null) {
				dir = new File(nvdDir.toURI().getPath());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return dir;
	}
	
	
	public static String getTemporaryFilePath(String tempfilename)
	{
		String tempPath = null;
		File tempFile = getTemporaryFile(tempfilename);
		if (tempFile != null) {
			tempPath = tempFile.getAbsolutePath();
		}
		return tempPath;
	}
	
	
	public static File getTemporaryFile(String tempfilename)
	{
		File newTempFile = null;
		try {
			URL tempStream = ProducedFileGetter.class.getClassLoader().getResource("temp/");
			String tempDirPath = tempStream.toURI().getPath();
			newTempFile = new File(tempDirPath + tempfilename);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return newTempFile;
	}

	
	public static File getFeatureMapFile(String entityextractedfilename, String featuretypes, int relationtype) {
		File featureFile = null;
		try {
			URL featureDir = ProducedFileGetter.class.getClassLoader().getResource("featureMapFiles/");
			URI featureDirURI = featureDir.toURI();
			featureFile = new File(featureDirURI.getPath() + "FeatureMap." + entityextractedfilename + "." + featuretypes + "." + relationtype);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return featureFile;
	}
	
	
	//Purely for testing
	public static void main(String[] args)
	{
		System.out.println(getLibSVMJarPath());
		
		System.out.println(getEETextResources("aliasreplaced").getAbsolutePath());
	}
}
