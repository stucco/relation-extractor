package gov.ornl.stucco.graph;


/**
 * Represents the relationship between two entities, where the entities are vertices
 * and the relationship is an edge in the knowledge graph.
 */
public class Edge {
	private static final String _type = "edge";
	
	private String _id;
	private String relation;
	private String inVertID;
	private String outVertID;
	private String inVType;
	private String outVType;
	private String source;
	
	
	public Edge(String id, String label, String inV, String inVType, String outV, String outVType, String source) {
		this._id = id;
		this.relation = label;
		this.inVertID = inV;
		this.inVType = inVType;
		this.outVertID = outV;
		this.outVType = outVType;
		this.source = source;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String get_label() {
		return relation;
	}

	public void set_label(String _label) {
		this.relation = _label;
	}

	public String get_inV() {
		return inVertID;
	}

	public void set_inV(String _inV) {
		this.inVertID = _inV;
	}

	public String get_outV() {
		return outVertID;
	}

	public void set_outV(String _outV) {
		this.outVertID = _outV;
	}

	public String getInVType() {
		return inVType;
	}

	public void setInVType(String inVType) {
		this.inVType = inVType;
	}

	public String getOutVType() {
		return outVType;
	}

	public void setOutVType(String outVType) {
		this.outVType = outVType;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public static String getType() {
		return _type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + ((inVertID == null) ? 0 : inVertID.hashCode());
		result = prime * result + ((relation == null) ? 0 : relation.hashCode());
		result = prime * result + ((outVertID == null) ? 0 : outVertID.hashCode());
		result = prime * result + ((inVType == null) ? 0 : inVType.hashCode());
		result = prime * result
				+ ((outVType == null) ? 0 : outVType.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		Edge other = (Edge) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (inVertID == null) {
			if (other.inVertID != null)
				return false;
		} else if (!inVertID.equals(other.inVertID))
			return false;
		if (relation == null) {
			if (other.relation != null)
				return false;
		} else if (!relation.equals(other.relation))
			return false;
		if (outVertID == null) {
			if (other.outVertID != null)
				return false;
		} else if (!outVertID.equals(other.outVertID))
			return false;
		if (inVType == null) {
			if (other.inVType != null)
				return false;
		} else if (!inVType.equals(other.inVType))
			return false;
		if (outVType == null) {
			if (other.outVType != null)
				return false;
		} else if (!outVType.equals(other.outVType))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[_id=" + _id + ", relation=" + relation + ", inVertID=" + inVertID
				+ ", outVertID=" + outVertID + ", inVType=" + inVType + ", outVType="
				+ outVType + ", source=" + source + "]";
	}
	
	public String toGraphSON() {
		StringBuilder graph = new StringBuilder();
		
		graph.append("{");
		
		graph.append("\"inVertID\":\"");
		graph.append(inVertID);
		graph.append("\",");
		
		graph.append("\"outVertID\":\"");
		graph.append(outVertID);
		graph.append("\",");
		
		graph.append("\"relation\":\"");
		graph.append(relation);
		
		graph.append("\"}");
		
		return graph.toString();
	}
	
}
