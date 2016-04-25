package gov.ornl.stucco;

import edu.stanford.nlp.pipeline.Annotation;
import gov.ornl.stucco.entity.EntityLabeler;

import org.junit.Test;

public class RelationExtractorTest {
	private static String expectedGraph = "{" +
	"\"vertices\": {" +
		"\"1235\": {" +
			"\"name\": \"1235\"," +
			"\"vertexType\": \"software\"," +
			"\"product\": \"Windows XP\"," +
			"\"source\": \"CNN\"," +
			"\"vendor\": \"Microsoft\"," +
			"\"version\": \"before 2.8\"" +
		"}," +
		"\"1236\": {" +
			"\"name\": \"file.php\"," +
			"\"vertexType\": \"file\"," +
			"\"source\": \"CNN\"" +
		"}," +
		"\"1237\": {" +
			"\"name\": \"1237\"," +
			"\"vertexType\": \"vulnerability\"," +
			"\"source\": \"CNN\"," +
			"\"cve\": \"CVE-2014-1234\"" +
		"}," +
		"\"1238\": {" +
			"\"name\": \"1238\"," +
			"\"vertexType\": \"software\"," +
			"\"product\": \"Windows XP\"," +
			"\"source\": \"CNN\"," +
			"\"vendor\": \"Microsoft\"" +
		"}," +
		"\"1239\": {" +
			"\"name\": \"1239\"," +
			"\"vertexType\": \"software\"," +
			"\"product\": \"Windows XP\"," +
			"\"source\": \"CNN\"," +
			"\"version\": \"before 2.8\"" +
		"}," +
		"\"1240\": {" +
			"\"name\": \"1240\"," +
			"\"vertexType\": \"software\"," +
			"\"product\": \"Windows XP\"," +
			"\"source\": \"CNN\"" +
		"}," +
		"\"1241\": {" +
			"\"name\": \"1241\"," +
			"\"vertexType\": \"vulnerability\"," +
			"\"source\": \"CNN\"," +
			"\"description\": \"cross-site scripting\"" +
		"}," +
		"\"1242\": {" +
			"\"name\": \"1242\"," +
			"\"vertexType\": \"software\"," +
			"\"product\": \"Windows XP\"," +
			"\"source\": \"CNN\"" +
		"}," +
		"\"1243\": {" +
			"\"name\": \"1243\"," +
			"\"vertexType\": \"vulnerability\"," +
			"\"source\": \"CNN\"," +
			"\"cve\": \"CVE-2014-1234\"" +
		"}," +
		"\"1244\": {" +
			"\"name\": \"1244\"," +
			"\"vertexType\": \"software\"," +
			"\"product\": \"Windows XP\"," +
			"\"source\": \"CNN\"" +
		"}," +
		"\"1245\": {" +
			"\"name\": \"file.php\"," +
			"\"vertexType\": \"file\"," +
			"\"source\": \"CNN\"" +
		"}" +
	"}," +
	"\"edges\": [" +
		"{" +
			"\"inVertID\": \"1236\"," +
			"\"outVertID\": \"1237\"," +
			"\"relation\": \"ExploitTargetRelatedObservable\"" +
		"}," +
		"{" +
			"\"inVertID\": \"1240\"," +
			"\"outVertID\": \"1241\"," +
			"\"relation\": \"ExploitTargetRelatedObservable\"" +
		"}," +
		"{" +
			"\"inVertID\": \"1242\"," +
			"\"outVertID\": \"1243\"," +
			"\"relation\": \"ExploitTargetRelatedObservable\"" +
		"}," +
		"{" +
			"\"inVertID\": \"1245\"," +
			"\"outVertID\": \"1244\"," +
			"\"relation\": \"Sub-Observable\"" +
		"}" +
	"]" +
"}";

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
