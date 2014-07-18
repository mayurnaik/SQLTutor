package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;
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
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.er.ERAttributeDataType;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.NumberToken.NumericType;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLNumberToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;

/**
 * If a numeric constant is compared with an attribute of a 
 * specific numeric data type, infer a more specific data type for 
 * the constant.
 */
public class NumberTypeInferenceRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(NumberTypeInferenceRule.class);
	
	private static final StaticRules staticRules = new StaticRules(NumberTypeInferenceRule.class);
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleNumberTypeInference", 3);
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?binop", "?attrType", "?numToken")
	);

	public NumberTypeInferenceRule() {
		super(DefaultPrecedence.FRAGMENT_ENHANCEMENT);
	}

	public NumberTypeInferenceRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {		
		boolean applied = false;
		while( ext.nextTuple() )
			applied |= handleNextResult(ext);
		return applied;
	}
	
	private boolean handleNextResult(RelationExtractor ext) {
		SQLToken binop = ext.getToken("?binop");
		SQLNumberToken numToken = ext.getToken("?numToken");
		
		ERAttributeDataType attrType = ERAttributeDataType.valueOf(ext.getString("?attrType"));
		switch( attrType ) {
		case DOLLARS: {
			NumericType numType = numToken.getNumericType();
			if( numType == NumericType.DOLLARS )
				return false;
			
			if( _log.isDebugEnabled(Markers.SYMBOLIC) ) {
				_log.debug(Markers.SYMBOLIC, "Inferred type {} for token {} by comparison in {}", 
					NumericType.DOLLARS, numToken, binop);
			}
			
			numToken.setNumericType(NumericType.DOLLARS);
			return true;
		}
		default:
			_log.warn(Markers.SYMBOLIC, "Unexpected attribute type {} in comparison {}", attrType, ext.getToken("?binop"));
			// fall through
		case NUMBER:
		case UNKNOWN:
			return false;
		}
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	protected int getVariableEstimate() {
		return PREDICATE.getArity();
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.SQL_ANALYSIS);
	}
}
