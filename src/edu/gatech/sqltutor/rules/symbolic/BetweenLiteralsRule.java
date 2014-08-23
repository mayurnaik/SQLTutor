package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.AndToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.BetweenToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;

public class BetweenLiteralsRule extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(BetweenLiteralsRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?between", SymbolicType.BETWEEN),
		literal(SymbolicPredicates.parentOf, "?parent", "?between", "?pos")
	);

	public BetweenLiteralsRule() {
	}

	public BetweenLiteralsRule(int precedence) {
		super(precedence);
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		ext.setCurrentTuple(relation.get(0));
		
		ISymbolicToken parent = ext.getToken("?parent");
		BetweenToken between = ext.getToken("?between");
		
		SequenceToken seq = new SequenceToken(between.getPartOfSpeech());
		seq.addChild(between.getObjectToken());
		seq.addChild(new LiteralToken("is", PartOfSpeech.VERB_RD_PERSON_SINGULAR_PRESENT));
		seq.addChild(new LiteralToken("between", PartOfSpeech.PREPOSITION_OR_SUBORDINATING_CONJUNCTION));
		
		AndToken and = new AndToken();
		and.addChild(between.getLowerBoundToken());
		and.addChild(between.getUpperBoundToken());
		seq.addChild(and);
		
		_log.debug(Markers.SYMBOLIC, "Replacing {} with {}", between, seq);
		SymbolicUtil.replaceChild(parent, between, seq);
		
		return true;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.LOWERING;
	}

}
