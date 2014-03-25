package edu.gatech.sqltutor.rules.datalog.iris;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IConcreteTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;

import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.util.IObjectMapper;

/**
 * Extractor for terms of tuples in a relation that uses 
 * the variable name.
 * <p>
 * Note that the type inference is for convienence when the type is known.  
 * If the type is not correct, a <code>ClassCastException</code> will be thrown 
 * at the call site.
 * </p>
 * 
 * <p>
 * Example use:<br />
 * <code><pre>
 * RelationExtractor ext = new RelationExtractor(bindings);
 * ext.setNodeMap(...);
 * ext.setTokenMap(...);
 * ext.setCurrentTuple(...);
 * 
 * ITerm someVal = ext.getTerm("?var");
 * AttributeToken attrToken = ext.getToken("?attrToken"); // cast exception if not an AttributeToken
 * FromTable fromTable = ext.getNode("?fromTable"); // cast exception if not a FromTable
 * </pre></code>
 */
public class RelationExtractor {
	private IObjectMapper<ISymbolicToken> tokenMap;
	private IObjectMapper<QueryTreeNode> nodeMap;
	private Map<String,Integer> varMap;
	private ITuple currentTuple;
	private List<IVariable> originalBindings;
	
	public RelationExtractor(List<IVariable> bindings) {
		if( bindings == null )
			throw new NullPointerException("bindings is null");
		varMap = mapVariables(bindings);
		originalBindings = bindings;
	}
	
	private Map<String,Integer> mapVariables(List<IVariable> bindings) {
		int size = bindings.size();
		Map<String,Integer> map = new LinkedHashMap<String, Integer>((int)(size / 0.75f) + 1);
		for(int i = 0; i < size; ++i ) {
			IVariable var = bindings.get(i);
			Integer position = i;
			map.put(var.getValue(), position);
			map.put(var.toString(), position);
		}
		return map;
	}
	
	/**
	 * Returns the set of mapped variables.
	 * @return the set of mapped variables
	 */
	public List<IVariable> getVariables() {
		return originalBindings;
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
	 * Get the term mapped to a variable using the current result tuple.
	 * 
	 * @param var   the variable name
	 * @return the term
	 * @throws SQLTutorException if <code>var</code> is not bound 
	 *                           or there is no current tuple set
	 */	
	public ITerm getTerm(String var) {
		return getTerm(var, currentTupleOrThrow());
	}
	
	/**
	 * Convienence method when the term is known to be a query tree node.
	 * @throws SQLTutorException if no node map has been associated
	 *                           or the term does not identify a node
	 */
	@SuppressWarnings("unchecked")
	public <NodeType extends QueryTreeNode> NodeType getNode(String var, ITuple tuple) {
		if( nodeMap == null )
			throw new SQLTutorException("No node map associated.");
		return (NodeType)nodeMap.getMappedObject(getTerm(var, tuple));
	}
	
	/**
	 * Convienence method when the term is known to be a query tree node 
	 * and a current tuple is set.
	 * @throws SQLTutorException if no node map has been associated,
	 *                           the term does not identify a node,
	 *                           or no current tuple is set
	 */
	public <NodeType extends QueryTreeNode> NodeType getNode(String var) {
		return getNode(var, currentTupleOrThrow());
	}

	/**
	 * Convienence method when the term is known to be a symbolic token.
	 * @throws SQLTutorException if no token map has been associated
	 *                           or the term does not identify a node
	 */
	@SuppressWarnings("unchecked")
	public <TokenType extends ISymbolicToken> TokenType  getToken(String var, ITuple tuple) {
		if( tokenMap == null )
			throw new SQLTutorException("No token map associated.");
		return (TokenType)tokenMap.getMappedObject(getTerm(var, tuple));
	}
	
	/**
	 * Convienence method when the term is known to be a symbolic token 
	 * and a current tuple is set.
	 * @throws SQLTutorException if no token map has been associated,
	 *                           the term does not identify a node,
	 *                           or no current tuple is set
	 */	
	public <TokenType extends ISymbolicToken> TokenType getToken(String var) {
		return getToken(var, currentTupleOrThrow());
	}
	
	public String getString(String var, ITuple tuple) {
		ITerm term = getTerm(var, tuple);
		if( term instanceof IConcreteTerm )
			return ((IConcreteTerm)term).toCanonicalString();
		return term.toString();
	}
	
	public String getString(String var) {
		return getString(var, currentTupleOrThrow());
	}
	
	/**
	 * Sets the node map so that <code>getNode</code> can be used.
	 * @param nodeMap
	 */
	public void setNodeMap(IObjectMapper<QueryTreeNode> nodeMap) {
		this.nodeMap = nodeMap;
	}
	
	/**
	 * Sets the token map so that <code>getToken</code> can be used.
	 * @param tokenMap
	 */
	public void setTokenMap(IObjectMapper<ISymbolicToken> tokenMap) {
		this.tokenMap = tokenMap;
	}
	
	/**
	 * Sets the current tuple to use for method overloads that don't take one.
	 * @param currentTuple the default tuple to use
	 */
	public void setCurrentTuple(ITuple currentTuple) {
		this.currentTuple = currentTuple;
	}
	
	protected ITuple currentTupleOrThrow() {
		if( currentTuple == null )
			throw new SQLTutorException("No current tuple.");
		return currentTuple;
	}
}