package gov.ornl.stucco.ontology;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Represents the list of vertex types and edge types from
 * the ontology.
 *
 */
public class Element implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("items")
	private List<Item> items;

	@JsonGetter("items")
	public List<Item> getItems() {
		return items;
	}

	@JsonSetter("items")
	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	@JsonIgnore
	public Item findPropertyKey(String title, String propertyKey) {
		Item foundItem = null;
		for (Item item : items) {
			if ((item.getTitle().equalsIgnoreCase(title)) && (item.getPropertyMap() != null) && (item.getPropertyMap().containsKey(propertyKey))) {
				foundItem = item;
				break;
			}
		}
		
		return foundItem;
	}
	
	@JsonIgnore
	public Item findPropertyValues(String key1, String value1, String key2, String value2) {
		Item foundItem = null;
		for (Item item : items) {
			boolean foundOneMatch = false;
			
			Map<String, Property> propertyMap = item.getPropertyMap();
			if ((propertyMap.containsKey(key1)) && (propertyMap.containsKey(key2))) {
				//only check the enum list of vertex types; exclude checking required
				List<String> vertexTypeList1 = propertyMap.get(key1).getVertexType();
				if (vertexTypeList1 != null) {
					for (String vType : vertexTypeList1) {
						if (vType.equalsIgnoreCase(value1)) {
							foundOneMatch = true;
							break;
						}
					}
				}
				
				List<String> vertexTypeList2 = propertyMap.get(key2).getVertexType();
				if (vertexTypeList2 != null) {
					for (String vType : vertexTypeList2) {
						if ((vType.equalsIgnoreCase(value2)) && (foundOneMatch)) {
							return item;
						}
					}
				}
			}
			
		}
		return foundItem;
	}

	@Override
	public String toString() {
		return "{" + items + "\n}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
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
		Element other = (Element) obj;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		return true;
	}
}
