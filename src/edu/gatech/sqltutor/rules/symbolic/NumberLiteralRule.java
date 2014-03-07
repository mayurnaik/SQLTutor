package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.text.NumberFormat;
import java.util.Locale;

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
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.NumberToken;

public class NumberLiteralRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(NumberLiteralRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.parentOf, "?parent", "?token", "?pos"),
		literal(SymbolicPredicates.type, "?token", SymbolicType.NUMBER)
	);

	public NumberLiteralRule() {
		super(DefaultPrecedence.LOWERING);
	}
	
	public NumberLiteralRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		ITuple first = relation.get(0);
		ISymbolicToken parent = ext.getToken("?parent", first);
		NumberToken numberToken = (NumberToken)ext.getToken("?token", first);
		NumberFormat numberFormat;
		switch( numberToken.getNumericType() ) {
			case DOLLARS:
				numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
				break;
			default:
				_log.warn("Unhandled numeric type in token: {}", numberToken);
				// fall-through
			case GENERAL:
				numberFormat = NumberFormat.getNumberInstance(Locale.US);
				break;
		}
		String expression = numberFormat.format(numberToken.getNumber());
		LiteralToken literal = new LiteralToken(expression, numberToken.getPartOfSpeech());
		
		SymbolicUtil.replaceChild(parent, numberToken, literal);
		_log.debug(Markers.SYMBOLIC, "Replaced {} with {}", numberToken, literal);
		return true;
	}
	
	@Override
	protected IQuery getQuery() { return QUERY; }
}
