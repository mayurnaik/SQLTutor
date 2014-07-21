package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.predicate;

import java.util.EnumSet;
import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
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
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.BetweenToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.NumberToken;

public class RangeToBetweenRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(RangeToBetweenRule.class);
	
	// ruleRangeToBetween(?parent,?lowerCompare,?lowerAttr,?lowerNumber,?upperCompare,?upperAttr,?upperNum)
	private static final IPredicate PREDICATE = predicate("ruleRangeToBetween", 7);
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?parent", 
			"?lowerCompare", "?lowerAttr", "?lowerNumber", 
			"?upperCompare", "?upperAttr", "?upperNumber")
	);
	
	private static final StaticRules staticRules = new StaticRules(RangeToBetweenRule.class);

	public RangeToBetweenRule() {
		super(DefaultPrecedence.FRAGMENT_REWRITE);
	}

	public RangeToBetweenRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		ITuple result = relation.get(0);
		ext.setCurrentTuple(result);
		
		ISymbolicToken parent = ext.getToken("?parent"),
		         lowerCompare = ext.getToken("?lowerCompare"),
		         upperCompare = ext.getToken("?upperCompare");
		NumberToken lowerNum = ext.getToken("?lowerNumber"), 
		            upperNum = ext.getToken("?upperNumber");
		AttributeToken attrToken = ext.getToken("?upperAttr"); // both are equivalent
		
		BetweenToken between = new BetweenToken();
		between.addChild(attrToken);
		between.addChild(lowerNum);
		between.addChild(upperNum);
		
		_log.debug(Markers.SYMBOLIC, "Merging to {} from {} and {}", between, upperCompare, lowerCompare);
		
		if( !parent.getChildren().remove(lowerCompare) )
			throw new SymbolicException("Failed to remove " + lowerCompare);
		SymbolicUtil.replaceChild(parent, upperCompare, between);
		
		return true;
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
	protected int getVariableEstimate() {
		return PREDICATE.getArity();
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
}
