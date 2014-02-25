package edu.gatech.sqltutor.rules.datalog.iris;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.concrete.IIntegerTerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.symbolic.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.RootToken;

/** Fact generator for symbolic state. */
public class SymbolicFacts extends DynamicFacts {
	private static final Logger _log = LoggerFactory.getLogger(SymbolicFacts.class);
	
	/** IDs assigned to symbolic tokens. */
	BiMap<Integer, ISymbolicToken> tokenIds = HashBiMap.create();
	
	public SymbolicFacts() { }
	
	public void generateFacts(RootToken root, boolean preserveIds) {
		facts.clear();
		if( !preserveIds || tokenIds.isEmpty() )
			mapTokens(root);
		
		long duration = -System.currentTimeMillis();
		addFacts(root);
		_log.info("Symbolic facts generation took {} ms.", duration += System.currentTimeMillis());
	}
	
	@Override
	public void reset() {
		super.reset();
		tokenIds.clear();
	}
	
	/**
	 * Returns the token mapped to <code>id</code>.
	 * @param id the token id
	 * @return the token
	 * @throws SQLTutorException if the id is not mapped
	 */
	public ISymbolicToken getToken(Integer id) {
		ISymbolicToken token = tokenIds.get(id);
		if( token == null )
			throw new SQLTutorException("No token mapped to id: " + id);
		return token;
	}
	
	/**
	 * Returns the id of a mapped token.
	 * @param token the token
	 * @return the token's id
	 * @throws SQLTutorException if the token is not mapped
	 */
	public Integer getTokenId(ISymbolicToken token) { 
		Integer id = tokenIds.inverse().get(token);
		if( id == null )
			throw new SQLTutorException("Token is not mapped to an id: " + token);
		return id;
	}
	
	/**
	 * Gets the token referenced by <code>id</code>.  
	 * <code>id</code> must be an integer term.
	 * 
	 * @param id the term containing the id
	 * @return the token, which is never <code>null</code>
	 * @throws SQLTutorException if there is no token mapped to the id or the term is the wrong type
	 */
	public ISymbolicToken getToken(ITerm id) {
		try {
			return getToken(((IIntegerTerm)id).getValue().intValueExact());
		} catch ( ClassCastException e ) {
			throw new SQLTutorException("Term is not an integer.", e);
		} catch( ArithmeticException e ) {
			throw new SQLTutorException("Term is not an integer or is too large.", e);
		}
	}
	
	/**
	 * Get the parent of <code>child</code>, evaluated using the knowledge base.
	 * 
	 * @param child the child token
	 * @param kb    the datalog knowledge base
	 * @return the parent token or <code>null</code> if the token has no parent
	 * @throws SQLTutorException if <code>child</code> is not mapped, 
	 *                           the query fails to evaluate,
	 *                           or the parent is not unique
	 */
	public ISymbolicToken getParent(ISymbolicToken child, IKnowledgeBase kb) {
		Integer childId = getTokenId(child);
		
		IQuery q = Factory.BASIC.createQuery(
			literal(SymbolicPredicates.parentOf, "?parentId", childId)
		);
		IRelation relation = null;
		try {
			relation = kb.execute(q);
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
		
		if( relation.size() == 0 )
			return null;
		if( relation.size() > 1 )
			throw new SQLTutorException("Non-unique parent, found " + relation.size() + " nodes.");
		
		return getToken(relation.get(0).get(0));
	}

	private void addFacts(RootToken root) {		
		Deque<ISymbolicToken> worklist = new LinkedList<ISymbolicToken>();
		worklist.addFirst(root);
		
		while( !worklist.isEmpty() ) {
			ISymbolicToken token = worklist.removeFirst();
			Integer tokenId = getTokenId(token);
			addLocalFacts(tokenId, token);
			
			int i = 0;
			for( ISymbolicToken child: token.getChildren() ) {
				Integer childId = getTokenId(child);
				addFact(SymbolicPredicates.parentOf, tokenId, childId, i++);
				worklist.addLast(child);
			}
		}
	}

	private void addLocalFacts(Integer tokenId, ISymbolicToken token) {
		addFact(SymbolicPredicates.partOfSpeech, tokenId, token.getPartOfSpeech().getTag());
		addFact(SymbolicPredicates.type, tokenId, token.getType());
		
		if( _log.isDebugEnabled() ) {
			addFact(SymbolicPredicates.debugString, tokenId, token.toString());
		}
	}

	/**
	 * Assigns ids to the symbolic token tree starting at <code>root</code>.
	 * @param root The distinguished root node.
	 */
	private void mapTokens(RootToken root) {
		tokenIds.clear();
		
		Deque<ISymbolicToken> worklist = new LinkedList<ISymbolicToken>();
		worklist.addFirst(root);
		
		int nextId = 0;
		while( !worklist.isEmpty() ) {
			ISymbolicToken token = worklist.removeFirst();
			tokenIds.put(nextId++, root);
			
			List<ISymbolicToken> children = token.getChildren();
			for( ISymbolicToken child: children )
				worklist.addLast(child);
		}
	}

}
