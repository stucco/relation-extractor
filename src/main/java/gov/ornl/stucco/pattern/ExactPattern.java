package gov.ornl.stucco.pattern;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberAnnotation;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberEntityMentionsAnnotation;
import gov.ornl.stucco.entity.models.CyberEntityMention;
import gov.ornl.stucco.model.CyberRelation;
import gov.ornl.stucco.pattern.elements.CyberEntity;
import gov.ornl.stucco.pattern.elements.POS;
import gov.ornl.stucco.pattern.elements.PatternElement;
import gov.ornl.stucco.pattern.elements.Token;

import java.util.ArrayList;
import java.util.List;

public class ExactPattern extends MatchingPattern {

	public ExactPattern(List<PatternElement> seq, String edgeType, String vertexType) {
		super(seq, edgeType, vertexType);
	}

	public List<CyberRelation> findPattern(Annotation doc) {
		List<CyberRelation> relationships = new ArrayList<CyberRelation>();
		
		//get the cyber entity elements from the pattern to find in the sentence
		//Note: patterns have at least 2 cyber entities to make a vertex or edge
		int patternEntityIndex1 = this.getCyberEntityIndex(0);
		if (patternEntityIndex1 == -1) {
			return relationships;
		}
		CyberEntity patternEntity1 = (CyberEntity) sequence.get(patternEntityIndex1);
		int patternEntityIndex2 = this.getCyberEntityIndex(patternEntityIndex1+1);
		if (patternEntityIndex2 == -1) {
			return relationships;
		}
		CyberEntity patternEntity2 = (CyberEntity) sequence.get(patternEntityIndex2);
		int patternDistance = patternEntityIndex2 - patternEntityIndex1;
		
		//collect all document's cyber entity mentions
		//Note: patterns can span sentences
		//TODO: make CyberEntityMention a doc level annotation??
		List<CyberEntityMention> entities = new ArrayList<CyberEntityMention>();
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			List<CyberEntityMention> sentenceEntities = sentence.get(CyberEntityMentionsAnnotation.class);
			if ((sentenceEntities != null) && (!sentenceEntities.isEmpty())) {
				entities.addAll(sentenceEntities);
			}
		}
		
		//there can be multiple instances of the same pattern in a given document
		for (int j=0; j<entities.size(); j++) {
			//search for the next instance of the cyber entity mentions within the document that corresponds to the pattern's first cyber entity
			CyberEntityMention mention1 = entities.get(j);
			if ((mention1.getType().equalsIgnoreCase(patternEntity1.getType())) && (mention1.getSubType().equalsIgnoreCase(patternEntity1.getSubType()))) {
			
				//find the second cyber mention in the document that matches the pattern entity
				for (int i=j+1; i<entities.size(); i++) {
					CyberEntityMention mention2 = entities.get(i);
					int startIndex = mention1.getHeadTokenEnd() - 1;
					int endIndex = mention2.getHeadTokenStart();
					if (startIndex + patternDistance > (mention1.getSentence().get(TokensAnnotation.class)).size()) {
						startIndex = (mention1.getSentence().get(TokensAnnotation.class)).size() - startIndex;
						startIndex = startIndex * -1;
					}
					
					if ((mention2.getType().equalsIgnoreCase(patternEntity2.getType()) && mention2.getSubType().equalsIgnoreCase(patternEntity2.getSubType())) && (startIndex + patternDistance >= endIndex)) {
						if (startIndex + patternDistance == endIndex) {
							CyberRelation newRelationship = this.subDocumentMatch(doc, patternEntityIndex1, mention1, mention2, entities, i+1);
							if (newRelationship != null) {
								relationships.add(newRelationship);
								break;
							}
							else {
								//there will not be another possible mention2 within the right token distance
								break;
							}
						}
					}
					// there cannot be another mention2 that matches token distance
					else if (startIndex + patternDistance < endIndex) {
						break;
					}
				}
				
			}
		}	
		
