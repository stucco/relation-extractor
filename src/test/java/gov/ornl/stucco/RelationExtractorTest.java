package gov.ornl.stucco;

import org.junit.Test;

import edu.stanford.nlp.pipeline.Annotation;
import gov.ornl.stucco.entity.EntityLabeler;

public class RelationExtractorTest {
	private static String expectedGraph = "{" +
	"\"vertices\": {" +
		"\"1235\": {" +
			"\"name\":\"1235\"," +
			"\"vertexType\":\"software\"," +
			"\"product\":\"Windows XP\"," +
			"\"vendor\":\"Microsoft\"," +
			"\"source\":\"CNN\"" +
		"}, " +
		"\"1236\": {" +
			"\"name\":\"1236\"," +
			"\"vertexType\":\"vulnerability\"," +
			"\"cve\":\"CVE-2014-1234\"," +
			"\"description\":\"cross-site scripting\"," +
			"\"source\":\"CNN\"" +
		"}, " +
		"\"1237\": {" +
			"\"name\":\"1237\"," +
			"\"vertexType\":\"software\"," +
			"\"product\":\"Windows XP\"," +
			"\"source\":\"CNN\"" +
		"}, " +
		"\"1238\": {" +
			"\"name\":\"1238\"," +
			"\"vertexType\":\"vulnerability\"," +
			"\"cve\":\"CVE-2014-1234\"," +
			"\"source\":\"CNN\"" +
		"}, " +
		"\"1239\": {" +
			"\"name\":\"1239\"," +
			"\"vertexType\":\"software\"," +
			"\"product\":\"Windows XP\"," +
			"\"source\":\"CNN\"" +
		"}, " +
		"\"1240\": {" +
			"\"name\":\"file.php\"," +
			"\"vertexType\":\"file\"," +
			"\"source\":\"CNN\"" +
		"}" +
	"}," +
	"\"edges\": [" +
		"{" +
			"\"inVertID\":\"1237\"," +
			"\"outVertID\":\"1238\"," +
			"\"relation\":\"ExploitTargetRelatedObservable\"" +
		"}, " +
		"{" +
			"\"inVertID\":\"1240\"," +
			"\"outVertID\":\"1239\"," +
			"\"relation\":\"Sub-Observable\"" +
		"}" +
	"]" +
" }";

	@Test
	public void testGetGraph() {
		String testSentence = "Microsoft Windows XP before 2.8 has cross-site scripting vulnerability in file.php (refer to CVE-2014-1234).";
		EntityLabeler labeler = new EntityLabeler();
		Annotation doc = labeler.getAnnotatedDoc("My Doc", testSentence);
		RelationExtractor rx = new RelationExtractor();
		String graph = rx.createSubgraph(doc, "CNN", "doc title");
//		System.err.println(graph);
//		System.err.println();
//		System.err.println(expectedGraph);
		assert(graph.equals(expectedGraph));
	}

}
