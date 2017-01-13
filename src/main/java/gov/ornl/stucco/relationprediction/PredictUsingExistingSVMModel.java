package gov.ornl.stucco.relationprediction;
//This program takes the instance data written by WriteRelationInstanceFiles and applies an SVM model to it that was written 
//during training. It chooses which parameters to use based on the output of CalculateResults, which gets run during the 
//training phase. This program will not run properly unless WriteRelationInstanceFiles has been run using the same 
//preprocessedtype parameter. And of course CalculateResults needs to be run with the same preprocessedtype parameter 
//during training as well.
//
//See the readme file in the github repository for a more complete description.


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.HashMap;



public class PredictUsingExistingSVMModel
{
	//This program is very expensive to run, so if this variable gets turned on, we will run it on only a subset of the available data to save time.  This should be good enough to let us know if the program is working alright.
	private static boolean testingprogram = false;
	
	
	private static String pid;	//In the even that we have multiple instances of this program running, we will use each process's pid to ensure that their temporary files do not interfere with eachother.
	static
	{
		pid = ManagementFactory.getRuntimeMXBean().getName();
//		pid = pid.replaceAll("@", "");
		pid = pid.substring(0, pid.indexOf('@'));
	}
	
	private static String entityextractedfilename;
	private static String featuretypes;
	
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
		readArgs(args);
		
		
		//We need a bunch of temporary files to hold different stuff repeatedly.  Build them, and give them names with this process's id to avoid collisions with other instances of this program that might be running.
		File testfile1 = ProducedFileGetter.getTemporaryFile("TE1." + pid);
		File testfile1comments = ProducedFileGetter.getTemporaryFile("TEC1." + pid);
		File testpredictionsfile1 = ProducedFileGetter.getTemporaryFile("PR1." + pid);
		
		
		File libsvmjar = ProducedFileGetter.getLibSVMJarFile();
		
		
		//Train a classifier for each relationship type.
		for(int positiverelationtype : GenericCyberEntityTextRelationship.allpositiverelationshiptypesset)
		{
			//For testing, we will only pay attention to one relationship type, as this program is about 50 times as expensive to run for all relationship types.
//			if(testingprogram && positiverelationtype != 1)
//				continue;
	
			
//			int[] relationandreverse = {positiverelationtype, -positiverelationtype};
//			for(int relationtype : relationandreverse)
//			{
				ParametersLine bestparameters = getBestParameters(entityextractedfilename, featuretypes, positiverelationtype);
				
//				HashMap<String,String> bestparametersTovalues = bestparameters.getParametersTovalues();
				//String context = bestparametersTovalues.get("featuretypes");
				String kerneltype = "Linear";	//bestparametersTovalues.get("kerneltype");
				double c = Double.parseDouble("0.01");	//Double.parseDouble(bestparametersTovalues.get("c"));
				double gamma = Double.parseDouble("1.0E-4");	//Double.parseDouble(bestparametersTovalues.get("gamma"));
			
//				PrintWriter testresultsout = new PrintWriter(new FileWriter(ProducedFileGetter.getPredictionsFile(entityextractedfilename, featuretypes, positiverelationtype, false)));
			
				
				FeatureMap featuremap = new FeatureMap(entityextractedfilename, featuretypes, positiverelationtype);
				writeSVMFiles(featuremap, positiverelationtype, entityextractedfilename, featuretypes, testfile1, testfile1comments);
						
					
				File modelfile = ProducedFileGetter.getSVMModelFile(kerneltype, entityextractedfilename, featuretypes, positiverelationtype, null, null, c, gamma);
							
							
				//Apply the model to test instances
				String[] t1array = {"java", "-cp",  libsvmjar.getAbsolutePath(), "svm_predict", testfile1.getAbsolutePath(), modelfile.getAbsolutePath(), testpredictionsfile1.getAbsolutePath()};
				Process t1process = (new ProcessBuilder(t1array)).start();
				BufferedReader br = new BufferedReader(new InputStreamReader(t1process.getErrorStream()));
				String line;
				while ((line = br.readLine()) != null)
					System.out.println(line);
				t1process.waitFor();
				
				
				//And print the resulting classified instances to a result file.
//				RunRelationSVMs.printResultsFile("null-" + RunRelationSVMs.getFoldSplitString(null, null) + " " + "kerneltype=" + kerneltype + " " + "c=" + c + " " + "gamma=" + gamma + " " + "featuretypes=" + featuretypes, testresultsout, testfile1, testfile1comments, testpredictionsfile1, true);
					
				
//				testresultsout.close();
//			}
		}
		

