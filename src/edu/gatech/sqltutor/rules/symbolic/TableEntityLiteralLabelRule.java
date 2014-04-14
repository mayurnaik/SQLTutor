package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.List;
import java.util.Random;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.concrete.IIntegerTerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;

public class TableEntityLiteralLabelRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(TableEntityLiteralLabelRule.class);
	private static final StaticRules staticRules = new StaticRules(TableEntityLiteralLabelRule.class);
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleTableEntityLiteralLabel", 6);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?parent","?token","?pos","?table","?label","?source")
	);
	
	private Random random = new Random();
	
	public TableEntityLiteralLabelRule() {
		super(DefaultPrecedence.LOWERING);
	}
	
	public TableEntityLiteralLabelRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		// FIXME need way to ensure all choices will be used eventually
		int choices = countChoicesForTable(relation, ext);
		ITuple result = relation.get(random.nextInt(choices));
		
		String label = ((IStringTerm)ext.getTerm("?label", result)).getValue();
		ISymbolicToken token = ext.getToken("?token", result);
		ISymbolicToken parent = ext.getToken("?parent", result);
		// FIXME what about multi-word labels like "Research Department"?
		LiteralToken literal = new LiteralToken(label, token.getPartOfSpeech());
		
		SymbolicUtil.replaceChild(parent, token, literal);
		_log.info(Markers.SYMBOLIC, "Replaced token {} with {}", token, literal);
		return true;
	}
	
	public int countChoicesForTable(IRelation results, RelationExtractor ext) {
		if( results.size() < 1 )
			return 0;
		
		int lastTable = -1;
		int i, ilen;
		for( i = 0, ilen = results.size(); i < ilen; ++i ) {
			ITuple result = results.get(i);
			Integer tableId = ((IIntegerTerm)ext.getTerm("?table", result)).getValue().intValueExact();
			if( lastTable == -1 )
				lastTable = tableId;
			else if( lastTable != tableId )
				break;
		}
		
		return i;
	}
	
	@Override
	protected IQuery getQuery() { return QUERY; }
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
	
	@Override
	protected int getVariableEstimate() {
		return PREDICATE.getArity();
	}
}
