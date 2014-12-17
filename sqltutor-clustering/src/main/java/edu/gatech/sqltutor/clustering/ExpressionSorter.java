package edu.gatech.sqltutor.clustering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.SQLParserContext;
import com.akiban.sql.parser.ValueNode;
import com.akiban.sql.parser.Visitable;
import com.akiban.sql.parser.Visitor;

import edu.gatech.sqltutor.QueryUtils;

public class ExpressionSorter extends ContextVisitorAdapter implements Visitor {
	private static final Logger _log = LoggerFactory.getLogger(ExpressionSorter.class);

	public ExpressionSorter(SQLParserContext context) {
		super(context);
	}

	@Override
	public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
		switch( node.getNodeType() ) {
		case NodeTypes.BINARY_EQUALS_OPERATOR_NODE:
		case NodeTypes.BINARY_NOT_EQUALS_OPERATOR_NODE: 
		case NodeTypes.BINARY_PLUS_OPERATOR_NODE:
		case NodeTypes.BINARY_TIMES_OPERATOR_NODE: {
			BinaryOperatorNode binop = (BinaryOperatorNode)node;
			ValueNode oldLeft = binop.getLeftOperand(),
			         oldRight = binop.getRightOperand();
			String leftSide = getComparisonString(oldLeft),
			      rightSide = getComparisonString(oldRight);
			if( leftSide.compareTo(rightSide) > 0 ) {
				binop.setLeftOperand(oldRight);
				binop.setRightOperand(oldLeft);
			}
			break;
		}
		}
		return node;
	}
	
	
	@Override
	public boolean visitChildrenFirst(Visitable node) {
		return true;
	}
	
	private String getComparisonString(QueryTreeNode node) {
		switch( node.getNodeType() ) {
		case NodeTypes.COLUMN_REFERENCE: {
			ColumnReference ref = (ColumnReference)node;
			ResultColumn resultColumn = ref.getSourceResultColumn();
			String sqlColumnName = ref.getSQLColumnName();
			_log.info("\nsqlColumnName: {}\nresultColumn: {}", sqlColumnName, resultColumn);
			return sqlColumnName;
		}
		}
		return QueryUtils.nodeToString(node);
//		throw new SQLTutorException("Don't know how to handle node: " + QueryUtils.nodeToString(node));
	}

}
