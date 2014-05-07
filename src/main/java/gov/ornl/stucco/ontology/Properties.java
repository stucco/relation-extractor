package gov.ornl.stucco.ontology;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Represents the "properties" JSON object from
 * the ontology.
 *
 */
public class Properties implements Serializable {
	private static final long serialVersionUID = 1L;
	

	@JsonProperty("vertices")
	private Element vertices;
	
	@JsonProperty("edges")
	private Element edges;
	
	
	@JsonGetter("vertices")
	public Element getVertices() {
		return vertices;
	}

	@JsonSetter("vertices")
	public void setVertices(Element vertices) {
		this.vertices = vertices;
	}

	@JsonGetter("edges")
	public Element getEdges() {
		return edges;
	}

	@JsonSetter("edges")
	public void setEdges(Element edges) {
		this.edges = edges;
	}

	@Override
	public String toString() {
		return "Properties [\n vertices=" + vertices + ",\n edges=" + edges + "\n]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
		result = prime * result
				+ ((vertices == null) ? 0 : vertices.hashCode());
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
		Properties other = (Properties) obj;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		if (vertices == null) {
			if (other.vertices != null)
				return false;
		} else if (!vertices.equals(other.vertices))
			return false;
		return true;
	}
}
