package gov.ornl.stucco.pattern;

import edu.stanford.nlp.pipeline.Annotation;
import gov.ornl.stucco.model.CyberRelation;
import gov.ornl.stucco.pattern.elements.PatternElement;

import java.util.ArrayList;
import java.util.List;

public class FuzzyPattern implements MatchingPattern {

	private List<PatternElement> sequence;
	
	public FuzzyPattern(List<PatternElement> seq) {
		this.sequence = seq;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<CyberRelation> findPattern(Annotation doc) {
		List<CyberRelation> relationships = new ArrayList<CyberRelation>();
		System.err.println(">>> Finding fuzzy pattern matches ...");
		
		return relationships;
	}

}
