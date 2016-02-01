package gov.ornl.stucco.pattern;

import edu.stanford.nlp.pipeline.Annotation;
import gov.ornl.stucco.model.CyberRelation;
import gov.ornl.stucco.pattern.elements.CyberEntity;
import gov.ornl.stucco.pattern.elements.PatternElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MatchingPattern {
	
	protected List<PatternElement> sequence;
	protected String edgeType;
	protected String vertexType;
	protected Set<String> inVTypes;
	protected Set<String> outVTypes;

	public MatchingPattern(List<PatternElement> seq, String edgeType, String vertexType) {
		this.sequence = seq;
		this.edgeType = edgeType;
		this.vertexType = vertexType;
		inVTypes = new HashSet<String>();
		outVTypes = new HashSet<String>();
		collectVTypes();
	}
	
	public List<PatternElement> getSequence() {
		return sequence;
	}

	public void setSequence(List<PatternElement> sequence) {
		this.sequence = sequence;
	}

	public String getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(String edgeType) {
		this.edgeType = edgeType;
	}

	public String getVertexType() {
		return vertexType;
	}

	public void setVertexType(String vertexType) {
		this.vertexType = vertexType;
	}

	public Set<String> getInVTypes() {
		return inVTypes;
	}

	public void setInVTypes(Set<String> inVTypes) {
		this.inVTypes = inVTypes;
	}
	
	public void addInVType(String inVType) {
		this.inVTypes.add(inVType);
	}

	public Set<String> getOutVTypes() {
		return outVTypes;
	}

	public void setOutVTypes(Set<String> outVTypes) {
		this.outVTypes = outVTypes;
	}
	
	public void addOutVType(String outVType) {
		this.outVTypes.add(outVType);
	}

	public List<CyberRelation> findPattern(Annotation doc) {
		List<CyberRelation> relationships = new ArrayList<CyberRelation>();
		return relationships;
	}
	
	private void collectVTypes() {
		if (this.edgeType != null) {
			for (int i=0; i<sequence.size(); i++) {
				PatternElement patternElement = sequence.get(i);
				if (patternElement instanceof CyberEntity) {
					String vType = "";
					if (patternElement.getValue().contains(".")) {
						int index = patternElement.getValue().indexOf(".");
						vType = patternElement.getValue().substring(0, index);
					}
					if (patternElement.getvType() == PatternElement.edgeVType.inV) {
						addInVType(vType);
					}
					else if (patternElement.getvType() == PatternElement.edgeVType.outV) {
						addOutVType(vType);
					}
					else {
						System.err.println("Warning: The Pattern with '" + this.sequence + "' has an invalid vType definition.");
					}
				}
			}
		}
	}
}
