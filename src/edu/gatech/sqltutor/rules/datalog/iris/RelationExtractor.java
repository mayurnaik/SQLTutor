package edu.gatech.sqltutor.rules.datalog.iris;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;

import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.SQLTutorException;

/**
 * Extractor for terms of tuples in a relation that uses 
 * the variable name.
 */
public class RelationExtractor {
	private SQLFacts sqlFacts;
	private Map<String,Integer> varMap;
	
	public RelationExtractor(List<IVariable> bindings) {
		if( bindings == null )
			throw new NullPointerException("bindings is null");
		varMap = mapVariables(bindings);
	}
	
	private Map<String,Integer> mapVariables(List<IVariable> bindings) {
		int size = bindings.size();
		Map<String,Integer> map = new HashMap<String, Integer>((int)(size / 0.75f) + 1);
		for(int i = 0; i < size; ++i ) {
			IVariable var = bindings.get(i);
			Integer position = i;
			map.put(var.getValue(), position);
			map.put(var.toString(), position);
		}
		return map;
	}
	
	/**
	 * Get the term mapped to a variable.
	 * 
	 * @param var   the variable name
	 * @param tuple the result tuple
	 * @return the term
	 * @throws SQLTutorException if <code>var</code> is not bound
	 */
	public ITerm getTerm(String var, ITuple tuple) {
		if( !varMap.containsKey(var) )
			throw new SQLTutorException("No binding for variable: " + var);
		return tuple.get(varMap.get(var));
	}
	
	/**
	 * Convienence method when the term is known to be a query tree node.
	 * @throws SQLTutorException if no <code>SQLFacts</code> has been associated
	 *                           or the term does not identify a node
	 * @throws IndexOutOfBoundsException if <code>pos</code> is out of range
	 */
	public QueryTreeNode getNode(String var, ITuple tuple) {
		if( sqlFacts == null )
			throw new SQLTutorException("No sql-facts associated.");
		return sqlFacts.getNode(getTerm(var, tuple));
	}
	
	public SQLFacts getSqlFacts() {
		return sqlFacts;
	}
	
	public void setSqlFacts(SQLFacts sqlFacts) {
		this.sqlFacts = sqlFacts;
	}
}