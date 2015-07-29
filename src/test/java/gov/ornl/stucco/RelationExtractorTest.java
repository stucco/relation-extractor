package gov.ornl.stucco;

import edu.stanford.nlp.pipeline.Annotation;
import gov.ornl.stucco.entity.EntityLabeler;

import org.junit.Test;

public class RelationExtractorTest {
	private static String expectedGraph = "{ \"vertices\": [{\"_id\":\"1235\",\"name\":\"1235\",\"_type\":\"vertex\",\"vertexType\":\"software\",\"product\":\"2.8\",\"vendor\":\"before\",\"source\":\"a source\",\"version\":\"has\"}, {\"_id\":\"1236\",\"name\":\"1236\",\"_type\":\"vertex\",\"vertexType\":\"software\",\"product\":\"Tomcat\",\"vendor\":\"Apache\",\"source\":\"a source\",\"version\":\"'s latest\"}, {\"_id\":\"1237\",\"name\":\"1237\",\"_type\":\"vertex\",\"vertexType\":\"software\",\"vendor\":\"Microsoft\",\"source\":\"a source\"}, {\"_id\":\"1238\",\"name\":\"1238\",\"_type\":\"vertex\",\"vertexType\":\"software\",\"product\":\"2.8\",\"vendor\":\"before\",\"source\":\"a source\",\"version\":\"has\"}, {\"_id\":\"1241\",\"name\":\"1241\",\"_type\":\"vertex\",\"vertexType\":\"vulnerability\",\"source\":\"a source\",\"relevant_term\":\"cross-site scripting vulnerability in\"}, {\"_id\":\"1242\",\"name\":\"1242\",\"_type\":\"vertex\",\"vertexType\":\"software\",\"product\":\"Tomcat\",\"source\":\"a source\",\"version\":\"'s latest\"}], \"edges\": [{\"_id\":\"1237_1241\",\"_type\":\"edge\",\"_label\":\"hasVulnerability\",\"_inV\":\"1241\",\"_outV\":\"1237\",\"inVType\":\"vuln\",\"outVType\":\"sw\",\"source\":\"a source\"}, {\"_id\":\"1238_1241\",\"_type\":\"edge\",\"_label\":\"hasVulnerability\",\"_inV\":\"1241\",\"_outV\":\"1238\",\"inVType\":\"vuln\",\"outVType\":\"sw\",\"source\":\"a source\"}] }";

	@Test
	public void testGetGraph() {
		String testSentence = "Microsoft Windows XP before 2.8 has cross-site scripting vulnerability in file.php (refer to CVE-2014-1234). This is a new paragraph about Apache Tomcat's latest update 7.0.1. The software developer who inserted a major security flaw into OpenSSL 1.2.4.8, using the file foo/bar/blah.php has said the error was \"quite trivial\" despite the severity of its impact, according to a new report. The Sydney Morning Herald published an interview today with Robin Seggelmann, who added the flawed code to OpenSSL, the world's most popular library for implementing HTTPS encryption in websites, e-mail servers, and applications. The flaw can expose user passwords and potentially the private key used in a website's cryptographic certificate (whether private keys are at risk is still being determined)."; //"Unspecified vulnerability in the update check in Vanilla Forums before 2.0.18.8 has unspecified impact and remote attack vectors, related to 'object injection'.";
		EntityLabeler labeler = new EntityLabeler();
		Annotation doc = labeler.getAnnotatedDoc("My Doc", testSentence);
		RelationExtractor rx = new RelationExtractor("src/main/resources/patterns_relations_abbrev.json");
		String graph = rx.createSubgraph(doc, "a source");
		assert(graph.equals(expectedGraph));
	}

}
