package gov.ornl.stucco;

import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberAnnotation;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberEntityMentionsAnnotation;
import gov.ornl.stucco.entity.EntityLabeler;
import gov.ornl.stucco.entity.models.CyberEntityMention;
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
	private static final String DEFAULT_PATTERNS = "patterns_relations.json";
	
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
		
		graphBuilder.append("},\n");
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
		String testSentence = "Microsoft Windows XP before 2.8 has cross-site scripting vulnerability in file.php (refer to CVE-2014-1234). Heap-based buffer overflow in the CDrawPoly::Serialize function in fxscover.exe in Microsoft Windows Fax Services Cover Page Editor 5.2 r2 in Windows XP Professional SP3, Server 2003 R2 Enterprise Edition SP2, and Windows 7 Professional allows remote attackers to execute arbitrary code via a long record in a Fax Cover Page (.cov) file. NOTE: some of these details are obtained from third party information."; // This is a new paragraph about Apache Tomcat's latest update 7.0.1. The software developer who inserted a major security flaw into OpenSSL 1.2.4.8, using the file foo/bar/blah.php has said the error was \"quite trivial\" despite the severity of its impact, according to a new report. The Sydney Morning Herald published an interview today with Robin Seggelmann, who added the flawed code to OpenSSL, the world's most popular library for implementing HTTPS encryption in websites, e-mail servers, and applications. The flaw can expose user passwords and potentially the private key used in a website's cryptographic certificate (whether private keys are at risk is still being determined)."; //"Unspecified vulnerability in the update check in Vanilla Forums before 2.0.18.8 has unspecified impact and remote attack vectors, related to 'object injection'.";
