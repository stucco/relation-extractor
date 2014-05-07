package gov.ornl.stucco.ontology;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Represents a type of vertex, or a type of edge from
 * the ontology.
 *
 */
public class Item implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("title")
	private String title;
	
	@JsonProperty("properties")
	private Map<String, Property> propertyMap;

	
	@JsonGetter("id")
	public String getId() {
		return id;
	}

	@JsonSetter("id")
	public void setId(String id) {
		this.id = id;
	}

	@JsonGetter("title")
	public String getTitle() {
		return title;
	}

	@JsonSetter("title")
	public void setTitle(String title) {
		this.title = title;
	}

	@JsonGetter("properties")
	public Map<String, Property> getPropertyMap() {
		return propertyMap;
	}

	@JsonSetter("properties")
	public void setPropertyMap(Map<String, Property> propertyMap) {
		this.propertyMap = propertyMap;
	}

	@Override
	public String toString() {
		return "\n\t[" + title + ", "
				+ propertyMap + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((propertyMap == null) ? 0 : propertyMap.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		Item other = (Item) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (propertyMap == null) {
			if (other.propertyMap != null)
				return false;
		} else if (!propertyMap.equals(other.propertyMap))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	
}
