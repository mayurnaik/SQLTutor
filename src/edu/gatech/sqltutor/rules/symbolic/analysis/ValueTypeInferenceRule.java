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
import edu.gatech.sqltutor.rules.symbolic.ValueType;
import edu.gatech.sqltutor.rules.symbolic.tokens.IHasValueType;

public class ValueTypeInferenceRule extends StandardAnalysisRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(ValueTypeInferenceRule.class);
	
	private static final StaticRules staticRules = new StaticRules(ValueTypeInferenceRule.class);
	
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleValueTypeInference", 3);
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?binop", "?attrType", "?token")
	);

	public ValueTypeInferenceRule() {
	}

	public ValueTypeInferenceRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		final boolean debug = _log.isDebugEnabled(Markers.SYMBOLIC);
		
		boolean applied = false;
		while( ext.nextTuple() ) {
			ERAttributeDataType attrType = ERAttributeDataType.valueOf(ext.getString("?attrType"));
			IHasValueType binop = ext.getToken("?binop"), 
				token = ext.getToken("?token");
			ValueType valueType = null;
			switch( attrType ) {
			case DATETIME:
				valueType = ValueType.DATETIME;
				break;
			case DOLLARS:
				valueType = ValueType.DOLLARS;
				break;
			default:
				break;
			}
			if( valueType != null && setValueType(valueType, binop, token) ) {
				applied = true;
				if( debug ) _log.debug(Markers.SYMBOLIC, "Inferred {} type for {} and {}", valueType, binop, token);
			}
		}
		return applied;
	}

	private boolean setValueType(ValueType valueType, IHasValueType binop,
			IHasValueType token) {
		boolean changed = binop.getValueType() != valueType || token.getValueType() != valueType;
		if( changed ) {
			binop.setValueType(valueType);
			token.setValueType(valueType);
		}
		return changed;
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
