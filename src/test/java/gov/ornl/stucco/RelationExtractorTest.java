package gov.ornl.stucco;

import edu.stanford.nlp.pipeline.Annotation;
import gov.ornl.stucco.entity.EntityLabeler;

import org.junit.Test;

public class RelationExtractorTest {
	private static String expectedGraph = "{ \"vertices\": {\"1235\": {\"name\":\"1235\",\"vertexType\":\"software\",\"product\":\"Windows XP\",\"source\":\"CNN\",\"vendor\":\"Microsoft\",\"version\":\"before 2.8\"}, \"1236\": {\"name\":\"1236\",\"vertexType\":\"file\",\"source\":\"CNN\",\"name\":\"file.php\"}, \"1237\": {\"name\":\"1237\",\"vertexType\":\"vulnerability\",\"source\":\"CNN\",\"cve\":\"CVE-2014-1234\"}, \"1238\": {\"name\":\"1238\",\"vertexType\":\"software\",\"product\":\"Windows XP\",\"source\":\"CNN\",\"vendor\":\"Microsoft\"}, \"1239\": {\"name\":\"1239\",\"vertexType\":\"software\",\"product\":\"Windows XP\",\"source\":\"CNN\",\"version\":\"before 2.8\"}, \"1240\": {\"name\":\"1240\",\"vertexType\":\"software\",\"product\":\"Windows XP\",\"source\":\"CNN\"}, \"1241\": {\"name\":\"1241\",\"vertexType\":\"vulnerability\",\"source\":\"CNN\",\"description\":\"cross-site scripting\"}, \"1242\": {\"name\":\"1242\",\"vertexType\":\"software\",\"product\":\"Windows XP\",\"source\":\"CNN\"}, \"1243\": {\"name\":\"1243\",\"vertexType\":\"vulnerability\",\"source\":\"CNN\",\"cve\":\"CVE-2014-1234\"}, \"1244\": {\"name\":\"1244\",\"vertexType\":\"software\",\"product\":\"Windows XP\",\"source\":\"CNN\"}, \"1245\": {\"name\":\"1245\",\"vertexType\":\"file\",\"source\":\"CNN\",\"name\":\"file.php\"}},\"edges\": [{\"_id\":\"1237_1236\",\"_type\":\"edge\",\"_label\":\"Exploit_Target_Related_Observable\",\"_inV\":\"1236\",\"_outV\":\"1237\",\"inVType\":\"file\",\"outVType\":\"vuln\",\"source\":\"CNN\"}, {\"_id\":\"1241_1240\",\"_type\":\"edge\",\"_label\":\"Exploit_Target_Related_Observable\",\"_inV\":\"1240\",\"_outV\":\"1241\",\"inVType\":\"sw\",\"outVType\":\"vuln\",\"source\":\"CNN\"}, {\"_id\":\"1243_1242\",\"_type\":\"edge\",\"_label\":\"Exploit_Target_Related_Observable\",\"_inV\":\"1242\",\"_outV\":\"1243\",\"inVType\":\"sw\",\"outVType\":\"vuln\",\"source\":\"CNN\"}, {\"_id\":\"1244_1245\",\"_type\":\"edge\",\"_label\":\"Sub_Observable\",\"_inV\":\"1245\",\"_outV\":\"1244\",\"inVType\":\"file\",\"outVType\":\"sw\",\"source\":\"CNN\"}] }";

	@Test
	public void testGetGraph() {
		String testSentence = "Microsoft Windows XP before 2.8 has cross-site scripting vulnerability in file.php (refer to CVE-2014-1234).";
		EntityLabeler labeler = new EntityLabeler();
		Annotation doc = labeler.getAnnotatedDoc("My Doc", testSentence);
		RelationExtractor rx = new RelationExtractor("src/main/resources/patterns_relations_abbrev.json");
		String graph = rx.createSubgraph(doc, "a source");
		assert(graph.equals(expectedGraph));
	}

}
