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

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.List;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
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

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.util.GetChildrenVisitor;
import edu.gatech.sqltutor.rules.util.ObjectMapper;
import edu.gatech.sqltutor.rules.util.ParserVisitorAdapter;

/**
 * Dynamic SQL AST facts.
 */
public class SQLFacts extends DynamicFacts {
	private static final Logger _log = LoggerFactory.getLogger(SQLFacts.class);
	
	/** A mapping of ids to query tree nodes. */
	public static class NodeMap extends ObjectMapper<QueryTreeNode> {
		@Override
		public void mapObjects(QueryTreeNode root) {
			if( !(root instanceof SelectNode) )
				throw new SQLTutorException("Root node should be select node.");
			clearMap();
			try {
				// assign ids to all nodes, from the top down
				root.accept(new ParserVisitorAdapter() {
					@Override
					public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
						mapObject(node);
						return node;
					}
				});
			} catch( StandardException e ) {
				throw new SQLTutorException(e);
			}
		}
		
		@Override
		protected String objectToString(QueryTreeNode obj) {
			if( obj == null ) return "null";
			return QueryUtils.nodeToString(obj);
		}
	}
	
	/** Map for AST nodes. */
	protected NodeMap nodeMap = new NodeMap();
	
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
		if( !preserveIds || nodeMap.size() < 1 )
			nodeMap.mapObjects(select);
		
		long duration = -System.currentTimeMillis();
		addFacts(select);
		_log.debug(Markers.TIMERS_FINE, "SQL fact generation took {} ms.", duration += System.currentTimeMillis());
	}
	
	@Override
	public void reset() {
		super.reset();
		nodeMap.clearMap();
	}
	
	public NodeMap getNodeMap() { return nodeMap; }
	
	
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
		Integer childId = nodeMap.getObjectId(child);
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
		
		return nodeMap.getMappedObject(relation.get(0).get(0));
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
					Integer nodeId = nodeMap.getObjectId(node);
					addLocalFacts(node);
					for( QueryTreeNode child: getChildren(node) ) {
						Integer childId = nodeMap.getObjectId(child);
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
		Integer nodeId = nodeMap.getObjectId(node);
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
