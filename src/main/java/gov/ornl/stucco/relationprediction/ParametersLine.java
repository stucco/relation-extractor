/*
 * Each ParametersLine represents one possible combination of parameters that can be used during experiments.
 * It is called "ParametersLine" because these combinations are written in the result files right above
 * their corresponding results in one line.  
 */

package gov.ornl.stucco.relationprediction;

import java.util.*;


public class ParametersLine 
{
	//Each line of parameters in the results file is associated with a results fold (the fold that results are calculated on
	//in the following line, and two excluded folds, which were left out of the training set for this experiment.  
	//(The resultsfold will be one of the two excluded folds).
	private Integer resultsfold;
	private Integer excludedfold1;
	private Integer excludedfold2;
	
	

	//Even though the combination of parameter values is represented as a string in the results file, here we represent
	//it as a map from the parameter's name to its value.  We use Strings as values since some of the values are non-numerical.
	private HashMap<String,String> parametersTovalues;
	
	
	
	//Line is the line printed in a results file that describes which test sets and parameters were used.  
	//Or, alternately, if iscmdlineconstraints is turned on, it could be a comma separated list of parameters
	//along with their allowed settings.
	ParametersLine(String line, boolean iscmdlineconstraints)
	{
		parametersTovalues = new HashMap<String,String>();
		
		
		if(iscmdlineconstraints)
		{
			if(!line.equals("null"))
			{
				String[] splitline = line.split(",");
				for(String oneparam : splitline)
				{
					String[] parameterAndvalue = oneparam.split("=");
					parametersTovalues.put(parameterAndvalue[0], parameterAndvalue[1]);
				}
			}
		}
		else
		{
			String[] splitline = line.split(" ");
		
			//Figure out from the parameters line which folds were excluded from the training set (and which fold was used for testing)
			String testfoldsarea = splitline[0];
			String[] resultsfoldAndexcludedfolds = testfoldsarea.split("-");
			
			
			if(resultsfoldAndexcludedfolds[0].equals("null"))
				resultsfold = null;
			else
				resultsfold = Integer.parseInt(resultsfoldAndexcludedfolds[0]);
			
			String[] excludedfolds = resultsfoldAndexcludedfolds[1].split(",");
			
			if(excludedfolds[0].equals("null"))
				excludedfold1 = null;
			else
				excludedfold1 = Integer.parseInt(excludedfolds[0]);
			if(excludedfolds[1].equals("null"))
				excludedfold2 = null;
			else
				excludedfold2 = Integer.parseInt(excludedfolds[1]);
		
			
			//Set the value of the parameter given on this line.
			for(int i = 1; i < splitline.length; i++)
			{
				String[] parameterAndvalue = splitline[i].split("=");
				parametersTovalues.put(parameterAndvalue[0], parameterAndvalue[1]);
			}
		}
	}
	
	
	//Get the test fold associated with the results following this results line in the results file.
	public Integer getResultsFold()
	{
		return resultsfold;
	}

	//Two folds get excluded from the training set during normal experiments.  Here are methods for getting both of them.
	public Integer getExcludedFold1()
	{
		return excludedfold1;
	}
	
	public Integer getExcludedFold2()
	{
		return excludedfold2;
	}
	
	//Even though the combination of parameter values is represented as a string in the results file, here we represent
	//it as a map from the parameter's name to its value.  We use Strings as values since some of the values are non-numerical.
	public HashMap<String,String> getParametersTovalues()
	{
		return parametersTovalues;
	}
	
	//If we sent some constraints to the command line, check whether this ParametersLine satisfies those constraints.
	public boolean matchesConstraints(ParametersLine constraintsline)
	{
		HashMap<String,String> constraintsparams = constraintsline.getParametersTovalues();
		
		for(String parametername : parametersTovalues.keySet())
		{
			String constraintvalue = constraintsparams.get(parametername);
			if(constraintvalue == null)
				continue;
			
			String parametervalue = parametersTovalues.get(parametername);
			
			if(!parametervalue.equals(constraintvalue))
				return false;
		}
		
		return true;
	}
	
	//Check whether two ParametersLines have the same parameters settings.
	public boolean exactlyMatches(ParametersLine parametersline2)
	{
		HashMap<String,String> params2 = parametersline2.getParametersTovalues();
		
		if(params2.size() != parametersTovalues.size())
			return false;
		
		for(String parametername : parametersTovalues.keySet())
		{
			String params2value = params2.get(parametername);
			if(params2value == null)
				return false;
			
			String params1value = parametersTovalues.get(parametername);
			if(params1value == null)
				return false;
			
			if(!params1value.equals(params2value))
				return false;
		}
		
		return true;
	}
	
	
	public static boolean isParametersLine(String line) //heuristically tests whether this looks like a line containing parameters.
	{
		if(line.contains("="))
			return true;
		
		return false;
	}
	
	
	//Write out the parameters in the same manner as command line constraints so that we can use them as command line constraints later.
	public String toString()
	{
		String result = "";
		for(String key : parametersTovalues.keySet())
			result += key + "=" + parametersTovalues.get(key) + " ";
		result = result.trim();
		result = result.replaceAll(" ", ",");
		
		return result;
	}


}