		//Delete all our temporary files.
		testfile1.delete();
		testpredictionsfile1.delete();
		testfile1comments.delete();
	}
	
	//Arguments: 
	//1. extractedfilename (This is the name of the file written by PrintPreprocessedDocuments.  
	//Valid known values for this argument are "original", "entityreplaced", and "aliasreplaced")
	private static void readArgs(String[] args)
	{
		entityextractedfilename = args[0];
		if( !(entityextractedfilename.equals("original") || entityextractedfilename.equals("entityreplaced") || entityextractedfilename.equals("aliasreplaced")) )
		{
			System.err.println("Error, invalid entityextractedfilename.  Entityextractedfilename must be original, entityreplaced, or aliasreplaced.");
			System.exit(3);
		}
		
		featuretypes = FeatureMap.getOrderedFeatureTypes(args[1]);
	}
	
	
	private static void writeSVMFiles(FeatureMap featuremap, int relationtype, String entityextractedfilename, String featuretypes, File testfile1, File testfile1comments) 
	{
		try
		{
			String[] featuretypesarray = new String[featuretypes.length()];
			BufferedReader[] inarray = new BufferedReader[featuretypes.length()];
			for(int featureindex = 0; featureindex < featuretypes.length(); featureindex++)
			{
				String featuretype = featuretypes.charAt(featureindex) + "";
				featuretypesarray[featureindex] = featuretype;
				
				File relationinstancesfile = ProducedFileGetter.getRelationshipSVMInstancesFile(entityextractedfilename, featuretype, relationtype);
				inarray[featureindex] = new BufferedReader(new FileReader(relationinstancesfile));
			}
			
			PrintWriter testout1 = new PrintWriter(new FileWriter(testfile1));
			PrintWriter testout1comments = new PrintWriter(new FileWriter(testfile1comments));
			
			outerloop:
			while(true)
			{
				String comment = null;
				String label = null;
				String featuresAndvaluesline = "";
				
				for(int featureindex = 0; featureindex < featuretypes.length(); featureindex++)
				{
					String line = inarray[featureindex].readLine();
					if(line == null)
						break outerloop;
					
					//String[] instanceAndcomments = line.split("#");
					//String instance = line.substring(0, line.indexOf('#'));
					//comment = line.substring(line.indexOf('#')+1);
					String[] instanceAndcomments = line.split(" # ");
					String instance = instanceAndcomments[0];
					comment = instanceAndcomments[1];
					
					//comment = instanceAndcomments[1];
					
					//String[] labelAndfeatures = instanceAndcomments[0].split(" ");
					String[] labelAndfeatures = instance.split(" ");
					label = labelAndfeatures[0];
					
					String featuretype = featuretypes.charAt(featureindex) + "";
					for(int i = 1; i < labelAndfeatures.length; i++)
					{
						String featurename = featuretype + ":" + labelAndfeatures[i].substring(0, labelAndfeatures[i].lastIndexOf(':'));
						int featureid = featuremap.getIndex(featurename, featuretype);
						String featurevalue = labelAndfeatures[i].substring(labelAndfeatures[i].lastIndexOf(':')+1);
					
						featuresAndvaluesline += " " + featureid + ":" + featurevalue;
					}
				}
				String instanceline = label + featuresAndvaluesline;
				
		
				testout1.println(instanceline);
				testout1comments.println(comment);
			}
			
			for(BufferedReader in : inarray)
				in.close();
			testout1.close();
			testout1comments.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	
	public static ParametersLine getBestParameters(String entityextractedfilename, String featuretypes, int relationshiptype)
	{
		ParametersLine result = null;
		
//		try
//		{
//			File f = ProducedFileGetter.getResultsFile(entityextractedfilename, featuretypes, Math.abs(relationshiptype));
//			BufferedReader in = new BufferedReader(new FileReader(f));
//			String lastline = in.readLine();
//			String line;
//			while((line = in.readLine()) != null)
//				lastline = line;
//			in.close();
			
			//relationshiptype + "\t" + formatter.format(fscore) + "\t" + formatter.format(precision) + "\t" + formatter.format(recall) + "\t" + bestnormalparametersline + "\t" + bestreverseparametersline;
//			String[] splitline = lastline.split("\t");
			String normalparametersstring = "Linear";	//splitline[splitline.length-2];
			String reverseparametersstring = "Linear"; //splitline[splitline.length-1];
			
			if(relationshiptype > 0)
				result = new ParametersLine(normalparametersstring, true);
			else if(relationshiptype < 0)
				result = new ParametersLine(reverseparametersstring, true);
//		}catch(IOException e)
//		{
//			System.out.println(e);
//			e.printStackTrace();
//			System.exit(3);
//		}
		
		return result;
	}
	
	
}
