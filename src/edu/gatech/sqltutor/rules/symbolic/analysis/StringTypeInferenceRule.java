package edu.gatech.sqltutor.rules.symbolic.analysis;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.er.ERAttributeDataType;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLStringToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLStringToken.StringType;

public class StringTypeInferenceRule extends StandardAnalysisRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(StringTypeInferenceRule.class);
	
	private static final StaticRules staticRules = new StaticRules(StringTypeInferenceRule.class);
	
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleStringTypeInference", 3);
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?binop", "?attrType", "?token")
	);

	public StringTypeInferenceRule() {
	}

	public StringTypeInferenceRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		final boolean debug = _log.isDebugEnabled(Markers.SYMBOLIC);
		
		boolean applied = false;
		while( ext.nextTuple() ) {
			ERAttributeDataType attrType = ERAttributeDataType.valueOf(ext.getString("?attrType"));
			SQLStringToken token = ext.getToken("?token");
			StringType stringType = token.getStringType();
			if( stringType == StringType.STRING ) {
				switch( attrType ) {
				case DATETIME:
					token.setStringType(StringType.DATETIME);
					applied = true;
					if( debug ) _log.debug(Markers.SYMBOLIC, "Inferred string type for {}", token);
					break;
				default:
					break;
				}
			}
		}
		return applied;
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}

	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
}
