package gov.ornl.stucco.model;

import gov.ornl.stucco.RelationExtractor;
import gov.ornl.stucco.entity.models.CyberEntityMention;
import gov.ornl.stucco.graph.Edge;
import gov.ornl.stucco.graph.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CyberRelation {
	
	private List<CyberEntityMention> cyberEntities;
	private String relationshipName;
	private boolean isEdge;
	private Set<String> inVTypes;
	private Set<String> outVTypes;
	
	public CyberRelation(List<CyberEntityMention> entities, String relationship, boolean isEdge, Set<String> inVTypes, Set<String> outVTypes) {
		this.cyberEntities = entities;
		this.relationshipName = relationship;
		this.isEdge = isEdge;
		this.inVTypes = inVTypes;
		this.outVTypes = outVTypes;
	}
	
	public CyberRelation(List<CyberEntityMention> entities, String relationship, boolean isEdge) {
		this(entities, relationship, isEdge, new HashSet<String>(), new HashSet<String>());
	}
	
	public CyberRelation(String relationship, boolean isEdge) {
		this(new ArrayList<CyberEntityMention>(), relationship, isEdge);
	}

	public List<CyberEntityMention> getCyberEntities() {
		return cyberEntities;
	}

	public void setCyberEntities(List<CyberEntityMention> cyberEntities) {
		this.cyberEntities = cyberEntities;
	}
	
	public void addCyberEntity(CyberEntityMention cyberEntity) {
		this.cyberEntities.add(cyberEntity);
	}

	public String getRelationshipName() {
		return relationshipName;
	}

	public void setRelationshipName(String relationshipName) {
		this.relationshipName = relationshipName;
	}

	public boolean isEdge() {
		return isEdge;
	}

	public void setIsEdge(boolean isEdge) {
		this.isEdge = isEdge;
	}
	
	public void addInVType(String inVType) {
		this.inVTypes.add(inVType);
	}
	
	public void addOutVType(String outVType) {
		this.outVTypes.add(outVType);
	}
	
	public List<Vertex> getVertexList(String source) {
		List<Vertex> vertices = new ArrayList<Vertex>();
		if (this.isEdge) {
			vertices = createVertexList(source);
		}
		else {
			Vertex v = createVertex(source);
			if (v != null) {
				vertices.add(v);
			}
		}
		return vertices;
	}
	
	private List<Vertex> createVertexList(String source) {
		List<Vertex> newVertices = new ArrayList<Vertex>();
		if ((this.cyberEntities != null) && (!this.cyberEntities.isEmpty())) {
			
			for (CyberEntityMention mention : this.cyberEntities) {
				Vertex v = null;
				v = new Vertex(mention.getType());
				
				boolean exists = false;
				for (int i=newVertices.size()-1; i>=0; i--) {
					Vertex existingV = newVertices.get(i);
					if ((existingV.getVertexType().equalsIgnoreCase(v.getVertexType())) && (existingV.getProperty(mention.getSubType()) == null)) {
						v = existingV;
						exists = true;
						break;
					}
				}
				v.addProperty(mention.getSubType(), mention.getValue());
				v.addProperty(RelationExtractor.SOURCE_PROPERTY, source);
				if (!exists) {
					newVertices.add(v);
				}
			}
			
		}
		
		return newVertices;
	}
	
	private Vertex createVertex(String source) {
		Vertex v = null;
		
		if ((this.cyberEntities != null) && (!this.cyberEntities.isEmpty())) {
			v = new Vertex(this.relationshipName);
			
			for (CyberEntityMention mention : this.cyberEntities) {
				v.addProperty(mention.getSubType(), mention.getValue());
			}
			v.addProperty(RelationExtractor.SOURCE_PROPERTY, source);
			
		}

		return v;
	}
	
	public List<Edge> getEdgeList(List<Vertex> vertices, String source) {
		List<Edge> edges = new ArrayList<Edge>();

		if (this.isEdge) {
			if ((vertices == null) || (vertices.isEmpty())) {
				vertices = getVertexList(source);
			}
		
			for (int i=0; i<vertices.size(); i++) {
				Vertex vertex1 = vertices.get(i);
				
				for (int j=i+1; j<vertices.size(); j++) {
					Vertex vertex2 = vertices.get(j);
					//create the edge between them depending on which vertex type is the inV and the outV
					if ((inVTypes.contains(vertex1.getVertexType())) && (outVTypes.contains(vertex2.getVertexType()))) {
						String id = vertex2.get_id() + "_" + vertex1.get_id();
						Edge e = new Edge(id, this.relationshipName, vertex1.get_id(), vertex1.getVertexType(), vertex2.get_id(), vertex2.getVertexType(), source);
						edges.add(e);
					}
					else if ((outVTypes.contains(vertex1.getVertexType())) && (inVTypes.contains(vertex2.getVertexType()))) {
						String id = vertex1.get_id() + "_" + vertex2.get_id();
						Edge e = new Edge(id, this.relationshipName, vertex2.get_id(), vertex2.getVertexType(), vertex1.get_id(), vertex1.getVertexType(), source);
						edges.add(e);
					}
				}
			}
		}
		
		return edges;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cyberEntities == null) ? 0 : cyberEntities.hashCode());
		result = prime * result + (isEdge ? 1231 : 1237);
		result = prime
				* result
				+ ((relationshipName == null) ? 0 : relationshipName.hashCode());
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
		CyberRelation other = (CyberRelation) obj;
		if (cyberEntities == null) {
			if (other.cyberEntities != null)
				return false;
		} else if (!cyberEntities.equals(other.cyberEntities))
			return false;
		if (isEdge != other.isEdge)
			return false;
		if (relationshipName == null) {
			if (other.relationshipName != null)
				return false;
		} else if (!relationshipName.equals(other.relationshipName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CyberRelation [cyberEntities=" + cyberEntities
				+ ", relationshipName=" + relationshipName + ", isEdge="
				+ isEdge + "]";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
