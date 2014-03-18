package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.ERPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.er.ERAttributeDataType;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.NumberToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.NumberToken.NumericType;

/**
 * If a numeric constant is compared with an attribute of a 
 * specific numeric data type, infer a more specific data type for 
 * the constant.
 */
public class NumberTypeInferenceRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(NumberTypeInferenceRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		// {binop: {attribute}, {number}}
		literal(SymbolicPredicates.type, "?binop", SymbolicType.BINARY_COMPARISON),
		literal(SymbolicPredicates.parentOf, "?binop", "?attrToken", "?attrPos"),
		literal(SymbolicPredicates.type, "?attrToken", SymbolicType.ATTRIBUTE),
		literal(SymbolicPredicates.parentOf, "?binop", "?numToken", "?numPos"),
		literal(SymbolicPredicates.type, "?numToken", SymbolicType.NUMBER),
		
		// attribute has a datatype ?attrType
		literal(SymbolicPredicates.refsAttribute, "?attrToken", "?entity", "?attr"),
		literal(ERPredicates.erAttributeDataType, "?entity", "?attr", "?attrType"),
		
		// number has a datatype ?numType
		literal(SymbolicPredicates.numberType, "?numToken", "?numType")
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
		for( int i = 0, ilen = relation.size(); i < ilen; ++i ) {
			ext.setCurrentTuple(relation.get(i));
			applied |= handleResultTuple(ext);
		}
		return applied;
	}
	
	private boolean handleResultTuple(RelationExtractor ext) {
		try {
			ERAttributeDataType attrType = ERAttributeDataType.valueOf(ext.getString("?attrType"));
			switch( attrType ) {
				case DOLLARS: {
					NumericType numType = NumericType.valueOf(ext.getString("?numType"));
					if( numType == NumericType.DOLLARS )
						return false;
					NumberToken numToken = ext.getToken("?numToken");
					
					if( _log.isDebugEnabled(Markers.SYMBOLIC) ) {
						_log.debug(Markers.SYMBOLIC, "Inferred type {} for token {} by comparison to {}", 
							NumericType.DOLLARS, numToken, ext.getToken("?attrToken"));
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
		} catch( RuntimeException e ) {
			throw new SymbolicException(e);
		}
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	protected int getVariableEstimate() {
		return 10;
	}
}