		return relationships;
		
	}
	
	
	private CyberRelation subDocumentMatch(Annotation doc, int patternIndex, CyberEntityMention mention1, CyberEntityMention mention2, List<CyberEntityMention> entities, int mentionIndex) {
		//collect the matching cyber entities to be used in the relationship
		List<CyberEntityMention> relationEntities = new ArrayList<CyberEntityMention>();
		
		//gather the vertex type names that are the inVertex types and those that are the outVertex types
//		Set<String> inVTypes = new HashSet<String>();
//		Set<String> outVTypes = new HashSet<String>();
		
		CoreMap testSentence = mention1.getSentence();
		int docIndex = mention1.getHeadTokenStart() - patternIndex;
		//if the sub document starts in the previous sentence from the first cyber entity, start there
		if (docIndex < 0) {
			testSentence = doc.get(SentencesAnnotation.class).get(mention1.getSentence().get(SentenceIndexAnnotation.class) - 1);
			docIndex = testSentence.get(TokensAnnotation.class).size() + (mention1.getHeadTokenStart() - patternIndex);
		}
		List<CoreLabel> tokens = testSentence.get(TokensAnnotation.class);
		CoreLabel candidateToken = tokens.get(docIndex);
		
		//match each pattern element to a consecutive element in the document
		for (int i=0; i<sequence.size(); i++) {
			PatternElement patternElement = sequence.get(i);
			
			if (i == patternIndex) {
				//if the consecutive elements in the document overlap a sentence, go to the next sentence, and continue
				if ((mention1.getHeadTokenEnd()) >= tokens.size()) {
					tokens = doc.get(SentencesAnnotation.class).get(testSentence.get(SentenceIndexAnnotation.class) + 1).get(TokensAnnotation.class);
					candidateToken = tokens.get(0);
				}
				else {
					candidateToken = tokens.get(mention1.getHeadTokenEnd());
				}
				relationEntities.add(mention1);
//				if (this.edgeType != null) {
//					if (patternElement.getvType() == PatternElement.edgeVType.inV) {
//						inVTypes.add(mention1.getType());
//					}
//					else if (patternElement.getvType() == PatternElement.edgeVType.outV) {
//						outVTypes.add(mention1.getType());
//					}
//					else {
//						System.err.println("Warning: The ExactPattern with '" + this.sequence + "' has an invalid vType definition.");
//					}
//				}
			}
			else if (patternElement instanceof CyberEntity) {
				if ((mention2.getType().equalsIgnoreCase(((CyberEntity) patternElement).getType())) && (mention2.getSubType().equalsIgnoreCase(((CyberEntity) patternElement).getSubType()))) {
					//if the consecutive elements in the document overlap a sentence, go to the next sentence, and continue
					if ((mention2.getHeadTokenEnd()) >= tokens.size()) {
						tokens = doc.get(SentencesAnnotation.class).get(testSentence.get(SentenceIndexAnnotation.class) + 1).get(TokensAnnotation.class);
						candidateToken = tokens.get(0);
					}
					else {
						candidateToken = tokens.get(mention2.getHeadTokenEnd());
					}
					relationEntities.add(mention2);
//					if (this.edgeType != null) {
//						if (patternElement.getvType() == PatternElement.edgeVType.inV) {
//							inVTypes.add(mention2.getType());
//						}
//						else if (patternElement.getvType() == PatternElement.edgeVType.outV) {
//							outVTypes.add(mention2.getType());
//						}
//						else {
//							System.err.println("Warning: The ExactPattern with '" + this.sequence + "' has an invalid vType definition.");
//						}
//					}
				}
				else if (!(candidateToken.get(CyberAnnotation.class).toString()).equalsIgnoreCase(patternElement.getValue())) {
					return null;
				}
				else {
					CyberEntityMention newMention = entities.get(mentionIndex);
					if(newMention.getHeadTokenStart() + 1 == candidateToken.index()) {
						if ((newMention.getType().equalsIgnoreCase(((CyberEntity) patternElement).getType())) && (newMention.getSubType().equalsIgnoreCase(((CyberEntity) patternElement).getSubType()))) {
							//if the consecutive elements in the document overlap a sentence, go to the next sentence, and continue
							if ((newMention.getHeadTokenEnd()) >= tokens.size()) {
								tokens = doc.get(SentencesAnnotation.class).get(testSentence.get(SentenceIndexAnnotation.class) + 1).get(TokensAnnotation.class);
								candidateToken = tokens.get(0);
							}
							else {
								candidateToken = tokens.get(newMention.getHeadTokenEnd());
							}
							mentionIndex = mentionIndex + 1;
							relationEntities.add(newMention);
//							if (this.edgeType != null) {
//								if (patternElement.getvType() == PatternElement.edgeVType.inV) {
//									inVTypes.add(newMention.getType());
//								}
//								else if (patternElement.getvType() == PatternElement.edgeVType.outV) {
//									outVTypes.add(newMention.getType());
//								}
//								else {
//									System.err.println("Warning: The ExactPattern with '" + this.sequence + "' has an invalid vType definition.");
//								}
//							}
						}
					}
				}
			}
			else if (patternElement instanceof Token) {
				if (!candidateToken.get(TextAnnotation.class).equalsIgnoreCase(patternElement.getValue())) {
					return null;
				}
				//if the consecutive elements in the document overlap a sentence, go to the next sentence, and continue
				if ((candidateToken.index()) >= tokens.size()) {
					tokens = doc.get(SentencesAnnotation.class).get(testSentence.get(SentenceIndexAnnotation.class) + 1).get(TokensAnnotation.class);
					candidateToken = tokens.get(0);
				}
				else {
					candidateToken = tokens.get(candidateToken.index());
				}
			}
			else if (patternElement instanceof POS) {
				if (!candidateToken.getString(PartOfSpeechAnnotation.class).equalsIgnoreCase(patternElement.getValue())) {
					return null;
				}
				//if the consecutive elements in the document overlap a sentence, go to the next sentence, and continue
				if ((candidateToken.index()) >= tokens.size()) {
					tokens = doc.get(SentencesAnnotation.class).get(testSentence.get(SentenceIndexAnnotation.class) + 1).get(TokensAnnotation.class);
					candidateToken = tokens.get(0);
				}
				else {
					candidateToken = tokens.get(candidateToken.index());
				}
			}
		}
		
		//build the new cyber-domain vertex or multiple vertices and connecting edge
		CyberRelation newRelation = null;
		if (this.edgeType != null) {
			newRelation = new CyberRelation(relationEntities, this.edgeType, true, this.inVTypes, this.outVTypes);
		}
		else if (this.vertexType != null) {
			newRelation = new CyberRelation(relationEntities, this.vertexType, false);
		}
		
		return newRelation;
	}

	private int getCyberEntityIndex(int fromIndex) {
		if (fromIndex >= 0) {
			for (int i=fromIndex; i<sequence.size(); i++) {
				if (sequence.get(i) instanceof CyberEntity) {
					return i;
				}
			}
		}
		return -1;
	}
}
