package gov.ornl.stucco.graph;


/**
 * Represents the relationship between two entities, where the entities are vertices
 * and the relationship is an edge in the knowledge graph.
 */
public class Edge {
	private static final String _type = "edge";
	
	private String _id;
	private String _label;
	private String _inV;
	private String _outV;
	private String inVType;
	private String outVType;
	private String source;
	
	
	public Edge(String id, String label, String inV, String inVType, String outV, String outVType, String source) {
		this._id = id;
		this._label = label;
		this._inV = inV;
		this.inVType = inVType;
		this._outV = outV;
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
		return _label;
	}

	public void set_label(String _label) {
		this._label = _label;
	}

	public String get_inV() {
		return _inV;
	}

	public void set_inV(String _inV) {
		this._inV = _inV;
	}

	public String get_outV() {
		return _outV;
	}

	public void set_outV(String _outV) {
		this._outV = _outV;
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
		result = prime * result + ((_inV == null) ? 0 : _inV.hashCode());
		result = prime * result + ((_label == null) ? 0 : _label.hashCode());
		result = prime * result + ((_outV == null) ? 0 : _outV.hashCode());
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
		if (_inV == null) {
			if (other._inV != null)
				return false;
		} else if (!_inV.equals(other._inV))
			return false;
		if (_label == null) {
			if (other._label != null)
				return false;
		} else if (!_label.equals(other._label))
			return false;
		if (_outV == null) {
			if (other._outV != null)
				return false;
		} else if (!_outV.equals(other._outV))
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
		return "Edge [_id=" + _id + ", _label=" + _label + ", _inV=" + _inV
				+ ", _outV=" + _outV + ", inVType=" + inVType + ", outVType="
				+ outVType + ", source=" + source + "]";
	}
	
	public String toGraphSON() {
		StringBuilder graph = new StringBuilder();
		
		graph.append("{");
		
		graph.append("\"_id\":\"");
		graph.append(_id);
		graph.append("\",");
		
		graph.append("\"_type\":\"");
		graph.append(_type);
		graph.append("\",");
		
		graph.append("\"_label\":\"");
		graph.append(_label);
		graph.append("\",");
		
		graph.append("\"_inV\":\"");
		graph.append(_inV);
		graph.append("\",");
		
		graph.append("\"_outV\":\"");
		graph.append(_outV);
		graph.append("\",");
		
		graph.append("\"inVType\":\"");
		graph.append(inVType);
		graph.append("\",");
		
		graph.append("\"outVType\":\"");
		graph.append(outVType);
		graph.append("\",");
		
		graph.append("\"source\":\"");
		graph.append(source);
		
		graph.append("\"}");
		
		return graph.toString();
	}
	
}
