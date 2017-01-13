package gov.ornl.stucco.relationprediction;
//This class determines (heuristically) whether a token sent to it in the constructor is a file.

import java.util.HashSet;

public class RelevantFile 
{
	//Suffixes for common file types.
	private static HashSet<String> filenamesuffixes = new HashSet<String>();
	static
	{
		filenamesuffixes.add("sys");
		filenamesuffixes.add("cgi");
		filenamesuffixes.add("cfg");
		filenamesuffixes.add("app");
		filenamesuffixes.add("cxx");
		filenamesuffixes.add("jar");
		filenamesuffixes.add("nlm");
		filenamesuffixes.add("zip");
		filenamesuffixes.add("mdb");
		filenamesuffixes.add("m3u");
		filenamesuffixes.add("so");
		filenamesuffixes.add("dat");
		filenamesuffixes.add("jpg");
		filenamesuffixes.add("xml");
		filenamesuffixes.add("ini");
		filenamesuffixes.add("xs");
		filenamesuffixes.add("cfm");
		filenamesuffixes.add("pm");
		filenamesuffixes.add("do");
		filenamesuffixes.add("conf");
		filenamesuffixes.add("java");
		filenamesuffixes.add("inc");
		filenamesuffixes.add("txt");
		filenamesuffixes.add("sh");
		filenamesuffixes.add("rb");
		filenamesuffixes.add("c");
		filenamesuffixes.add("cpp");
		filenamesuffixes.add("php");
		filenamesuffixes.add("js");
		filenamesuffixes.add("swf");
		filenamesuffixes.add("as");
		filenamesuffixes.add("exe");
		filenamesuffixes.add("cab");
		filenamesuffixes.add("ogm");
		filenamesuffixes.add("aac");
		filenamesuffixes.add("pgm");
		filenamesuffixes.add("asp");
		filenamesuffixes.add("dll");
		filenamesuffixes.add("cgi");
		filenamesuffixes.add("pl");
		filenamesuffixes.add("sys");
		filenamesuffixes.add("cc");
		filenamesuffixes.add("py");
		filenamesuffixes.add("php3");
		filenamesuffixes.add("ocx");
	}
	

	
	private String filename;
	
	
	RelevantFile(String filename)
	{
		this.filename = filename;
	}
	
	public String getFileName()
	{
		return filename;
	}
	
	
	//A token is a file name of a common type if 1) it contains a period other than the one it ends with (if it ends with a period), and 2) if, after stripping extraneous punctiuations from its end (')', ';', ','), it ends with the same suffix as some common file name.
	public static RelevantFile getRelevantFileOfCommonType(String token)
	{
		token = token.toLowerCase();
		
		if(token.length() >= 2)
		{
			if(token.charAt(token.length()-1) == '.' || token.charAt(token.length()-1) == ',' || token.charAt(token.length()-1) == ';' || token.charAt(token.length()-1) == ')')
				token = token.substring(0, token.length()-1);
		
			int lastdotposition = token.lastIndexOf('.');
			if(lastdotposition > 0 && lastdotposition < token.length()-1)
			{
				String suffix = token.substring(lastdotposition+1);
				
				if(filenamesuffixes.contains(suffix))
					return new RelevantFile(token);
			}
		}
		
		return null;
	}
}
