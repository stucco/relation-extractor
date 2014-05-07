package gov.ornl.stucco.ontology;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;


/**
 * Represents the portion of the knowledge graph schema that
 * was needed to create vertices and edges.
 *
 */
public class Ontology implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("properties")
	private Properties properties;
	
	
	@JsonGetter("properties")
	public Properties getProperties() {
		return properties;
	}

	@JsonSetter("properties")
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return properties.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
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
		Ontology other = (Ontology) obj;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}
}
