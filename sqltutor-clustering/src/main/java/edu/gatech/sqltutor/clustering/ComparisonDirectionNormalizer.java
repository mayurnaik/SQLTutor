/*
 *   Copyright (c) 2015 Program Analysis Group, Georgia Tech
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
package edu.gatech.sqltutor.clustering;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.BinaryRelationalOperatorNode;
import com.akiban.sql.parser.NodeFactory;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SQLParserContext;
import com.akiban.sql.parser.ValueNode;
import com.akiban.sql.parser.Visitor;

import edu.gatech.sqltutor.rules.util.ParserVisitorAdapter;

/**
 * 
 * @author Jake Cobb
 */
public class ComparisonDirectionNormalizer extends ParserVisitorAdapter
		implements Visitor {
	
	private SQLParserContext context;
	private NodeFactory nodeFactory;

	public ComparisonDirectionNormalizer(SQLParserContext context) {
		if( context == null ) throw new NullPointerException("context is null");
		this.context = context;
		this.nodeFactory = context.getNodeFactory();
	}

	@Override
	public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
		switch( node.getNodeType() ) {
		case NodeTypes.BINARY_GREATER_EQUALS_OPERATOR_NODE:
		case NodeTypes.BINARY_GREATER_THAN_OPERATOR_NODE: {
			BinaryRelationalOperatorNode binop = (BinaryRelationalOperatorNode)node;
			ValueNode oldLeft = binop.getLeftOperand(),
					oldRight = binop.getRightOperand();
			
			int newNodeType;
			if (node.getNodeType() == NodeTypes.BINARY_GREATER_EQUALS_OPERATOR_NODE)
				newNodeType = NodeTypes.BINARY_LESS_EQUALS_OPERATOR_NODE;
			else // BINARY_GREATER_THAN_OPERATOR_NODE
				newNodeType = NodeTypes.BINARY_LESS_THAN_OPERATOR_NODE;
			
			QueryTreeNode newNode = nodeFactory.getNode(newNodeType, oldRight, oldLeft, context);
			return newNode;
		}
		}
		return node;
	}
}
