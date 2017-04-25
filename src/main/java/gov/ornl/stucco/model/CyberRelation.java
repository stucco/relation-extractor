package gov.ornl.stucco.model;

import java.util.ArrayList;
import java.util.List;

import gov.ornl.stucco.RelationExtractor;
import gov.ornl.stucco.entity.models.CyberEntityType;
import gov.ornl.stucco.graph.Edge;
import gov.ornl.stucco.graph.Vertex;
import gov.ornl.stucco.relationprediction.GenericCyberEntityTextRelationship;

public class CyberRelation {
	
	private CyberEntityType entityType1;
	private String entity1;
	private CyberEntityType entityType2;
	private String entity2;
	private String relationshipName;
	private boolean isEdge;
	
	public CyberRelation(CyberEntityType type1, String entity1, CyberEntityType type2, String entity2, String relationship, boolean isEdge) {
		this.entityType1 = type1;
		this.entity1 = entity1;
		this.entityType2 = type2;
		this.entity2 = entity2;
		this.relationshipName = relationship;
		this.isEdge = isEdge;
	}
	
	public CyberRelation(CyberEntityType type1, String entity1, CyberEntityType type2, String entity2, String relationship) {
		this(type1, entity1, type2, entity2, relationship, (relationship.equalsIgnoreCase(GenericCyberEntityTextRelationship.SAME_VERTEX)) ? false : true);
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
	
	public List<Vertex> getRelationVertices(String source) {
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
		
		Vertex v1 = null;
		
		if ((entity1 != null) && (entityType1 != null) ) {
			v1 = new Vertex(entityType1.getFullCyberType());
			v1.addProperty(entityType1.getCyberSubType(), entity1);
			v1.addProperty(RelationExtractor.SOURCE_PROPERTY, source);
			newVertices.add(v1);
		}
		
		Vertex v2 = null;
		
		if ((entity2 != null) && (entityType2 != null) ) {
			v2 = new Vertex(entityType2.getFullCyberType());
			v2.addProperty(entityType2.getCyberSubType(), entity2);
			v2.addProperty(RelationExtractor.SOURCE_PROPERTY, source);
			newVertices.add(v2);
		}
		
		return newVertices;
	}
	
	private Vertex createVertex(String source) {
		Vertex v = null;
		
		if ((entity1 != null) && (entityType1 != null) && (entity2 != null) && (entityType2 != null)) {
			v = new Vertex(entityType1.getFullCyberType());
			v.addProperty(entityType1.getCyberSubType(), entity1);
			v.addProperty(entityType2.getCyberSubType(), entity2);
			v.addProperty(RelationExtractor.SOURCE_PROPERTY, source);
		}

		return v;
	}
	
	public List<Edge> getRelationEdges(Vertex v1, Vertex v2, String source) {
		List<Edge> edges = new ArrayList<Edge>();

		if (this.isEdge) {
			
			if (v1.getVertexType().equalsIgnoreCase("vulnerability")) {
				String id = v1.get_id() + "_" + v2.get_id();
				Edge e = new Edge(id, this.relationshipName, v2.get_id(), v2.getVertexType(), v1.get_id(), v1.getVertexType(), source);
				edges.add(e);
			}
			else if (v2.getVertexType().equalsIgnoreCase("vulnerability")) {
				String id = v2.get_id() + "_" + v1.get_id();
				Edge e = new Edge(id, this.relationshipName, v1.get_id(), v1.getVertexType(), v2.get_id(), v2.getVertexType(), source);
				edges.add(e);
			}
			else if (v1.getVertexType().equalsIgnoreCase("file")) {
				String id = v2.get_id() + "_" + v1.get_id();
				Edge e = new Edge(id, this.relationshipName, v1.get_id(), v1.getVertexType(), v2.get_id(), v2.getVertexType(), source);
				edges.add(e);
			}
			else if (v1.getVertexType().equalsIgnoreCase("function")) {
				String id = v2.get_id() + "_" + v1.get_id();
				Edge e = new Edge(id, this.relationshipName, v1.get_id(), v1.getVertexType(), v2.get_id(), v2.getVertexType(), source);
				edges.add(e);
			}
			else if (v2.getVertexType().equalsIgnoreCase("file")) {
				String id = v1.get_id() + "_" + v2.get_id();
				Edge e = new Edge(id, this.relationshipName, v2.get_id(), v2.getVertexType(), v1.get_id(), v1.getVertexType(), source);
				edges.add(e);
			}
			else if (v2.getVertexType().equalsIgnoreCase("function")) {
				String id = v1.get_id() + "_" + v2.get_id();
				Edge e = new Edge(id, this.relationshipName, v2.get_id(), v2.getVertexType(), v1.get_id(), v1.getVertexType(), source);
				edges.add(e);
			}
			else {
				System.err.println("ERROR: Unhandled case of relationship direction within CyberRelation class.");
			}
		
		}
		
		return edges;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity1 == null) ? 0 : entity1.hashCode());
		result = prime * result + ((entity2 == null) ? 0 : entity2.hashCode());
		result = prime * result + ((entityType1 == null) ? 0 : entityType1.hashCode());
		result = prime * result + ((entityType2 == null) ? 0 : entityType2.hashCode());
		result = prime * result + (isEdge ? 1231 : 1237);
		result = prime * result + ((relationshipName == null) ? 0 : relationshipName.hashCode());
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
		if (entity1 == null) {
			if (other.entity1 != null)
				return false;
		} else if (!entity1.equals(other.entity1))
			return false;
		if (entity2 == null) {
			if (other.entity2 != null)
				return false;
		} else if (!entity2.equals(other.entity2))
			return false;
		if (entityType1 == null) {
			if (other.entityType1 != null)
				return false;
		} else if (!entityType1.equals(other.entityType1))
			return false;
		if (entityType2 == null) {
			if (other.entityType2 != null)
				return false;
		} else if (!entityType2.equals(other.entityType2))
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
		return "CyberRelation [entityType1=" + entityType1 + ", entity1=" + entity1 + ", entityType2=" + entityType2 + ", entity2=" + entity2 + ", relationshipName=" + relationshipName + ", isEdge="
				+ isEdge + "]";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