//		String exampleText = "Breaking News\n\n\n\n\nU.S. Edition\n\tU.S.\n\tInternational\n\tArabic\n\tEspañol\n\tSet edition preference\n\tConfirm\n\n\n\n\nSign in\nMyCNN\n\n\n\n\n\n\n\nWatch Live TV\n\n\n\n\n\n\tNews\tU.S.\n\tWorld\n\tPolitics\n\tTech\n\tHealth\n\tEntertainment\n\tLiving\n\tTravel\n\tMoney\n\tSports\n\nWatch Live TV\n\n\n\tVideo\tCNNgo\n\tLatest News\n\tMust Watch Videos\n\tDigital Studios\n\nWatch Live TV\n\n\n\tTV\tCNNgo\n\tSchedule\n\tCNN Films\n\tShows A-Z\n\tFaces of CNN Worldwide\n\nWatch Live TV\n\n\n\tOpinions\tPolitical Op-Eds\n\tSocial Commentary\n\tiReport\n\nWatch Live TV\n\n\n\tMore\u2026\tPhotos\n\tLongform\n\tInvestigations\n\tCNN profiles A-Z\n\tCNN Leadership\n\tSomebody's Gotta Do It\n\tParts Unknown\n\tNew Explorers\n\tHigh Profits\n\tThe Guns Project\n\tDigital Shorts\n\t2 Degrees\n\tCNN Heroes\n\tImpact Your World\n\nWatch Live TV\n\n\tQuick Links\n\tPhotos\n\tLongform\n\tInvestigations\n\tCNN profiles A-Z\n\tCNN Leadership\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nTexas floods: Cleanup continues -- and so does the search for bodies\nBy Greg Botelho and Holly Yan, CNN\n\nUpdated 5:28 PM ET, Wed May 27, 2015\n\n\n\n\n\n\n\n\n\n\n\nJUST WATCHED\nWall of water demolished areas \n\n\nReplay\nMore Videos ...\n\n\n\nMUST WATCH\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n (16)\n\n\n\nWall of water demolished areas \n\n\n\n\n\n\nTexas dam on brink of breach\n\n\n\n\n\n\nTexas mom, two kids missing in storm\n\n\n\n\n\n\nHomecoming queen dies on way home from prom\n\n\n\n\n\n\nDwight Howard visits fans stuck in stadium\n\n\n\n\n\n\nTexans scramble to recover after 'wall of water' hits\n\n\n\n\n\n\nPowerful Texas flood sweeps SUV away\n\n\n\n\n\n\nU.S. hurricane drought breaks records\n\n\n\n\n\n\nBefore and after tornado damage\n\n\n\n\n\n\nHow do tornadoes form?\n\n\n\n\n\n\nVanuatu: Before and after Cyclone Pam \n\n\n\n\n\n\nFrozen Niagara Falls creates huge misty ice cloud\n\n\n\n\n\n\nSee beachgoers flee freak hail storm\n\n\n\n\n\n\nA building on fire turns to ice\n\n\n\n\n\n\nHurricanes: What you don't know\n\n\n\n\n\n\nWhat NOT to do in a snowstorm\n\n\n\n\n\nStory highlights\n\t Deputies search for a 73-year-old woman whose car was found in a creek\n\tA Texas woman calls losing everything in her house to flooding \"overwhelming\"\n\t14 missing include a mother and two children whose cabin was washed away\n\n\n\n\n\n\n (CNN)Jennifer Gonzales lost everything after 7 to 8 feet of water swept through her Martindale, Texas, house. The same goes for the residence of her daughter and 4-month grandchild, as well as those of her neighbors and cousin down the street.\n\nEven then, Gonzales considers herself lucky.\n\"It's very overwhelming,\" she told CNN on Wednesday. \"But we have community.\" \nGonzales is hardly alone in her sadness mixed with gratitude. Many others around central and southeastern Texas feel the same, glad to have survived this week's torrential rains and dangerous flooding even if their homes did not.\nFor dozens of families, though, there is no silver lining. At least 33 people -- 14 in northern Mexico, 13 in Texas plus six in Oklahoma -- have died in the severe weather, both tornadoes and flooding from raging rivers.  \nStories from the flood: A 'harrowing' rescue, a teen's life cut short\n\n\n\nAnother 14 people are missing, two of them an elderly couple lost after their rescue boat in Houston capsized. \nOutside the city in nearby Fort Bend County, deputies are searching for a 73-year-old woman who never showed up to work her shift at the convenience store Monday night. The next day, her daughter spotted her car submerged in a creek.  \nThe rest of the missing are in Hays County, where 400-odd homes were destroyed when the Blanco River swelled many times over in a few hours.\n\"It's just very heartbreaking, that we have this loss of life,\" said Kristi Wyatt, a spokeswoman for the Hays County seat in San Marcos. \"Some of those people were in a home together, celebrating the holidays, and they were swept away in the stormwater. ... It's just a terrible situation.\"\n\nSixth body found in Houston\nEven as water levels fell, the horror continued Wednesday.\nSearchers spent the day in Wimberley, the hardest-hit community in Hays County, looking for the 11 people there who are missing and, according to Wyatt, presumed dead.\nTheir efforts were hindered by the wreckage, as well as a near constant threat of thunderstorms -- an inevitable part of late spring in this part of Texas, but a dangerous one if it whips up floodwaters into a frenzy once again. \n\"Every time in rains, it poses a problem for our guys on the ground,\" said San Marcos Fire Marshal Kenneth Bell.\nOpinion: What can be done to stop deadly floods?\nThere were no discoveries of bodies Wednesday in Hays County, but there was one in Houston. After using water pumps, crews found a 31-year-old Hispanic male in a car parked along an entrance ramp to U.S. 59, the city said. \nThis makes for the sixth death so far in Houston, where more searches are underway and more danger is possible if Mother Nature strikes again.\n\nMore storms possible\nThe problem is twofold: When rain falls at a rate of an inch or so an hour, water pools in low-lying areas such as underpasses and decreases visibility to next to nothing, making it hard to go anywhere or see dangers ahead. When it comes on top of weeks of heavy rain, it swells ponds, rivers and bayous -- defining features in Houston -- and they can spill over into neighborhoods.\nThat's what happened Monday and Tuesday, when more than 11 inches fell in some spots around the United States' fourth largest city. \nMore rain is coming, with the National Weather Service noting a chance of storms for at least the next six days in Houston. \n\n\n\nAbandoned vehicles litter a flooded Interstate 45 in Houston on Tuesday.\nEXPAND IMAGE\n\nAreas farther north, including Dallas, are expected to get 2 to 4 inches from Wednesday to Sunday. And parts of eastern Oklahoma will get drenched with 4 to 6 inches of rain. \nFor Houston residents, heavy rain farther north could be as dangerous as whatever falls in the city.\n\"You think conditions are improving, but if it's raining hundreds of miles to the north, it could cause problems,\" CNN meteorologist Pedram Javaheri said. \n\n1,400 structures in Houston damaged\nAs if these Texans haven't had enough problems.\nIn addition to hundreds of stranded vehicles, some 1,400 structures in Houston suffered severe damage as waters crept up.\nSaundra Brown recalled her daughter waking her early Sunday with news \"the bayou was rising.\" As the family rolled up their rug, someone knocked on the door asking for shelter after their vehicle got stuck. Soon, it became apparent nobody was going anywhere.\n\n\n\n\n\n\n\nJUST WATCHED\nHouston's mayor describes the devastation in the city\n\n\nReplay\nMore Videos ...\n\n\n\nMUST WATCH\n\n\n\n\n\n\n\n\n\n\n\n\n\nHouston's mayor describes the devastation in the city 01:39\nPLAY VIDEO\n\"We just told everybody, 'Get on the couches,' \" Brown told CNN. \"Then we put the family on the dining room table. (We moved to) the counters next. And if it was going to rise more, we'd go on the roof.\"\nSix hours later, it was finally safe to get their feet back on the soaked ground. The few days since then have been spent bunking with friends and cleaning up. \n\"It wasn't fun,\" Brown said. \"We're lucky to have a big support structure.\"\n\nMiracles and tragedies\nGood things do happen. So does tragedy. Joe McComb knows both.\nHis son Jonathan, daugther-in-law Laura, grandson Andrew and granddaughter Leighton were in their vacation cabin in Wimberley on Saturday night as the Blanco River swelled.\nFirst, the family moved their cars uphill then went back into the house, which was on stilts. Within a few minutes, as the waters surged, it became evident they wouldn't be able to get to their cars.\n\n\n\n\n\n\n\nJUST WATCHED\nSisters on the phone when home washed away\n\n\nReplay\nMore Videos ...\n\n\n\nMUST WATCH\n\n\n\n\n\n\n\n\n\n\n\nSisters on the phone when home washed away 03:50\nPLAY VIDEO\nThen came a bang, which Joe McComb thinks was something that knocked the cabin off its foundation and into the raging floodwaters. \"All of them gathered in the rooms there, holding onto furniture,\" he said. They \"started floating down the river,\" he said.\nLaura McComb called her sister just before the house hit a bridge and broke apart, scattering the family. Jonathan McComb finally got to dry land about 7 to 12 miles away, his father said.\n\"He said he was fighting the whole time and saying, 'I've got to get out of here, I've got to get out of here,' \" Joe McComb said Wednesday. \"And he said, ... 'Somehow, I was able to get up and catch a breath of air and finally ... work myself up.\"\nJonathan McComb is now in a hospital with a collapsed lung and broken sternum. As much as he's hurting from that, he's hurting more from the fact his beloved wife, his ballet-loving daughter and his baseball-playing son aren't with him.\n\"We're hoping and praying that miracles will happen,\" Joe McComb said. \"But at the same time, we're very realistic.\"\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nWorkers in Midlothian, Texas, try to relieve pressure from the dam at Padera Lake on Wednesday, May 27. Water was flowing over the top of the dam following days of heavy rain. Record-setting rains and dangerous storms have been battering Texas and Oklahoma since Memorial Day weekend.\nHide Caption\n 1 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nBrian Quattrucci piles flood-damaged debris at the curb in front of a home in the Brays Bayou area of Houston on May 27.\nHide Caption\n 2 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nVehicles are left stranded on Highway 288 in Houston on Tuesday, May 26. \nHide Caption\n 3 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nRoberto Salas, left, and Lewis Sternhagen check on a flooded car in Houston on May 26.\nHide Caption\n 4 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nMembers of the Texas National Guard search for bodies on the banks of the Blanco River after flooding in Wimberley, Texas, on May 26.\nHide Caption\n 5 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nClothes and other relief supplies are gathered at Wimberley High School on May 26.\nHide Caption\n 6 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nVehicles in Houston are stranded on Interstate 45 on May 26.\nHide Caption\n 7 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nRobert Briscoe removes a suitcase from his flooded car along I-45 in Houston on May 26.\nHide Caption\n 8 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nNayeli Cervantes carries her friend's daughter through the floodwaters outside their Houston apartment on May 26.\nHide Caption\n 9 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nLucas Rivas looks into a flooded store in Austin, Texas, on Monday, May 25.\nHide Caption\n 10 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nRescue personnel grab the the hand of a man who was stranded in rushing water in Austin on May 25.\nHide Caption\n 11 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nThe cement stilts of a family's home in Wimberley are all that remain on May 25. The home was swept away by floodwaters a day earlier.\nHide Caption\n 12 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nJanie Bell helps her neighbors search for possessions after their vacation home was destroyed in a flash flood along the Blanco River in Wimberley on May 25.\nHide Caption\n 13 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nA shopping center is submerged in water in San Marcos, Texas, on Sunday, May 24.\nHide Caption\n 14 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nHeather Williams and Jayden Martinez Corpus assist the Villegas family in clearing flood-damaged furniture from their home in San Marcos on May 24.\nHide Caption\n 15 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nDavid Barry consoles his 5-year-old daughter, Marley, while she tries to sleep in a flood evacuee room created at the San Marcos Activity Center on May 24.\nHide Caption\n 16 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nRetha Norris, Ally Smith and Christina Norris, all seated in the canoe, are rescued by firefighters on May 24 after they clung to a tree in Kyle, Texas.\nHide Caption\n 17 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nA large tree rests on the Highway 12 bridge over the Blanco River in Wimberley on May 24. \nHide Caption\n 18 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nA grounds worker squeegees water off the course at the Colonial Country Club in Fort Worth, Texas, during the final round of a PGA golf tournament on May 24.\nHide Caption\n 19 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nA cabin is damaged at the Rio Bonito Resort on the banks of the Blanco River in Wimberley on May 24.\nHide Caption\n 20 of 21\n\n\n\n\n\n\nHeavy rains flood Texas and Oklahoma 21 photos\nPolice make an emergency rescue in Amarillo, Texas, on Saturday, May 23.\nHide Caption\n 21 of 21\n\n\n\n\n\n\n\n\n\nCNN's Holly Yan, Rosa Flores, Dana Ford, Christina Zdanowicz, AnneClaire Stapleton, Shawn Nottingham, Jennifer Gray and Ed Lavandera contributed to this report.\n\n\n\n\n\n\n\n\n\t\n\n\n\n\n\t\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nWeathering the storm\n\t\n\n\nStaying safe when the lights go out\n\n\n\n\t\n\n\n5 ways to keep your phone charged\n\n\n\n\t\n\n\nWhat to know about tornadoes\n\n\n\n\t\n\n\n10 deadliest U.S. tornadoes on record\n\n\n\n\t\n\n\nFood safety 101 in a storm\n\n\n\n\t\n\n\nKeep a hurricane preparation checklist\n\n\n\n\n\n\n\n\n\n\t\n\n\n\t\n\n\n\n\n\t\n\n\n\n\n\t\n\n\n\n\n\n\n\n\n\n\n\nMore from US\n\t\n\n\nTexas dam on brink of breach\n\n\n\n\t\n\n\nFBI issues terror bulletin on ISIS social media reach \n\n\n\n\t\n\n\nEngines fail on Singapore Airlines flight\n\n\n\n\n\n\n\nMore from Greg Botelho\n\t\n\n\nFIFA corruption probe targets 'World Cup of fraud,' IRS chief says\n\n\n\n\t\n\n\nStories from the flood: A 'harrowing' rescue, a teen's life cut short\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\tNews\tU.S.\n\tWorld\n\tPolitics\n\tTech\n\tHealth\n\tEntertainment\n\tLiving\n\tTravel\n\tMoney\n\tSports\n\n\n\n\tVideo\tCNNgo\n\tLatest News\n\tMust Watch Videos\n\tDigital Studios\n\n\n\n\tTV\tCNNgo\n\tSchedule\n\tCNN Films\n\tShows A-Z\n\tFaces of CNN Worldwide\n\n\n\n\tOpinions\tPolitical Op-Eds\n\tSocial Commentary\n\tiReport\n\n\n\n\tMore\u2026\tPhotos\n\tLongform\n\tInvestigations\n\tCNN profiles A-Z\n\tCNN Leadership\n\n\n\n\n\nU.S. Edition\n\tU.S.\n\tInternational\n\tArabic\n\tEspañol\n\tSet edition preference\n\tConfirm\n\n\n\n© 2015 Cable News Network. Turner Broadcasting System, Inc. All Rights Reserved.\n\tTerms of service\n\tPrivacy guidelines\n\tAdChoices\n\tAdvertise with us\n\tAbout us\n\tWork for us\n\tHelp\n\tTranscripts\n\tLicense Footage\n\tCNN Newsource\n\n\n\n\n\n\n\n\n";
//				"Buffer overflow in the RPC Locator service for Microsoft Windows NT 4.0, Windows NT 4.0 Terminal Server Edition, Windows 2000, and Windows XP allows local users to execute arbitrary code via an RPC call to the service containing certain parameter information.";
		EntityLabeler labeler = new EntityLabeler();
		Annotation doc = labeler.getAnnotatedDoc("My Doc", testSentence);
		
							
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for ( CoreMap sentence : sentences) {
			// Label cyber entities appropriately
			for ( CoreLabel token : sentence.get(TokensAnnotation.class)) {
//				String word = token.get(TextAnnotation.class);
//				if (word.equalsIgnoreCase("Windows") || word.equalsIgnoreCase("XP")) {
//					token.set(CyberAnnotation.class, "sw.product");
//				}
//				else if (word.equalsIgnoreCase("before") || word.equalsIgnoreCase("2.8")) {
//					token.set(CyberAnnotation.class, "sw.version");
//				}
//				else if (word.equalsIgnoreCase("has") || word.equalsIgnoreCase("in")) {
//					token.set(CyberAnnotation.class, "O");
//				}
//				else if (word.equalsIgnoreCase("cross-site") || word.equalsIgnoreCase("scripting") || word.equalsIgnoreCase("vulnerability")) {
//					token.set(CyberAnnotation.class, "vuln.description");
//				}
//				else if (word.equalsIgnoreCase("file.php")) {
//					token.set(CyberAnnotation.class, "sw.file");
//				}
//				else if (word.equalsIgnoreCase("CVE-2014-1234")) {
//					token.set(CyberAnnotation.class, "vuln.cve");
//				}
				System.out.println(token.get(TextAnnotation.class) + "\t\t" + token.get(CyberAnnotation.class));
			}
			System.out.println();
//			CyberEntityMention e1 = new CyberEntityMention(CyberEntityMention.makeUniqueId(), sentence, new Span(0,1), new Span(0,1), "sw", "vendor", null);
//			CyberEntityMention e2 = new CyberEntityMention(CyberEntityMention.makeUniqueId(), sentence, new Span(1,3), new Span(1,3), "sw", "product", null);
//			CyberEntityMention e3 = new CyberEntityMention(CyberEntityMention.makeUniqueId(), sentence, new Span(3,5), new Span(3,5), "sw", "version", null);
//			CyberEntityMention e4 = new CyberEntityMention(CyberEntityMention.makeUniqueId(), sentence, new Span(6,9), new Span(6,9), "vuln", "description", null);
//			CyberEntityMention e5 = new CyberEntityMention(CyberEntityMention.makeUniqueId(), sentence, new Span(10,11), new Span(10,11), "sw", "file", null);
//			CyberEntityMention e6 = new CyberEntityMention(CyberEntityMention.makeUniqueId(), sentence, new Span(14,15), new Span(14,15), "vuln", "cve", null);
//			List<CyberEntityMention> mentionList = new ArrayList<CyberEntityMention>();
//			mentionList.add(e1);
//			mentionList.add(e2);
//			mentionList.add(e3);
//			mentionList.add(e4);
//			mentionList.add(e5);
//			mentionList.add(e6);
//			sentence.set(CyberEntityMentionsAnnotation.class, mentionList);
////			System.out.println("Entities:\n" + sentence.get(CyberEntityMentionsAnnotation.class));
//////			System.out.println("Semantic Graph:\n" + sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));
		}

		RelationExtractor rx = new RelationExtractor("/Users/k5y/Documents/Projects/STUCCO/Workspace/relation-extractor/src/main/resources/patterns_relations_abbrev.json");
		System.out.println(rx.createSubgraph(doc, "CNN"));
		
	}

}
