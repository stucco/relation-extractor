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
import java.util.ArrayList;
import java.util.List;

import gov.ornl.stucco.entity.models.CyberEntityType;
import gov.ornl.stucco.model.CyberRelation;



public class PredictUsingExistingSVMModel
{
	
	
	public static List<CyberRelation> predictRelationship(String extractedfilename, String featureCodeList) throws IOException, InterruptedException
	{
		if( !((extractedfilename.equals("original") || extractedfilename.equals("entityreplaced") || extractedfilename.equals("aliasreplaced"))) )
		{
			System.err.println("ERROR: PredictUsingExistingSVMModel - invalid extractedfilename - must be 'original', 'entityreplaced', or 'aliasreplaced'.");
			System.exit(3);
		}
		
		featureCodeList = FeatureMap.getOrderedFeatureTypes(featureCodeList);
		
		//We need a bunch of temporary files to hold different stuff repeatedly.  Build them, and give them names with this process's id to avoid collisions with other instances of this program that might be running.
		String featureFilePath = ProducedFileGetter.getTemporaryFilePath("docFeatures");
//		InputStream featureFileStream = ProducedFileGetter.getTemporaryFileStream("docFeatures");
		String commentFilePath = ProducedFileGetter.getTemporaryFilePath("docComments");
		String predictionsFilePath = ProducedFileGetter.getTemporaryFilePath("docPrediction");
		
		
		String libsvmjarPath = ProducedFileGetter.getLibSVMJarPath();
		
		List<CyberRelation> relations = new ArrayList<CyberRelation>();
		
		for(int relationtypeIndex : GenericCyberEntityTextRelationship.allrelationshiptypesset)
		{

			//String context = bestparametersTovalues.get("featuretypes");
			String kerneltype = "Linear";	//bestparametersTovalues.get("kerneltype");
			double c = Double.parseDouble("0.01");	//Double.parseDouble(bestparametersTovalues.get("c"));
			double gamma = Double.parseDouble("1.0E-4");	//Double.parseDouble(bestparametersTovalues.get("gamma"));
			
			FeatureMap featuremap = new FeatureMap(extractedfilename, featureCodeList, relationtypeIndex);
			writeSVMFiles(featuremap, relationtypeIndex, extractedfilename, featureCodeList, (new File(featureFilePath)), (new File(commentFilePath)));
				
			String modelfilePath = ProducedFileGetter.getSVMModelFilePath(kerneltype, extractedfilename, featureCodeList, relationtypeIndex, null, null, c, gamma);	
						
			//Apply the model to test instances
			String[] t1array = {"java", "-cp",  libsvmjarPath, "svm_predict", featureFilePath, modelfilePath, predictionsFilePath};
			Process t1process = (new ProcessBuilder(t1array)).start();
			BufferedReader br = new BufferedReader(new InputStreamReader(t1process.getErrorStream()));
			String line;
			while ((line = br.readLine()) != null)
				System.out.println(line);
			t1process.waitFor();
			
			// Read predictions and comments in parallel to get corresponding prediction with entity pair
			File predictionsFileStream = ProducedFileGetter.getTemporaryFile("docPrediction");
			File commentFileStream = ProducedFileGetter.getTemporaryFile("docComments");
			BufferedReader fileReader = new BufferedReader(new FileReader(predictionsFileStream));
			String prediction = fileReader.readLine();
			BufferedReader commentReader = new BufferedReader(new FileReader(commentFileStream));
			String commentLine = commentReader.readLine();
			while ((prediction != null) && (commentLine != null)) {
//				if (prediction.equalsIgnoreCase("1.0")) {
					String[] commentArray = commentLine.split(" ");
					if (commentArray.length >= 5) {
						CyberEntityType entity1 = CyberEntity.getEntityTypeFromToken(commentArray[1]);
						String entityText1 = CyberEntity.getEntitySpacedText(commentArray[1]);
						CyberEntityType entity2 = CyberEntity.getEntityTypeFromToken(commentArray[3]);
						String entityText2 = CyberEntity.getEntitySpacedText(commentArray[3]);
						if ((entity1 != null) && (entityText1 != null) && (entity2 != null) && (entityText2 != null) && (GenericCyberEntityTextRelationship.relationshipidTorelationshipname.containsKey(Integer.valueOf(relationtypeIndex)))) {
							String edgeType = GenericCyberEntityTextRelationship.relationshipidTorelationshipname.get(Integer.valueOf(relationtypeIndex));
							CyberRelation newRelation = new CyberRelation(entity1, entityText1, entity2, entityText2, edgeType);
							relations.add(newRelation);
						}
					}
//				}
				prediction = fileReader.readLine();
				commentLine = commentReader.readLine();
			}
			commentReader.close();
			fileReader.close();
		}

		//Delete all our temporary files.
		(new File(featureFilePath)).delete();
		(new File(predictionsFilePath)).delete();
		(new File(commentFilePath)).delete();
		
		return relations;
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
