package gov.ornl.stucco;

import gov.ornl.stucco.entity.models.Sentence;
import gov.ornl.stucco.entity.models.Sentences;
import gov.ornl.stucco.entity.models.Word;
import gov.ornl.stucco.graph.Edge;
import gov.ornl.stucco.graph.Vertex;
import gov.ornl.stucco.ontology.Item;
import gov.ornl.stucco.ontology.Ontology;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Main class responsible for creating the vertices from labeled entities,
 * and edges from interpreted relationships between the entities.
 *
 */
public class RelationExtractor {
		
	private static final String ONTOLOGY_PATH = "https://raw.githubusercontent.com/stucco/ontology/master/stucco_schema.json";
	private static final int RELATIONSHIP_SENTENCE_DISTANCE = 2;
	private static ObjectMapper mapper = new ObjectMapper();
	
	private Ontology ontology;
	
	
	public RelationExtractor() {		
		this(ONTOLOGY_PATH);
	}
	
	public RelationExtractor(String ontologyURL) {
		try {
			InputStream inputStream = (new URL(ontologyURL)).openStream();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			ontology = mapper.readValue(inputStream, Ontology.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param source name of the data source
	 * @param labeledEntities annotated version of the unstructured text
	 * @return
	 */
	public String getGraph(String source, Sentences labeledEntities) {
		StringBuilder graphBuilder = new StringBuilder();
		graphBuilder.append("{ \"vertices\": [");
		
		if ((labeledEntities != null) && (!labeledEntities.getSentenceList().isEmpty())) {
			Map<Integer, List<Vertex>> vertices = getVertices(source, labeledEntities);
			for (Integer key : vertices.keySet()) {
				for (Vertex vertex : vertices.get(key)) {
					graphBuilder.append(vertex.toGraphSON());
					graphBuilder.append(",");
				}
			}
			graphBuilder.deleteCharAt(graphBuilder.length()-1);
			graphBuilder.append("],\n");
			
			graphBuilder.append("\"edges\": [");
			List<Edge> edges = getEdges(source, vertices);
			for (Edge edge : edges) {
				graphBuilder.append(edge.toGraphSON());
				graphBuilder.append(",");
			}
			graphBuilder.deleteCharAt(graphBuilder.length()-1);
		}
		
		graphBuilder.append("] }");
		
		return graphBuilder.toString();
	}
	
	/**
	 * @param source name of the data source
	 * @param sentences annotated version of the unstructured text
	 * @return map of sentence index to list of vertices created from the sentence
	 */
	private Map<Integer, List<Vertex>> getVertices(String source, Sentences sentences) {
		Map<Integer, List<Vertex>> vertices = new HashMap<Integer, List<Vertex>>();
		
		List<Sentence> sentenceList = sentences.getSentenceList();
		for (int i=0; i<sentenceList.size(); i++) {
			Sentence sentence = sentenceList.get(i);
					
			StringBuilder phrase = new StringBuilder();
			String label = "";
			for (Word word : sentence.getWordList()) {
				
				if (word.getIob().equalsIgnoreCase("B")) {
					
					if (phrase.length() > 0) {
						List<Vertex> sentVertices = new ArrayList<Vertex>();
						if (vertices.containsKey(Integer.valueOf(i))) {
							sentVertices = vertices.get(Integer.valueOf(i));
						}
						sentVertices = updateVertexList(sentVertices, i, source, phrase.toString(), label);
						
						vertices.put(Integer.valueOf(i), sentVertices);
						
						phrase = new StringBuilder();
					}
					
					phrase.append(word.getWord());
					label = word.getDomainLabel();
					
				}
				else if ((word.getIob().equalsIgnoreCase("I")) && (phrase.length() > 0)) {
					
					phrase.append(" ");
					phrase.append(word.getWord());
					
				}
				else if ((word.getIob().equalsIgnoreCase("O")) && (phrase.length() > 0)) {
					
					List<Vertex> sentVertices = new ArrayList<Vertex>();
					if (vertices.containsKey(Integer.valueOf(i))) {
						sentVertices = vertices.get(Integer.valueOf(i));
					}
					sentVertices = updateVertexList(sentVertices, i, source, phrase.toString(), label);
					
					vertices.put(Integer.valueOf(i), sentVertices);
					
					phrase = new StringBuilder();
					
				}
					
			}
			
		}
		
		return vertices;
	}
	
	/**
	 * @param vertices list of current vertices created from the sentence
	 * @param sentNum index of the current sentence from the list of sentences
	 * @param source name of the data source
	 * @param phrase property value to be added to the vertex
	 * @param label property of the vertex
	 * @return
	 */
	private List<Vertex> updateVertexList(List<Vertex> vertices, int sentNum, String source, String phrase, String label) {
		if (label.startsWith("sw.")) {
			String vertexType = "software";
			label = label.replace("sw.", "");
			
			Item matchingSchema = ontology.getProperties().getVertices().findPropertyKey(vertexType, label);
			if (matchingSchema != null) {
				boolean updated = false;
				
				//check for a vertex to update, starting from the most recently added
				for (int i=vertices.size()-1; i>=0; i--) {
					Vertex currentVertex = vertices.get(i);
					if ((currentVertex.getVertexType().equalsIgnoreCase("software")) && (!currentVertex.getProperties().containsKey(label))) {
						currentVertex.addProperty(label, phrase);
						updated = true;
						break;
					}
				}
				
				//if no existing vertex was found to update, then create one
				if (!updated) {
					String id = "software_" + sentNum + "_" + vertices.size();
					Vertex newVertex = new Vertex(id, vertexType);
					newVertex.addProperty(label, phrase);
					newVertex.addProperty("source", source);
					vertices.add(newVertex);
				}
			}
			else {
				System.err.println("The vertex property '" + label + "' for phrase '" + phrase + "' is not a valid property, according to the ontology.");
			}
			
		}
		else if (label.startsWith("vuln.")) {
			String vertexType = "vulnerability";
			
			if (label.equalsIgnoreCase("vuln.name")) {
				label = "id";
			}
			else if ((label.equalsIgnoreCase("vuln.relevant_term")) || (label.equalsIgnoreCase("vuln.symbol"))) {
				label = "description";
			}
			
			Item matchingSchema = ontology.getProperties().getVertices().findPropertyKey(vertexType, label);
			if (matchingSchema != null) {
				boolean updated = false;
				
				//check for a vertex to update, starting from the most recently added
				for (int i=vertices.size()-1; i>=0; i--) {
					Vertex currentVertex = vertices.get(i);
					if ((currentVertex.getVertexType().equalsIgnoreCase("vulnerability")) && (!currentVertex.getProperties().containsKey(label))) {
						currentVertex.addProperty(label, phrase);
						updated = true;
						break;
					}
				}
				
				//if no existing vertex was found to update, then create one
				if (!updated) {
					String id = "vulnerability_" + sentNum + "_" + vertices.size();
					Vertex newVertex = new Vertex(id, vertexType);
					newVertex.addProperty(label, phrase);
					newVertex.addProperty("source", source);
					vertices.add(newVertex);
				}
			}
			else if (label.equalsIgnoreCase("id")) {
				Vertex newVertex = new Vertex(phrase, vertexType);
				newVertex.addProperty("source", source);
				vertices.add(newVertex);
			}
			else {
				System.err.println("The vertex property '" + label + "' for phrase '" + phrase + "' is not a valid property, according to the ontology.");
			}
		}
		else {
			System.err.println("Unknown label '" + label + "' for phrase '" + phrase + "' encountered.");
		}
		
		return vertices;
	}
	
	/**
	 * @param source name of the data source
	 * @param vertexMap map of sentence index to list of vertices created from the sentence
	 * @return list of edges created
	 */
	private List<Edge> getEdges(String source, Map<Integer, List<Vertex>> vertexMap) {
		List<Edge> edges = new ArrayList<Edge>();
		Map<String, Edge> edgeIdMap = new HashMap<String, Edge>();
		
		for (Integer sentNum : vertexMap.keySet()) {
			List<Vertex> sentVertices = new ArrayList<Vertex>();
			
			int lastSentNum = Math.min(vertexMap.size(), (sentNum.intValue() + RELATIONSHIP_SENTENCE_DISTANCE));
			for (int n=sentNum.intValue(); n<lastSentNum; n++) {		
				sentVertices.addAll(vertexMap.get(Integer.valueOf(n)));
			}
			
			//add an edge between all vertices in the same sentence
			for (int i=0; i<sentVertices.size(); i++) {
				Vertex v1 = sentVertices.get(i);
				
				for (int j=i+1; j<sentVertices.size(); j++) {
					Vertex v2 = sentVertices.get(j);
					
					Vertex inV = v1;
					Vertex outV = v2;
					
					Item matchingEdge = ontology.getProperties().getEdges().findPropertyValues("inVType", v1.getVertexType(), "outVType", v2.getVertexType());
					//if there is no edge matching the search parameters, try looking for the vertex types reversed
					if (matchingEdge == null) {
						matchingEdge = ontology.getProperties().getEdges().findPropertyValues("outVType", v1.getVertexType(), "inVType", v2.getVertexType());
						inV = v2;
						outV = v1;
					}
					
					if (matchingEdge != null) {
						String edgeId = outV.get_id() + "__" + inV.get_id();
						Edge newEdge = new Edge(edgeId, matchingEdge.getTitle());
						newEdge.setSource(source);
						newEdge.set_inV(inV.get_id());
						newEdge.set_outV(outV.get_id());
						newEdge.setInVType(inV.getVertexType());
						newEdge.setOutVType(outV.getVertexType());
						edgeIdMap.put(edgeId, newEdge);
					}
					else if (inV.getVertexType().equalsIgnoreCase(outV.getVertexType())) {
						String edgeId = outV.get_id() + "__" + inV.get_id();
						Edge newEdge = new Edge(edgeId, "sameAs");
						newEdge.setSource(source);
						newEdge.set_inV(inV.get_id());
						newEdge.set_outV(outV.get_id());
						newEdge.setInVType(inV.getVertexType());
						newEdge.setOutVType(outV.getVertexType());
						edgeIdMap.put(edgeId, newEdge);
					}
				}
			}
		}
		
		edges.addAll(edgeIdMap.values());
		return edges;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String testSentence = "Microsoft Windows XP before 2.8 has cross-site scripting vulnerability in file.php (refer to CVE-2014-1234)."; //"Unspecified vulnerability in the update check in Vanilla Forums before 2.0.18.8 has unspecified impact and remote attack vectors, related to 'object injection'.";
		String[] words = {"Microsoft", "Windows", "XP", "before", "2.8", "has", "cross-site", "scripting", "vulnerability", "in", "file.php", "(", "refer", "to", "CVE-2014-1234", ")", "."};
		String[] iob = {"B", "B", "I", "B", "I", "O", "B", "I", "I", "O", "B", "O", "O", "O", "B", "O", "O"};
		String[] labels = {"sw.vendor", "sw.product", "sw.product", "sw.version", "sw.version", "O", "vuln.relevant_term", "vuln.relevant_term", "vuln.relevant_term", "O", "vuln.symbol", "O", "O", "O", "vuln.name", "O", "O"};
		
		Sentence sentence = new Sentence();
		for (int i=0; i<words.length; i++) {
			Word word = new Word(words[i]);
			word.setPos("N");
			word.setIob(iob[i]);
			word.setDomainLabel(labels[i]);
			word.setDomainScore(1.0);
			sentence.addWord(word);
		}
		Sentences sentences = new Sentences();
		sentences.addSentence(sentence);

		RelationExtractor relEx = new RelationExtractor();
		String graph = relEx.getGraph("nvd", sentences);
		System.out.println(graph);
		
	}

}
