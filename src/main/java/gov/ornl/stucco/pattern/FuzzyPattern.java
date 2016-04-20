package gov.ornl.stucco.pattern;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberEntityMentionsAnnotation;
import gov.ornl.stucco.entity.models.CyberEntityMention;
import gov.ornl.stucco.model.CyberRelation;
import gov.ornl.stucco.pattern.elements.CyberEntity;
import gov.ornl.stucco.pattern.elements.POS;
import gov.ornl.stucco.pattern.elements.PatternElement;
import gov.ornl.stucco.pattern.elements.Token;

import java.util.ArrayList;
import java.util.List;
 

public class FuzzyPattern extends MatchingPattern {

	public static final int FUZZY_MATCH_DISTANCE = 10;

	
	public FuzzyPattern(List<PatternElement> seq, String edgeType, String vertexType) {
		super(seq, edgeType, vertexType);
	}

	public List<CyberRelation> findPattern(Annotation doc) {
		List<CyberRelation> relationships = new ArrayList<CyberRelation>();
		System.err.println(">>> Finding fuzzy pattern matches ...");
		
		// Collect all document's cyber entity mentions
		// Note: patterns can span sentences
		//TODO: make CyberEntityMention a doc level annotation??
		List<CyberEntityMention> entities = new ArrayList<CyberEntityMention>();
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			List<CyberEntityMention> sentenceEntities = sentence.get(CyberEntityMentionsAnnotation.class);
			if ((sentenceEntities != null) && (!sentenceEntities.isEmpty())) {
				entities.addAll(sentenceEntities);
			}
		}
		
		//gather the vertex type names that are the inVertex types and those that are the outVertex types
//		Set<String> inVTypes = new HashSet<String>();
//		Set<String> outVTypes = new HashSet<String>();
		
		// First and last elements of the sequence should be labeled cyber entities
		CyberEntity seqEntity1 = null;
		CyberEntity seqEntity2 = null;
		PatternElement seqElement = this.sequence.get(0);
		if (seqElement instanceof CyberEntity) {
			seqEntity1 = (CyberEntity)seqElement;
		}
		seqElement = this.sequence.get(this.sequence.size()-1);
		if (seqElement instanceof CyberEntity) {
			seqEntity2 = (CyberEntity)seqElement;
		}

		for (int i=0; i<entities.size(); i++) {
			CyberEntityMention docEntity1 = entities.get(i);
			// Find the first sequence element in the doc's list of cyber entities
			if ((docEntity1.getType().equalsIgnoreCase(seqEntity1.getType())) && (docEntity1.getSubType().equalsIgnoreCase(seqEntity1.getSubType()))) {
			
				// Check if the last element in the sequence is the next cyber entity in the doc's list
				if (i+1 < entities.size()) {
					CyberEntityMention docEntity2 = entities.get(i+1);
					
					if ((docEntity2.getType().equalsIgnoreCase(seqEntity2.getType())) && (docEntity2.getSubType().equalsIgnoreCase(seqEntity2.getSubType()))) {
						
						CoreMap docSentence1 = docEntity1.getSentence();
						CoreMap docSentence2 = docEntity2.getSentence();
						// If so, is it within 10 words
						if (docSentence1.get(SentenceIndexAnnotation.class) == docSentence2.get(SentenceIndexAnnotation.class)) {
							if ((docEntity2.getHeadTokenStart() - docEntity1.getHeadTokenEnd()) <= FUZZY_MATCH_DISTANCE) {
								if (isBetweenMatch(docSentence1, null, docEntity1, docEntity2)) {
									List<CyberEntityMention> entityMentionList = new ArrayList<CyberEntityMention>();
									entityMentionList.add(docEntity1);
									entityMentionList.add(docEntity2);
									if (this.edgeType != null) {
										CyberRelation newRelation = new CyberRelation(entityMentionList, this.edgeType, true, this.inVTypes, this.outVTypes);
										relationships.add(newRelation);
									}
									else if (this.vertexType != null) {
										CyberRelation newRelation = new CyberRelation(entityMentionList, this.vertexType, false);
										relationships.add(newRelation);
									}
								}
							}
						}
						else if (docSentence1.get(SentenceIndexAnnotation.class) + 1 == docSentence2.get(SentenceIndexAnnotation.class)) {
							int distance = (docSentence1.get(TokensAnnotation.class).size() - docEntity1.getHeadTokenEnd()) + docEntity2.getHeadTokenStart();
							if (distance <= FUZZY_MATCH_DISTANCE) {
								if (isBetweenMatch(docSentence1, docSentence2, docEntity1, docEntity2)) {
									List<CyberEntityMention> entityMentionList = new ArrayList<CyberEntityMention>();
									entityMentionList.add(docEntity1);
									entityMentionList.add(docEntity2);
									if (this.edgeType != null) {
										CyberRelation newRelation = new CyberRelation(entityMentionList, this.edgeType, true, this.inVTypes, this.outVTypes);
										relationships.add(newRelation);
									}
									else if (this.vertexType != null) {
										CyberRelation newRelation = new CyberRelation(entityMentionList, this.vertexType, false);
										relationships.add(newRelation);
									}
								}
							}
						}
						
					}
					
				}
				else {
					break;
				}
			}
		}
		
		return relationships;
	}
	
	
	private boolean isBetweenMatch(CoreMap docSentence1, CoreMap docSentence2, CyberEntityMention docEntity1, CyberEntityMention docEntity2) {
		List<CoreLabel> tokens = docSentence1.get(TokensAnnotation.class);
		int startTokenIndex = docEntity1.getHeadTokenEnd();
		int endTokenIndex = docEntity2.getHeadTokenStart();
		
		//Working with two consecutive sentence, or a single sentence
		if (docSentence2 != null) {
			endTokenIndex = tokens.size() + docEntity2.getHeadTokenStart() - 1;
			tokens.addAll(docSentence2.get(TokensAnnotation.class));
		}
		
		// Loop through all tokens and see that each PatternElement appears
		int tokenIndex = startTokenIndex;
		int i;
		for (i=1; i<sequence.size()-1; i++) {
			PatternElement patternElement = sequence.get(i);
			while (tokenIndex <= endTokenIndex) {
				if (patternElement instanceof Token) {
					if (tokens.get(tokenIndex).get(TextAnnotation.class).equalsIgnoreCase(patternElement.getValue())) {
						tokenIndex = tokenIndex + 1;
						break;
					}
				}
				else if (patternElement instanceof POS) {
					if (tokens.get(tokenIndex).get(PartOfSpeechAnnotation.class).equalsIgnoreCase(patternElement.getValue())) {
						tokenIndex = tokenIndex + 1;
						break;
					}
				}
				tokenIndex = tokenIndex + 1;
			}
			// End of possible tokens between the cyber entities is reached
			if (tokenIndex > endTokenIndex) {
				return false;
			}
		}
		// If we have matched all PatternElements, this subDocument is a match
		if (i == sequence.size()-1) {
			return true;
		}
		
		return false;
	}

}
