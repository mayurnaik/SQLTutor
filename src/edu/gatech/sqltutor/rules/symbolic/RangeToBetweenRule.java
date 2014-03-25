package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.BetweenToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.NumberToken;

public class RangeToBetweenRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(RangeToBetweenRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.binaryOperator, "?lessThan", "<"),
		literal(SymbolicPredicates.binaryOperator, "?greaterThan", ">"),
		literal(SymbolicPredicates.parentOf, "?lessThan", "?attrToken1", 0),
		literal(SymbolicPredicates.parentOf, "?greaterThan", "?attrToken2", 0),
		literal(SymbolicPredicates.type, "?attrToken1", SymbolicType.ATTRIBUTE),
		literal(SymbolicPredicates.type, "?attrToken2", SymbolicType.ATTRIBUTE),
		literal(SymbolicPredicates.refsAttribute, "?attrToken1", "?entity", "?attribute"),
		literal(SymbolicPredicates.refsAttribute, "?attrToken2", "?entity", "?attribute"),
		literal(SymbolicPredicates.parentOf, "?lessThan", "?upperNumber", 1),
		literal(SymbolicPredicates.type, "?upperNumber", SymbolicType.NUMBER),
		literal(SymbolicPredicates.parentOf, "?greaterThan", "?lowerNumber", 1),
		literal(SymbolicPredicates.type, "?lowerNumber", SymbolicType.NUMBER),
		
		literal(SymbolicPredicates.parentOf, "?parent", "?lessThan", "?ltPos"),
		literal(SymbolicPredicates.parentOf, "?parent", "?greaterThan", "?gtPos")
	);

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
		          greaterThan = ext.getToken("?greaterThan"),
		             lessThan = ext.getToken("?lessThan");
		NumberToken lowerNum = ext.getToken("?lowerNumber"), 
		          upperNum = ext.getToken("?upperNumber");
		AttributeToken attrToken = ext.getToken("?attrToken1"); // both are equivalent
		
		
		BetweenToken between = new BetweenToken();
		between.addChild(attrToken);
		between.addChild(lowerNum);
		between.addChild(upperNum);
		
		_log.debug(Markers.SYMBOLIC, "Merging to {} from {} and {}", between, lessThan, greaterThan);
		
		if( !parent.getChildren().remove(greaterThan) )
			throw new SymbolicException("Failed to remove " + greaterThan);
		SymbolicUtil.replaceChild(parent, lessThan, between);
		
		return true;
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}

}
