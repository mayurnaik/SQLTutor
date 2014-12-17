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
