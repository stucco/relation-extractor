package gov.ornl.stucco.pattern;

import gov.ornl.stucco.pattern.elements.PatternElement;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Pattern implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("edgeType")
	private String edgeType;
	
	@JsonProperty("vertexType")
	private String vertexType;
	
	@JsonProperty("patternType")
	private String patternType;
	
	@JsonProperty("patternSequence")
	private List<PatternElement> patternSequence;
	
	@JsonIgnore
	private MatchingPattern pattern;
	
	@JsonGetter("edgeType")
	public String getEdgeType() {
		return edgeType;
	}
	
	@JsonSetter("edgeType")
	public void setEdgeType(String edgeType) {
		this.edgeType = edgeType;
	}
	
	@JsonGetter("vertexType")
	public String getVertexType() {
		return vertexType;
	}
	
	@JsonSetter("vertexType")
	public void setVertexType(String vertexType) {
		this.vertexType = vertexType;
	}
	
	@JsonGetter("patternType")
	public String getPatternType() {
		return patternType;
	}
	
	@JsonSetter("patternType")
	public void setPatternType(String patternType) {
		this.patternType = patternType;
	}
	
	@JsonSetter("patternSequence")
	public void setPatternSequence(List<PatternElement> patternSequence) {
		this.patternSequence = patternSequence;
	}

	@JsonGetter("patternSequence")
	public List<PatternElement> getPatternSequence() {
		return patternSequence;
	}

	public MatchingPattern getPattern() {
		this.createPattern();
		return pattern;
	}

	public void createPattern() {
		String patternClassName = Pattern.class.getPackage().getName() + "." + this.patternType;
		try {
			Class<?> clas = Pattern.class.getClassLoader().loadClass(patternClassName);
			Constructor<?> constructor = clas.getConstructor(List.class, String.class, String.class);
			this.pattern = (MatchingPattern) constructor.newInstance(this.patternSequence, this.edgeType, this.vertexType);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "Pattern [edgeType=" + edgeType + ", vertexType=" + vertexType
				+ ", patternType=" + patternType + ", patternSequence="
				+ patternSequence + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((edgeType == null) ? 0 : edgeType.hashCode());
		result = prime * result
				+ ((patternSequence == null) ? 0 : patternSequence.hashCode());
		result = prime * result
				+ ((patternType == null) ? 0 : patternType.hashCode());
		result = prime * result
				+ ((vertexType == null) ? 0 : vertexType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pattern other = (Pattern) obj;
		if (edgeType == null) {
			if (other.edgeType != null)
				return false;
		} else if (!edgeType.equals(other.edgeType))
			return false;
		if (patternSequence == null) {
			if (other.patternSequence != null)
				return false;
		} else if (!patternSequence.equals(other.patternSequence))
			return false;
		if (patternType == null) {
			if (other.patternType != null)
				return false;
		} else if (!patternType.equals(other.patternType))
			return false;
		if (vertexType == null) {
			if (other.vertexType != null)
				return false;
		} else if (!vertexType.equals(other.vertexType))
			return false;
		return true;
	}

}
