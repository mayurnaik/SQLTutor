package edu.gatech.sqltutor.rules.datalog.iris;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IConcreteTerm;
import org.deri.iris.api.terms.INumericTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.storage.IRelation;

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
 * 
 * Or you can have it hold the relation internally:
 * <code><pre>
 * RelationExtractor ext = new RelationExtractor(relation, bindings);
 * // same as above
 * while( ext.nextTuple() ) {
 *  // same as above
 * }
 */
public class RelationExtractor {
	/** Optional map for symbolic tokens. */
	private IObjectMapper<ISymbolicToken> tokenMap;
	/** Optional map for SQL AST nodes. */
	private IObjectMapper<QueryTreeNode> nodeMap;
	/** Map from variable name to position. */
	private Map<String,Integer> varMap;
	/** Original variable bindings. */
	private List<IVariable> originalBindings;
	
	private int position = -1;
	private IRelation relation;
	private ITuple currentTuple;
	
	public RelationExtractor(List<IVariable> bindings) {
		this(null, bindings);
	}
	
	public RelationExtractor(IRelation relation, List<IVariable> bindings) {
		if( bindings == null )
			throw new NullPointerException("bindings is null");
		this.relation = relation;
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
	
	public Integer getInteger(String var, ITuple tuple) {
		ITerm term = getTerm(var, tuple);
		if( term instanceof INumericTerm )
			return ((INumericTerm)term).getValue().intValueExact();
		throw new SQLTutorException(var + " is not int type: " + term);
	}
	
	public Integer getInteger(String var) {
		return getInteger(var, currentTupleOrThrow());
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
	
	public IRelation getRelation() {
		return relation;
	}
	
	public void setRelation(IRelation relation) {
		this.relation = relation;
		this.position = -1;
	}
	
	public int getCurrentPosition() {
		if( relation == null )
			throw new IllegalStateException("No relation is associated.");
		return this.position;
	}
	
	public void setPosition(int position) {
		if( relation == null )
			throw new IllegalStateException("No relation is associated.");
		if( position < 0 || position >= relation.size() )
			throw new IllegalArgumentException("Position out of range: " + position);
		this.position = position - 1;
		nextTuple();
	}
	
	/**
	 * Advance the current position and set that as the current tuple.
	 * @return <code>true</code> if there was another tuple
	 * @throws IllegalStateException if no relation has been associated
	 */
	public boolean nextTuple() {
		if( relation == null )
			throw new IllegalStateException("No relation is associated.");
		if( position == relation.size() )
			return false;
		ITuple tuple = relation.get(++position);
		setCurrentTuple(tuple);
		return true;
	}
}