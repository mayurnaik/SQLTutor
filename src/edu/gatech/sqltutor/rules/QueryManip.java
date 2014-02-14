package edu.gatech.sqltutor.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.ValueNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;

/** Static utility functions for manipulating the AST. */
public class QueryManip {
	private static final Logger _log = LoggerFactory.getLogger(QueryManip.class);
	
	/**
	 * Delete the given condition from the AST.
	 * 
	 * @param state the current SQL state
	 * @param binop the operator condition to delete
	 */
	public static void deleteCondition(SQLState state, BinaryOperatorNode binop) {
		QueryTreeNode parent = QueryUtils.findParent(state.getAst(), binop);
		if( _log.isTraceEnabled() ) {
			_log.trace("Deleting condition \"{}\" in parent \"{}\"", 
				QueryUtils.nodeToString(binop), QueryUtils.nodeToString(parent));
		}
		
		if( parent instanceof BinaryOperatorNode ) {
			BinaryOperatorNode parentOp = (BinaryOperatorNode)parent;
			if( binop == parentOp.getLeftOperand() ) {
				replaceParent(state, parentOp, parentOp.getRightOperand());
			} else {
				replaceParent(state, parentOp, parentOp.getLeftOperand());
			}
		} else if( parent instanceof SelectNode ) {
			_log.debug("Deleting WHERE clause.");
			((SelectNode)parent).setWhereClause(null);
		} else {
			String type = parent.getClass().getName();
			_log.warn("Unhandled parent type ({}) for node: {}", type, QueryUtils.nodeToString(parent));
			throw new SQLTutorException("FIXME: Unhandled parent type: " + type);
		}
	}

	/**
	 * Replace the parent of a conditional operator with one of the children.
	 * 
	 * @param state        the current SQL state
	 * @param parentOp     the parent operator
	 * @param withOperand  the child that will replace the parent
	 */
	public static void replaceParent(SQLState state, BinaryOperatorNode parentOp, ValueNode withOperand) {
		parentOp.setLeftOperand(null);
		parentOp.setRightOperand(null);
		
		QueryTreeNode grandparent = QueryUtils.findParent(state.getAst(), parentOp);
		if( grandparent instanceof BinaryOperatorNode ) {
			BinaryOperatorNode binop = (BinaryOperatorNode)grandparent;
			if( binop.getLeftOperand() == parentOp )
				binop.setLeftOperand(withOperand);
			else
				binop.setRightOperand(withOperand);
		} else if( grandparent instanceof SelectNode ) {
			if( _log.isDebugEnabled() ) _log.debug("Replacing WHERE clause with: {}", QueryUtils.nodeToString(withOperand));
			((SelectNode)grandparent).setWhereClause(withOperand);
		} else {
			throw new SQLTutorException("FIXME: Unhandled parent type: " + grandparent.getClass().getName());
		}
	}
}
