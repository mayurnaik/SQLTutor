/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
	
	// defined statically
	/** <code>(?colId:int,?tableAlias:string,?tableName:string,?colName:string)</code> */
	public static final IPredicate columnInfo = create("sqlColumnInfo", 4);
	
	private static IPredicate create(String symbol, int arity) {
		return Factory.BASIC.createPredicate(symbol, arity);
	}
	private static IPredicate create(String symbol) {
		return create(symbol, 2);
	}
}