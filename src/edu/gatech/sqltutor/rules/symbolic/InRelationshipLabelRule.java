package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.er.ERRelationship;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.InRelationshipToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.util.NLUtil;

public class InRelationshipLabelRule extends StandardSymbolicRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(InRelationshipLabelRule.class);
	
	private static final String PREFIX = "ruleInRelationshipLabel_";
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleInRelationshipLabel", 3);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?inrel", "?leftEntity", "?rightEntity")
	);
	
	private static final StaticRules staticRules = new StaticRules(InRelationshipLabelRule.class);

	public InRelationshipLabelRule() {
	}

	public InRelationshipLabelRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		SymbolicQueries queries = state.getQueries();
		
		while( ext.nextTuple() ) {
			InRelationshipToken inrel = ext.getToken("?inrel");
			TableEntityToken leftEntity = ext.getToken("?leftEntity"),
				rightEntity = ext.getToken("?rightEntity");
			
			ERRelationship relationship = inrel.getRelationship();
			String leftLabel = relationship.getLeftEdge().getConstraint().getLabel();
			
			// don't process if no special label or label change is a no-op
			if( leftLabel == null || leftLabel.isEmpty() 
					|| leftLabel.equalsIgnoreCase(leftEntity.getSingularLabel()) ) {
				continue;
			}
			leftLabel = leftLabel.toLowerCase();
			// FIXME should probably skip if current label is non-default as well
			
			// use the relationship-based label
			leftEntity.setSingularLabel(leftLabel);
			leftEntity.setPluralLabel(NLUtil.pluralize(leftLabel)); // FIXME allow ER override
			
			inrel.getParent().removeChild(inrel);
			List<TableEntityRefToken> refs = queries.getTableEntityReferences(leftEntity);
			for( TableEntityRefToken ref: refs ) {
				makePossessiveRef(ref, rightEntity);
			}
			return true;
		}
		return false;
	}

	private void makePossessiveRef(TableEntityRefToken ref,
			TableEntityToken possessor) {
		TableEntityRefToken leftRef = new TableEntityRefToken(possessor);
		
		ISymbolicToken parent = ref.getParent();
//		List<ISymbolicToken> children = parent.getChildren();
		
		SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);
		seq.addChild(leftRef);

		String posExpr = possessor.getSingularLabel().endsWith("s") ? "'" : "'s";
		LiteralToken literal = new LiteralToken(posExpr, PartOfSpeech.POSSESSIVE_ENDING); 
		seq.addChild(literal);
		
		seq.addChild(new TableEntityRefToken(ref));
		
		SymbolicUtil.replaceChild(ref, seq);
//		int refIdx = children.indexOf(ref);
//		
//		System.out.println("Children before: " + children);
//		
//		children.add(refIdx, literal);
//		children.add(refIdx, leftRef);
//		
//		// manually set parents since we bypassed the normal child mechanism
//		literal.setParent(parent);
//		leftRef.setParent(parent);
//		
//
//		System.out.println("Children after: " + children);
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.FRAGMENT_ENHANCEMENT;
	}

}
