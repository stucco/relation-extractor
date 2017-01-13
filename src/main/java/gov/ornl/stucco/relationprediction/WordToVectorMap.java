package gov.ornl.stucco.relationprediction;
//This class, when initialized, reads a file written by TrainModel.py listing all words in our vocabulary along with their
//vector representations.  It just stores the vectors for each string using a HashMap.


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class WordToVectorMap extends HashMap<String,double[]>
{
	private static HashMap<String,WordToVectorMap> typeTomap = new HashMap<String,WordToVectorMap>();
	private static WordToVectorMap themap = null;
	
	private int vectorlength;
	
	
	WordToVectorMap(File wvf)
	{
		readWordToVectorsFile(wvf);
	}
	
	private void readWordToVectorsFile(File wvf)
	{
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(wvf));
			String line;
			while((line = in.readLine()) != null)
			{
				String[] splitline = line.split(" ");
				String word = splitline[0];
				double[] vector = new double[splitline.length-1];
				for(int i = 1; i < splitline.length; i++)
					vector[i-1] = Double.parseDouble(splitline[i]);
				put(word, vector);
				
				vectorlength = vector.length;
			}
			in.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	
	
	public static WordToVectorMap getWordToVectorMap(String trainingtype)
	{
		WordToVectorMap wvm = typeTomap.get(trainingtype);
		if(wvm == null)
		{
			File f = ProducedFileGetter.getWordVectorsFile(trainingtype);
			wvm = new WordToVectorMap(f);
			typeTomap.put(trainingtype, wvm);
		}
		
		return wvm;
	}
	
	
	//Construct a vector corresponding to this context by averaging the vectors of its words.
	public double[] getContextVector(String[] context)
	{
		double[] result = new double[vectorlength];
		
		int existingtokenscount = 0;	//Count the number of tokens in this context that we appear in our map.
		for(String token : context)
		{
			double[] tokenvector = get(token);
			
			if(tokenvector != null)
			{
				existingtokenscount++;
				
				for(int i = 0; i < vectorlength; i++)
					result[i] += tokenvector[i];
			}
		}
		
		if(existingtokenscount != 0)
		{
			for(int i = 0; i < vectorlength; i++)
				result[i] /= existingtokenscount;
		}
		
		return result;
	}
	//Construct a vector corresponding to this context by averaging the vectors of its words.
	public double[] getContextVector(ArrayList<String> context)
	{
		double[] result = new double[vectorlength];
		
		int existingtokenscount = 0;	//Count the number of tokens in this context that we appear in our map.
		for(String token : context)
		{
			double[] tokenvector = get(token);
			
			if(tokenvector != null)
			{
				existingtokenscount++;
				
				for(int i = 0; i < vectorlength; i++)
					result[i] += tokenvector[i];
			}
		}
		
		if(existingtokenscount != 0)
		{
			for(int i = 0; i < vectorlength; i++)
				result[i] /= existingtokenscount;
		}
		
		return result;
	}


	public ArrayList<ObjectRank> findNearestWords(String testword)
	{
		ArrayList<ObjectRank> similarityrankedwords = new ArrayList<ObjectRank>();
		
		double[] testwordvector = get(testword);
		for(String word : keySet())
		{
			double[] wordvector = get(word);
			
			//similarityrankedwords.add(new ObjectRank(word, cosineSimilarity(testwordvector, wordvector)));
			similarityrankedwords.add(new ObjectRank(word, vectorDistance(testwordvector, wordvector)));
		}
		
		Collections.sort(similarityrankedwords);
		//Collections.reverse(similarityrankedwords);
		
		return similarityrankedwords;
	}
	
	public static double vectorDistance(double[] v1, double[] v2)
	{
		double result = 0.;
		
		for(int i = 0; i < v1.length; i++)
		{
			double dif = v1[i] - v2[i];
			result += dif * dif;
		}
		
		return Math.sqrt(result);
	}
	
	public static double cosineSimilarity(double[] v1, double[] v2)
	{
		double dotproduct = dotProduct(v1, v2);
		double v1length = vectorLength(v1);
		double v2length = vectorLength(v2);
		
		return dotproduct / (v1length * v2length);
	}
	
	public static double vectorLength(double[] vector)
	{
		double result = 0.;
		
		for(double v : vector)
			result += v * v;
		
		return Math.sqrt(result);
	}
	
	public static double dotProduct(double[] v1, double[] v2)
	{
		double result = 0.;
		
		for(int i = 0; i < v1.length; i++)
			result += v1[i] * v2[i];
		
		return result;
	}
}
