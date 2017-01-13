package gov.ornl.stucco.relationprediction;
//This class just parses the lines that get written to the prediction files by RunRelationSVMs.



import java.util.ArrayList;

public class RelationPrediction 
{	
	public static int TPindex = 0;
	public static int FPindex = 1;
	public static int TNindex = 2;
	public static int FNindex = 3;
	
	public static int positiveclasslabel = 1;
	public static int negativeclasslabel = -1;
	
	
	private String comment;
	private int goldclass;
	private int predictedclass;
	private double predictedpositiveprobability;	//We are not using this yet, but may need it if we add probability outputs to our SVM's predictions.
	
	
	
	
	RelationPrediction(String resultline)
	{
		String[] resultsAndcomment = resultline.split("#");
		if(resultsAndcomment.length == 2)
			comment = resultsAndcomment[1];
		
		String[] splitresults = resultsAndcomment[0].split(" ");
		
		String classandprediction = splitresults[0];
		String[] splitclassandprediction = classandprediction.split("/");
		goldclass = Integer.parseInt(splitclassandprediction[0]);		//Subtract 1 from gold and predicted class because they are 1-indexed, but arrays and such that deal with these classes (which we use elsewhere) are 0-indexed.
		predictedclass = Integer.parseInt(splitclassandprediction[1]);
		
		if(splitresults.length > 1)
			predictedpositiveprobability = Double.parseDouble(splitresults[1]);
	}
	
	public String getComment()
	{
		return comment;
	}
	
	public int getTPorFPorTNorFN()
	{
		//In the general case, we could figure this out for each class.  But since we are working with a two class problem, in practice we care only about the positive class.
		int forlabel = positiveclasslabel;
		
		if(forlabel == goldclass && forlabel == predictedclass)
			return TPindex;
		if(forlabel == goldclass && forlabel != predictedclass)
			return FNindex;
		if(forlabel != goldclass && forlabel == predictedclass)
			return FPindex;
		if(forlabel != goldclass && forlabel != predictedclass)
			return TNindex;
		
		
		System.out.println("Error: instance not classifiable.");
		new Exception().printStackTrace();
		System.exit(3);
		
		
		return -1;
	}
	
	//In the results, instances without a known class are labeled 0.  Positive and negative instances are labeled 1 and -1 respectively.
	public boolean instanceHasKnownClass()
	{
		return goldclass == positiveclasslabel || goldclass == negativeclasslabel;
	}

	public boolean getPredictionIsCorrect()
	{
		return goldclass == predictedclass;
	}
}
