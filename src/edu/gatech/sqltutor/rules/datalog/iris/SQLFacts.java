package edu.gatech.sqltutor.rules.datalog.iris;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

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

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.ConstantNode;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SelectNode;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.util.GetChildrenVisitor;
import edu.gatech.sqltutor.rules.util.ParserVisitorAdapter;

/**
 * Dynamic SQL AST facts.
 */
public class SQLFacts extends DynamicFacts {
	private static final Logger _log = LoggerFactory.getLogger(SQLFacts.class);
	
	/** IDs assigned to AST nodes. */
	BiMap<Integer, QueryTreeNode> nodeIds = HashBiMap.create();
	
	public SQLFacts() {}

	/**
	 * (Re)generate facts for the given AST.
	 * <p>
	 * If the AST has been modified but no new nodes exist, 
	 * use <code>preserveIds</code> to keep the old node ids.</p> 
	 * 
	 * @param select      the <code>SELECT</code> statement root of the AST
	 * @param preserveIds if previously generated node ids should be preserved
	 */
	public void generateFacts(SelectNode select, boolean preserveIds) {
		facts.clear();
		if( !preserveIds || nodeIds.isEmpty() )
			mapNodes(select);
		
		long duration = -System.currentTimeMillis();
		addFacts(select);
		_log.info("SQL fact generation took {} ms.", duration += System.currentTimeMillis());
	}
	
	@Override
	public void reset() {
		super.reset();
		nodeIds.clear();
	}

	/**
	 * Gets the ID assigned to <code>node</code>.
	 * 
	 * @param node the node
	 * @return the id, which is never <code>null</code>
	 * @throws SQLTutorException if there is no id for the node
	 */
	public Integer getNodeId(QueryTreeNode node) {
		if( node == null )
			throw new NullPointerException("node is null");
		Integer id = nodeIds.inverse().get(node);
		if( id == null )
			throw new SQLTutorException("No id mapped to node: " + node);
		return id;
	}
	
	/**
	 * Gets the node referenced by <code>id</code>.
	 * 
	 * @param id the node id
	 * @return the node, which is never <code>null</code>
	 * @throws SQLTutorException if there is no node mapped to the id
	 */
	public QueryTreeNode getNode(Integer id) {
		if( id == null )
			throw new NullPointerException("id is null");
		QueryTreeNode node = nodeIds.get(id);
		if( node == null )
			throw new SQLTutorException("No node with id: " + id);
		return node;
	}
	
	/**
	 * Gets the node referenced by <code>id</code>.  
	 * <code>id</code> must be an integer term.
	 * 
	 * @param id the term containing the id
	 * @return the node, which is never <code>null</code>
	 * @throws SQLTutorException if there is no node mapped to the id or the term is the wrong type
	 */
	public QueryTreeNode getNode(ITerm id) {
		try {
			return getNode(((IIntegerTerm)id).getValue().intValueExact());
		} catch ( ClassCastException e ) {
			throw new SQLTutorException("Term is not an integer.", e);
		} catch( ArithmeticException e ) {
			throw new SQLTutorException("Term is not an integer or is too large.", e);
		}
	}
	
	/**
	 * Get the parent of <code>child</code>, evaluated using the knowledge base.
	 * 
	 * @param child the child node
	 * @param kb    the datalog knowledge base
	 * @return the parent node or <code>null</code> if the node has no parent
	 * @throws SQLTutorException if <code>child</code> is not mapped, 
	 *                           the query fails to evaluate,
	 *                           or the parent is not unique
	 */
	public QueryTreeNode getParent(QueryTreeNode child, IKnowledgeBase kb) {
		Integer childId = nodeIds.inverse().get(child);
		if( childId == null )
			throw new SQLTutorException("No id mapped to child: " + QueryUtils.nodeToString(child));
		
		IQuery q = Factory.BASIC.createQuery(
			literal(SQLPredicates.parentOf, "?parentId", childId)
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
		
		return getNode(relation.get(0).get(0));
	}
	
	private void mapNodes(SelectNode select) {
		nodeIds.clear();
		try {
			// assign ids to all nodes, from the top down
			select.accept(new ParserVisitorAdapter() {
				int nextId = 0;
				@Override
				public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
					nodeIds.put(nextId++, node);
					return node;
				}
			});
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
	}
	
	private void addFacts(QueryTreeNode node) {
		try {
			node.accept(new ParserVisitorAdapter() {
				GetChildrenVisitor childVisitor = new GetChildrenVisitor();
				
				List<QueryTreeNode> getChildren(QueryTreeNode node) throws StandardException {
					childVisitor.reset();
					node.accept(childVisitor);
					return childVisitor.getChildren();
				}
				
				@Override
				public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
					Integer nodeId = nodeIds.inverse().get(node);
					addLocalFacts(node);
					for( QueryTreeNode child: getChildren(node) ) {
						Integer childId = nodeIds.inverse().get(child);
						addFact(SQLPredicates.parentOf, nodeId, childId);
					}
					return node;
				}
			});
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
	}
	
	private void addLocalFacts(QueryTreeNode node) {
		Integer nodeId = nodeIds.inverse().get(node);
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
