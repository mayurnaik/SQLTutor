package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
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
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;

public class WhereLiteralRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(WhereLiteralRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?token", SymbolicType.WHERE),
		literal(SymbolicPredicates.parentOf, "?parent", "?token", "?pos")
	);

	public WhereLiteralRule() {
		super(DefaultPrecedence.LOWERING);
	}
	
	public WhereLiteralRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		ITuple result = relation.get(0);
		ISymbolicToken token = ext.getToken("?token", result);
		ISymbolicToken parent = ext.getToken("?parent", result);
		LiteralToken where = new LiteralToken("where", PartOfSpeech.WH_ADVERB);
		
		SymbolicUtil.replaceChild(parent, token, where);
		_log.debug(Markers.SYMBOLIC, "Replaced {} with {}", token, where);
		
		return true;
	}
	
	@Override
	protected IQuery getQuery() { return QUERY; }

	@Override
	protected int getVariableEstimate() { return 3; }
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
}
