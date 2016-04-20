package gov.ornl.stucco;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberAnnotation;
import gov.ornl.stucco.entity.EntityLabeler;
import gov.ornl.stucco.graph.Edge;
import gov.ornl.stucco.graph.Vertex;
import gov.ornl.stucco.model.CyberRelation;
import gov.ornl.stucco.pattern.MatchingPattern;
import gov.ornl.stucco.pattern.Pattern;
import gov.ornl.stucco.pattern.Patterns;
import gov.ornl.stucco.pattern.utils.PatternLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class responsible for creating the vertices from labeled entities,
 * and edges from interpreted relationships between the entities.
 *
 */
public class RelationExtractor {
		
	public static final String SOURCE_PROPERTY = "source";
	private static final String DEFAULT_PATTERNS = "patterns/patterns_relations.json";
	
	private Patterns patterns;
	private List<CyberRelation> relationships;
	
	public RelationExtractor() {
		this(DEFAULT_PATTERNS);
	}
	
	public RelationExtractor(String patternFile) {
		this.patterns = PatternLoader.loadPatterns(patternFile);
		this.relationships = new ArrayList<CyberRelation>();
	}
	
	private void findPatterns(Annotation doc, String source) {
		for (Pattern pattern : patterns.getPatterns()) {
			MatchingPattern patternClass = pattern.getPattern();
			relationships.addAll(patternClass.findPattern(doc));
		}
	}
	
	/**
	 * @param doc annotated version of the unstructured text
	 * @param source name of the data source
	 * @return graph in GraphSON format
	 */
	public String createSubgraph(Annotation doc, String source) {
		this.findPatterns(doc, source);
		
		List<Vertex> vertices = new ArrayList<Vertex>();
		List<Edge> edges = new ArrayList<Edge>();
		StringBuilder graphBuilder = new StringBuilder();
		for (CyberRelation relation : relationships) {
			List<Vertex> relationVertices = relation.getVertexList(source);
			List<Edge> relationEdges = relation.getEdgeList(relationVertices, source);
			vertices.addAll(relationVertices);
			edges.addAll(relationEdges);
		}
		
		graphBuilder.append("{ \"vertices\": {");
		for (int i=0; i<vertices.size(); i++) {
			Vertex vertex1 = vertices.get(i);
			graphBuilder.append(vertex1.toJSON());
			if (i < vertices.size() - 1) {
				graphBuilder.append(", ");
			}
			
		}
		
		graphBuilder.append("},");
		graphBuilder.append("\"edges\": [");
		
		for (int k=0; k<edges.size(); k++) {
			Edge e = edges.get(k);
			graphBuilder.append(e.toGraphSON());
			if (k < edges.size() - 1) {
				graphBuilder.append(", ");
			}
		}

		graphBuilder.append("] }");
		
		return graphBuilder.toString();
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String testSentence = "Microsoft Windows XP before 2.8 has cross-site scripting vulnerability in file.php (refer to CVE-2014-1234).";
		EntityLabeler labeler = new EntityLabeler();
		Annotation doc = labeler.getAnnotatedDoc("My Doc", testSentence);
		
							
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for ( CoreMap sentence : sentences) {
			// Label cyber entities appropriately
			for ( CoreLabel token : sentence.get(TokensAnnotation.class)) {
				System.out.println(token.get(TextAnnotation.class) + "\t\t" + token.get(CyberAnnotation.class));
			}
			System.out.println();
		}

		RelationExtractor rx = new RelationExtractor("src/main/resources/patterns_relations_abbrev.json");
		System.out.println(rx.createSubgraph(doc, "CNN"));
		
	}

}
