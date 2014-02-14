package edu.gatech.sqltutor.rules.datalog.iris;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.factory.Factory;

/** Predicates for facts we will generate. */
public class SQLPredicates {
	/** <code>nodeHasType(?nodeId:int,?type:string)</code> => node <code>nodeId</code> has AST type </code>type</code>. */
	public static final IPredicate nodeHasType = create("sqlNodeHasType");

	/** <code>parentOf(?id1:int,?id2:int)</code> => node <code>id1</code> is the direct parent of node <code>id2</code>. */
	public static final IPredicate parentOf = create("sqlParentOf");

	/** <code>operator(?nodeId:int,?op:string)</code>. */
	public static final IPredicate operator = create("sqlOperator");

	/** <code>tableName(?nodeId:int, ?name:string)</code>. */
	public static final IPredicate tableName = create("sqlTableName");

	/** <code>tableAlias(?nodeId:int, ?alias:string)</code>. */
	public static final IPredicate tableAlias = create("sqlTableAlias");

	/** <code>columnName(?nodeId:int, ?name:string)</code>. */
	public static final IPredicate columnName = create("sqlColumnName");
	
	/** <code>(?node:int,?val:string|number)</code> => <code>node</code> is a constant with literal value <code>val</code>. */
	public static final IPredicate literalValue = create("sqlLiteralValue", 2); 
	
	/** For debugging only. */
	public static final IPredicate nodeDebugString = create("sqlNodeDebugString");
	
	private static IPredicate create(String symbol, int arity) {
		return Factory.BASIC.createPredicate(symbol, arity);
	}
	private static IPredicate create(String symbol) {
		return create(symbol, 2);
	}
}