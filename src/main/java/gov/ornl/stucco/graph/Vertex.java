package gov.ornl.stucco.graph;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an entity in the knowledge graph.
 *
 */
public class Vertex {
	private static long uid = 1234;
	
	private static final String _type = "vertex";
			
	private String _id;
	private String name;
	private String vertexType;
	private Map<String, String> properties;
	
	public Vertex(String id, String type) {
		this._id = id;
		this.name = id;
		this.vertexType = type;
		this.properties = new HashMap<String, String>();
	}
	
	public Vertex(String type) {
		this(String.valueOf(getNextUID()), type);
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVertexType() {
		return vertexType;
	}

	public void setVertexType(String vertexType) {
//		if (vertexType.equalsIgnoreCase("sw")) {
//			this.vertexType = "software";
//		}
//		else if (vertexType.equalsIgnoreCase("vuln")) {
//			this.vertexType = "vulnerability";
//		}
//		else {
			this.vertexType = vertexType;
//		}
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public void addProperty(String propertyName, String propertyValue) {
		properties.put(propertyName, propertyValue);
	}
	
	public String getProperty(String propertyName) {
		String value = null;
		if (properties.containsKey(propertyName)) {
			value = (String) properties.get(propertyName);
		}
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
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
		Vertex other = (Vertex) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (vertexType == null) {
			if (other.vertexType != null)
				return false;
		} else if (!vertexType.equals(other.vertexType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Vertex [_id=" + _id + ", name=" + name + ", vertexType="
				+ vertexType + ", properties=" + properties + "]";
	}
	
	public String toJSON() {
		StringBuilder graph = new StringBuilder();
		graph.append("\"");
		graph.append(_id);
		graph.append("\": {");

		graph.append("\"name\":\"");
		graph.append(name);
		graph.append("\",");
		
		graph.append("\"vertexType\":\"");
		if (vertexType.equalsIgnoreCase("sw")) {
			graph.append("software");
		}
		else if (vertexType.equalsIgnoreCase("vuln")) {
			graph.append("vulnerability");
		}
		else {
			graph.append(vertexType);
		}
		graph.append("\",");
		
		for(String prop : properties.keySet()) {
			graph.append("\"");
			graph.append(prop);
			graph.append("\":\"");
			graph.append(properties.get(prop));
			graph.append("\",");
		}
		graph.deleteCharAt(graph.length()-1);
		
		graph.append("}");
		
		return graph.toString();
	}
	
	private static long getNextUID() {
		uid = uid + 1;
		return uid;
	}
}
