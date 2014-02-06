package edu.gatech.sqltutor.rules.datalog.iris;

import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.SQLTutorException;

/** Static util functions for the IRIS reasoner. */
public class IrisUtil {
	private static final Logger _log = LoggerFactory.getLogger(IrisUtil.class);
	private static final IRelationFactory relationFactory = new SimpleRelationFactory();

	/**
	 * Attempt to convert a value to an equivalent <code>ITerm</code>.
	 *  
	 * @param val the value to convert
	 * @return the equivalent term
	 */
	public static ITerm asTerm(Object val) {
		if( val instanceof ITerm )
			return (ITerm)val;
		
		if( val instanceof Number ) {
			String cname = val.getClass().getName();
			Number num = (Number)val;
			if( cname.contains("Integer") )
				return Factory.CONCRETE.createInt(num.intValue());
			if( cname.contains("Long") )
				return Factory.CONCRETE.createLong(num.longValue());
			if( cname.contains("Float") )
				return Factory.CONCRETE.createFloat(num.floatValue());
			if( cname.contains("Double") )
				return Factory.CONCRETE.createDouble(num.doubleValue());
			throw new SQLTutorException("Unhandled numeric type: " + cname);
		}
		
		String strVal = val.toString();
		if( !(val instanceof String) ) {
			_log.warn("Unknown type, using string representation: " + val.getClass().getName());
		}
		if( strVal.startsWith("?") )
			return Factory.TERM.createVariable(strVal.substring(1));
		return Factory.TERM.createString(strVal);
	}

	/**
	 * Convert each value to an equivalent term.
	 * 
	 * @param vals the values to convert
	 * @return the equivalent terms
	 */
	public static ITerm[] asTerms(Object... vals) {
		if( vals instanceof ITerm[] )
			return (ITerm[])vals;
		ITerm[] terms = new ITerm[vals.length];
		for( int i = 0; i < vals.length; i++ )
			terms[i] = asTerm(vals[i]);
		return terms;
	}

	/**
	 * Convert values to a tuple.
	 * @param vals the values to convert
	 * @return
	 */
	public static ITuple asTuple(Object... vals) {
		if( vals.length == 1 && vals[0] instanceof ITuple )
			return (ITuple)vals[0];
		return Factory.BASIC.createTuple(asTerms(vals));
	}
	
	public static ILiteral literal(IAtom atom) { return literal(true, atom); }
	public static ILiteral literal(boolean isPositive, IAtom atom) {
		return Factory.BASIC.createLiteral(isPositive, atom);
	}
	
	public static ILiteral literal(boolean isPositive, IPredicate pred, Object... vals) {
		return Factory.BASIC.createLiteral(isPositive, pred, asTuple(vals));
	}
	
	public static ILiteral literal(IPredicate pred, Object... vals) {
		return literal(true, pred, vals);
	}
	
	public static IRelation relation() { return relationFactory.createRelation(); }
	
	public static IPredicate predicate(String symbol, int arity) {
		return Factory.BASIC.createPredicate(symbol, arity);
	}
}
