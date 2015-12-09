package gov.ornl.stucco.pattern;

import edu.stanford.nlp.pipeline.Annotation;
import gov.ornl.stucco.model.CyberRelation;

import java.util.List;

public interface MatchingPattern {

	public List<CyberRelation> findPattern(Annotation doc);
}
