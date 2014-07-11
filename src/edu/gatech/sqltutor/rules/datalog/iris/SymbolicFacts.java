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

import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.ConstantNode;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.QueryTreeNode;
import com.google.common.base.Joiner;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.BinaryComparisonToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.INounToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.NumberToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.RootToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLNounToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.util.IObjectMapper;
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
	protected IObjectMapper<QueryTreeNode> nodeMap;
	
	public SymbolicFacts() { }
	
	public void generateFacts(RootToken root, boolean preserveIds) {
		facts.clear();
		if( !preserveIds || tokenMap.size() < 1 )
			tokenMap.mapObjects(root);
		
		long duration = -System.currentTimeMillis();
		addFacts(root);
		_log.debug(Markers.TIMERS_FINE, "Symbolic facts generation took {} ms.", duration += System.currentTimeMillis());
	}
	
	@Override
	public void reset() {
		super.reset();
		tokenMap.clearMap();
	}
	
	public TokenMap getTokenMap() {
		return tokenMap;
	}
	
	public IObjectMapper<QueryTreeNode> getNodeMap() {
		return nodeMap;
	}
	
	public void setNodeMap(IObjectMapper<QueryTreeNode> nodeMap) {
		this.nodeMap = nodeMap;
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
	 * @deprecated Use {@link ISymbolicToken#getParent()} instead.
	 */
	@Deprecated
	public ISymbolicToken getParent(ISymbolicToken child, IKnowledgeBase kb) {
		Integer childId = tokenMap.getObjectId(child);
		
		IQuery q = Factory.BASIC.createQuery(
			literal(SymbolicPredicates.parentOf, "?parentId", childId, "?pos")
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
			final boolean isParentAST = token instanceof SQLToken;
			for( ISymbolicToken child: token.getChildren() ) {
				Integer childId = tokenMap.getObjectId(child);
				// TODO sql phase unified with symbolic, generate sql facts to avoid updating datalog for now
				if( isParentAST && child instanceof SQLToken )
					addFact(SQLPredicates.parentOf, tokenId, childId);
				addFact(SymbolicPredicates.parentOf, tokenId, childId, i++);
				worklist.addLast(child);
			}
		}
	}

	private void addLocalFacts(Integer tokenId, ISymbolicToken token) {
		addFact(SymbolicPredicates.partOfSpeech, tokenId, token.getPartOfSpeech().getTag());
		SymbolicType tokenType = token.getType();
		addFact(SymbolicPredicates.type, tokenId, tokenType);
		addFact(SymbolicPredicates.provenance, tokenId, 
			Joiner.on('|').join(token.getProvenance()));
		
		if( token instanceof INounToken )
			addNounFacts(tokenId, (INounToken)token);
		
		switch( tokenType ) {
			case ATTRIBUTE: 
				addAttributeFacts(tokenId, (AttributeToken)token); 
				break;
			case TABLE_ENTITY: 
				addTableEntityFacts(tokenId, (TableEntityToken)token); 
				break;
			case NUMBER: 
				addNumberFacts(tokenId, (NumberToken)token); 
				break;
			case BINARY_COMPARISON: 
				addBinaryComparisonFacts(tokenId, (BinaryComparisonToken)token); 
				break;
			case LITERAL:
				addLiteralFacts(tokenId, (LiteralToken)token);
				break;
			case SQL_AST:
				// unification of sql/symbolic phases
				addSQLFacts(tokenId, (SQLToken)token);
				break;
			default: break;
		}
		
		if( _log.isDebugEnabled() ) {
			addFact(SymbolicPredicates.debugString, tokenId, token.toString());
		}
	}
	
	private void addNounFacts(Integer tokenId, INounToken token) {
		String singular = token.getSingularLabel();
		String plural = token.getPluralLabel();
		
		if( singular == null ) singular = "";
		if( plural == null ) plural = "";
		addFact(SymbolicPredicates.singularLabel, tokenId, singular);
		addFact(SymbolicPredicates.pluralLabel, tokenId, plural);
	}

	private void addLiteralFacts(Integer tokenId, LiteralToken token) {
		addFact(SymbolicPredicates.literalExpression, tokenId, token.getExpression());
	}

	private void addAttributeFacts(Integer tokenId, AttributeToken token) {
		ERAttribute attr = token.getAttribute();
		if( attr == null )
			throw new NullPointerException("No attr for token: " + token);
		String[] parts = attr.getFullName().split("\\.");
		addFact(SymbolicPredicates.refsAttribute, tokenId, parts[0], parts[parts.length-1]);
	}
	
	private void addTableEntityFacts(Integer tokenId, TableEntityToken token) {
		Integer tableId = nodeMap.getObjectId(token.getTable());
		addFact(SymbolicPredicates.refsTable, tokenId, tableId);
	}
	
	private void addNumberFacts(Integer tokenId, NumberToken token) {
		addFact(SymbolicPredicates.number, tokenId, token.getNumber());
		addFact(SymbolicPredicates.numberType, tokenId, token.getNumericType());
	}
	
	private void addBinaryComparisonFacts(Integer tokenId, BinaryComparisonToken token) {
		addFact(SymbolicPredicates.binaryOperator, tokenId, token.getOperator());
	}
	
	// FIXME migrated from SQLFacts for sql/symbolic phase unification
	private void addSQLFacts(Integer nodeId, SQLToken token) {
		QueryTreeNode node = token.getAstNode();
		String nodeType = node.getClass().getName().replaceAll("^.*\\.", "");
		addFact(SQLPredicates.nodeHasType, nodeId, nodeType);
		
		if( node instanceof ColumnReference )
			addColumnReferenceFacts(nodeId, (ColumnReference)node);
		if( node instanceof BinaryOperatorNode )
			addBinopFacts(nodeId, (BinaryOperatorNode)node);
		if( node instanceof FromBaseTable )
			addTableFacts(nodeId, (FromBaseTable)node);
		if( node instanceof ConstantNode )
			addConstantFacts(nodeId, (ConstantNode)node);
		
		if( _log.isDebugEnabled() ) {
			// gen facts to make debugging easier
			addFact(SQLPredicates.nodeDebugString, nodeId, QueryUtils.nodeToString(node));
		}
	}
	
	private void addBinopFacts(Integer nodeId, BinaryOperatorNode binop) {
		String op = binop.getOperator();
		addFact(SQLPredicates.operator, nodeId, op);
	}
	
	private void addTableFacts(Integer nodeId, FromBaseTable table) {
		addFact(SQLPredicates.tableName, nodeId, table.getOrigTableName().getTableName());
		addFact(SQLPredicates.tableAlias, nodeId, table.getExposedName());
	}
	
	private void addColumnReferenceFacts(Integer nodeId, ColumnReference col) {
		addFact(SQLPredicates.tableAlias, nodeId, col.getTableName());
		addFact(SQLPredicates.columnName, nodeId, col.getColumnName());
	}
	
	private void addConstantFacts(Integer nodeId, ConstantNode constant) {
		addFact(SQLPredicates.literalValue, nodeId, constant.getValue());
	}
}
