package gov.ornl.stucco.ontology;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Represents a possible value for a property of a
 * vertex or an edge within the ontology.
 *
 */
public class Property implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("required")
	private boolean required;
	
	@JsonProperty("enum")
	private List<String> vertexType;
	
	
	@JsonGetter("required")
	public boolean isRequired() {
		return required;
	}

	@JsonSetter("required")
	public void setRequired(boolean required) {
		this.required = required;
	}

	@JsonGetter("enum")
	public List<String> getVertexType() {
		return vertexType;
	}

	@JsonSetter("enum")
	public void setVertexType(List<String> vertexType) {
		this.vertexType = vertexType;
	}

	@Override
	public String toString() {
		if ((vertexType == null) || (vertexType.isEmpty())) {
			return "[]";
		}
		return "[" + vertexType + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (required ? 1231 : 1237);
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
		Property other = (Property) obj;
		if (required != other.required)
			return false;
		if (vertexType == null) {
			if (other.vertexType != null)
				return false;
		} else if (!vertexType.equals(other.vertexType))
			return false;
		return true;
	}

}
