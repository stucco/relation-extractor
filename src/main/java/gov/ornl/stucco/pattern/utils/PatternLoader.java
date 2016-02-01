package gov.ornl.stucco.pattern.utils;

import gov.ornl.stucco.pattern.Patterns;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PatternLoader {
	private static ObjectMapper mapper = new ObjectMapper();

	public static Patterns loadPatterns(String patternFile) {
		Patterns patterns = null;
		try {
			InputStream inputStream = new FileInputStream(new File(patternFile));
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			patterns = mapper.readValue(inputStream, Patterns.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return patterns;
	}

	public static void main(String[] args) {
		Patterns patterns = PatternLoader.loadPatterns("patterns_relations.json");
		System.out.println(patterns.toString());
	}

}
