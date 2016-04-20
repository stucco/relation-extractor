package gov.ornl.stucco.pattern;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberEntityMentionsAnnotation;
import gov.ornl.stucco.entity.models.CyberEntityMention;
import gov.ornl.stucco.model.CyberRelation;
import gov.ornl.stucco.pattern.elements.CyberEntity;
import gov.ornl.stucco.pattern.elements.PatternElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ParseTreePattern extends MatchingPattern {
	
	public ParseTreePattern(List<PatternElement> seq, String edgeType, String vertexType) {
		super(seq, edgeType, vertexType);
	}

	// For each cyber entity mention in the sentence
	//	1) Find the head node of the subtree containing all words of the cyber entity mention
	//	2) Attempt to find this pattern's sequence of parse-tree elements between each pair of cyber entities
	//		in this sentence.
	public List<CyberRelation> findPattern(Annotation doc) {
		List<CyberRelation> relationships = new ArrayList<CyberRelation>();
		
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		// Parse trees are only constructed per sentence, so there is no paths
		//	that cross the sentence boundary.
		for (CoreMap sentence : sentences) {
			Tree rootTree = sentence.get(TreeAnnotation.class);
			List<CyberEntityMention> cyberMentions = sentence.get(CyberEntityMentionsAnnotation.class);
			if ((cyberMentions == null) || (cyberMentions.isEmpty())) {
				continue;
			}
			Map<CyberEntityMention, Tree> entitySubtreeMap = new HashMap<CyberEntityMention, Tree>();
			
			// Gather subtrees of cyber entity mentions
			for (CyberEntityMention cyberMention : cyberMentions) {
				Tree sharedHead = getSharedHeadNode(cyberMention, rootTree);
				if (sharedHead != null) {
					entitySubtreeMap.put(cyberMention, sharedHead);
				}
			}
			
			// Attempt to find the pattern's sequence between every two cyber entity mentions
			Iterator<CyberEntityMention> cyberMentionIter = entitySubtreeMap.keySet().iterator();
			while (cyberMentionIter.hasNext()) {
				CyberEntityMention cyberMention = cyberMentionIter.next();
				Tree subTree = entitySubtreeMap.get(cyberMention);
				
				// Initial checks include the first cyber mention and its subtree root matches the first two element in the sequence,
				//		and the second cyber mention, along with its subtree root matches the last two sequence element. Otherwise,
				//		don't bother comparing the path between.
				if ((this.sequence.get(0) instanceof CyberEntity) && (this.sequence.get(this.sequence.size()-1) instanceof CyberEntity)) {
					CyberEntity seqFirst = (CyberEntity) this.sequence.get(0);
					CyberEntity seqLast = ((CyberEntity) this.sequence.get(this.sequence.size()-1));
				
					if ((cyberMention.getType().equalsIgnoreCase(seqFirst.getType())) && (cyberMention.getSubType().equalsIgnoreCase(seqFirst.getSubType())) && ((subTree.label().toString()).equalsIgnoreCase(this.sequence.get(1).getValue()))) {
						for (CyberEntityMention cyberMention2 : entitySubtreeMap.keySet()) {
							Tree subTree2 = entitySubtreeMap.get(cyberMention2);
							if ((!cyberMention2.equals(cyberMention, true)) && (cyberMention2.getType().equalsIgnoreCase(seqLast.getType())) && (cyberMention2.getSubType().equalsIgnoreCase(seqLast.getSubType())) && ((subTree2.label().toString()).equalsIgnoreCase(this.sequence.get(this.sequence.size()-2).getValue()))) {
								
								// Compare the path between elements to the parse-tree portion of the sequence
								List<Tree> pathBetween = rootTree.pathNodeToNode(subTree, subTree2);
								if (pathBetween != null) {
									int i;
									for (i=1; i<this.sequence.size()-1; i++) {
										String pathLabel = pathBetween.get(i-1).label().toString();
										String sequenceLabel = this.sequence.get(i).getValue();
										if (!pathLabel.equalsIgnoreCase(sequenceLabel)) {
											break;
										}
									}
									// Found a matching path, so create a relationship
									if (i == this.sequence.size()-1) {
										List<CyberEntityMention> relationEntityList = new ArrayList<CyberEntityMention>();
										relationEntityList.add(cyberMention);
										relationEntityList.add(cyberMention2);
										if (this.edgeType != null) {
											CyberRelation newRelation = new CyberRelation(relationEntityList, this.edgeType, true, this.inVTypes, this.outVTypes);
											relationships.add(newRelation);
										}
										else if (this.vertexType != null) {
											CyberRelation newRelation = new CyberRelation(relationEntityList, this.vertexType, false);
											relationships.add(newRelation);
										}
									}
								}
								
							}
						}
					}
				}
			}
		}
		
		return relationships;
	}
	
	private Tree getSharedHeadNode(CyberEntityMention cyberMention, Tree rootTree) {
		Tree currentHead = null;
		
		// Match the cyber entity word(s) to the parse-tree leaf/leaves
		List<Tree> leaves = rootTree.getLeaves();
		int mentionStart = cyberMention.getHeadTokenStart();
		int mentionEnd = cyberMention.getHeadTokenEnd() - 1;
		
		// Find the most dominate node in all paths from the first word in the cyber entity mention
		//	to each of the other words. If this is a one-word cyber entity, then the POS of the word
		//	will be the shared head node.
		Tree t1 = leaves.get(mentionStart);
		currentHead = t1.parent(rootTree);
		for (int i=mentionStart+1; i<=mentionEnd; i++) {
			Tree t2 = leaves.get(i);
			List<Tree> path = rootTree.pathNodeToNode(t1.parent(rootTree), t2.parent(rootTree));
			if (path != null) {
				for (Tree pathTree : path) {
					if (pathTree.dominates(currentHead)) {
						currentHead = pathTree;
					}
				}
			}
		}
		
		return currentHead;
	}

}
