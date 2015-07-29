package gov.ornl.stucco.pattern;

import java.util.List;

import edu.stanford.nlp.pipeline.Annotation;
import gov.ornl.stucco.model.CyberRelation;

public interface MatchingPattern {

	public List<CyberRelation> findPattern(Annotation doc);
}
