package gov.ornl.stucco;

import gov.ornl.stucco.entity.models.Sentence;
import gov.ornl.stucco.entity.models.Sentences;
import gov.ornl.stucco.entity.models.Word;

import org.junit.Test;

public class RelationExtractorTest {
	private static String expectedGraph = "{ \"vertices\": [{\"_id\":\"software_0_0\",\"name\":\"software_0_0\",\"_type\":\"vertex\",\"vertexType\":\"software\",\"product\":\"Windows XP\",\"source\":\"nvd\",\"vendor\":\"Microsoft\",\"version\":\"before 2.8\"}," +
			"{\"_id\":\"vulnerability_0_1\",\"name\":\"vulnerability_0_1\",\"_type\":\"vertex\",\"vertexType\":\"vulnerability\",\"source\":\"nvd\",\"description\":\"cross-site scripting vulnerability\"}," +
			"{\"_id\":\"vulnerability_0_2\",\"name\":\"vulnerability_0_2\",\"_type\":\"vertex\",\"vertexType\":\"vulnerability\",\"source\":\"nvd\",\"description\":\"file.php\"}," +
			"{\"_id\":\"CVE-2014-1234\",\"name\":\"CVE-2014-1234\",\"_type\":\"vertex\",\"vertexType\":\"vulnerability\",\"source\":\"nvd\"}],\n" +
			"\"edges\": [{\"_id\":\"software_0_0__CVE-2014-1234\",\"_type\":\"edge\",\"_label\":\"hasVulnerability\",\"_inV\":\"CVE-2014-1234\",\"_outV\":\"software_0_0\",\"inVType\":\"vulnerability\",\"outVType\":\"software\",\"source\":\"nvd\"}," +
			"{\"_id\":\"vulnerability_0_1__CVE-2014-1234\",\"_type\":\"edge\",\"_label\":\"sameAs\",\"_inV\":\"CVE-2014-1234\",\"_outV\":\"vulnerability_0_1\",\"inVType\":\"vulnerability\",\"outVType\":\"vulnerability\",\"source\":\"nvd\"}," +
			"{\"_id\":\"vulnerability_0_2__CVE-2014-1234\",\"_type\":\"edge\",\"_label\":\"sameAs\",\"_inV\":\"CVE-2014-1234\",\"_outV\":\"vulnerability_0_2\",\"inVType\":\"vulnerability\",\"outVType\":\"vulnerability\",\"source\":\"nvd\"}," +
			"{\"_id\":\"vulnerability_0_1__vulnerability_0_2\",\"_type\":\"edge\",\"_label\":\"sameAs\",\"_inV\":\"vulnerability_0_2\",\"_outV\":\"vulnerability_0_1\",\"inVType\":\"vulnerability\",\"outVType\":\"vulnerability\",\"source\":\"nvd\"}," +
			"{\"_id\":\"software_0_0__vulnerability_0_2\",\"_type\":\"edge\",\"_label\":\"hasVulnerability\",\"_inV\":\"vulnerability_0_2\",\"_outV\":\"software_0_0\",\"inVType\":\"vulnerability\",\"outVType\":\"software\",\"source\":\"nvd\"}," +
			"{\"_id\":\"software_0_0__vulnerability_0_1\",\"_type\":\"edge\",\"_label\":\"hasVulnerability\",\"_inV\":\"vulnerability_0_1\",\"_outV\":\"software_0_0\",\"inVType\":\"vulnerability\",\"outVType\":\"software\",\"source\":\"nvd\"}] }";
	
	private static String[] words = {"Microsoft", "Windows", "XP", "before", "2.8", "has", "cross-site", "scripting", "vulnerability", "in", "file.php", "(", "refer", "to", "CVE-2014-1234", ")", "."};
	private static String[] iob = {"B", "B", "I", "B", "I", "O", "B", "I", "I", "O", "B", "O", "O", "O", "B", "O", "O"};
	private static String[] labels = {"sw.vendor", "sw.product", "sw.product", "sw.version", "sw.version", "O", "vuln.relevant_term", "vuln.relevant_term", "vuln.relevant_term", "O", "vuln.symbol", "O", "O", "O", "vuln.name", "O", "O"};

	@Test
	public void testGetGraph() {
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
		
		assert(graph.equals(expectedGraph));
	}

}
