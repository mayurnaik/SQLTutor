package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
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
import edu.gatech.sqltutor.rules.symbolic.tokens.INounToken;

public class DefaultColumnLabelRule extends StandardSymbolicRule implements
		ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(DefaultColumnLabelRule.class);
	
	private static final StaticRules staticRules = new StaticRules(DefaultColumnLabelRule.class);
	
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleDefaultColumnLabel", 3);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?col", "?singular", "?plural"
	));

	public DefaultColumnLabelRule() {
		super(DefaultPrecedence.DESTRUCTIVE_UPDATE + 10);
	}

	public DefaultColumnLabelRule(int precedence) {
		super(precedence);
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		while( ext.nextTuple() ) {
			INounToken token = ext.getToken("?col");
			String singular = ext.getString("?singular"),
					plural = ext.getString("?plural");
			token.setSingularLabel(singular);
			token.setPluralLabel(plural);
			
			_log.info(Markers.METARULE, "Updated labels for token: {}", token);
		}
		return true;
	}

	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
}
