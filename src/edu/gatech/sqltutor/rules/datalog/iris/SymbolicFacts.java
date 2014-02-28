package edu.gatech.sqltutor.rules.datalog.iris;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.Deque;
import java.util.LinkedList;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.RootToken;
import edu.gatech.sqltutor.rules.util.ObjectMapper;

/** Fact generator for symbolic state. */
public class SymbolicFacts extends DynamicFacts {
	private static final Logger _log = LoggerFactory.getLogger(SymbolicFacts.class);
	
	public static class TokenMap extends ObjectMapper<ISymbolicToken> {
		@Override
		public void mapObjects(ISymbolicToken root) {
			if( !(root instanceof RootToken) )
				throw new SQLTutorException("Token should be the root token: " + root);
			mapTokens((RootToken)root);
		}
		
		/**
		 * Assigns ids to the symbolic token tree starting at <code>root</code>.
		 * @param root The distinguished root node.
		 */
		private void mapTokens(RootToken root) {
			clearMap();
			
			Deque<ISymbolicToken> worklist = new LinkedList<ISymbolicToken>();
			worklist.addFirst(root);
			
			while( !worklist.isEmpty() ) {
				ISymbolicToken token = worklist.removeFirst();
				mapObject(token);
				worklist.addAll(token.getChildren());
			}
		}
	}
	
	protected TokenMap tokenMap = new TokenMap();
	
	public SymbolicFacts() { }
	
	public void generateFacts(RootToken root, boolean preserveIds) {
		facts.clear();
		if( !preserveIds || tokenMap.size() < 1 )
			tokenMap.mapObjects(root);
		
		long duration = -System.currentTimeMillis();
		addFacts(root);
		_log.info("Symbolic facts generation took {} ms.", duration += System.currentTimeMillis());
	}
	
	@Override
	public void reset() {
		super.reset();
		tokenMap.clearMap();
	}
	
	public TokenMap getTokenMap() {
		return tokenMap;
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
		Integer childId = tokenMap.getObjectId(child);
		
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
		
		return tokenMap.getMappedObject(relation.get(0).get(0));
	}

	private void addFacts(RootToken root) {		
		Deque<ISymbolicToken> worklist = new LinkedList<ISymbolicToken>();
		worklist.addFirst(root);
		
		while( !worklist.isEmpty() ) {
			ISymbolicToken token = worklist.removeFirst();
			Integer tokenId = tokenMap.getObjectId(token);
			addLocalFacts(tokenId, token);
			
			int i = 0;
			for( ISymbolicToken child: token.getChildren() ) {
				Integer childId = tokenMap.getObjectId(child);
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

}
