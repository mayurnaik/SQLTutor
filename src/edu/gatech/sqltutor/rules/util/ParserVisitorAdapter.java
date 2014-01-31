package edu.gatech.sqltutor.rules.util;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.Visitable;
import com.akiban.sql.parser.Visitor;

/**
 * Common visitor structure that handles casting for the parser 
 * and is top-down by default.
 */
public abstract class ParserVisitorAdapter implements Visitor {
	public ParserVisitorAdapter() { }
	
	/**
	 * Pre-cast version of {@link #visit(Visitable)}.
	 * @param node
	 * @return
	 * @throws StandardException
	 */
	public abstract QueryTreeNode visit(QueryTreeNode node) throws StandardException;
	
	/** @see Visitor#visitChildrenFirst(Visitable) */
	public boolean visitChildrenFirst(QueryTreeNode node) {
		return false;
	}
	
	/** @see Visitor#skipChildren(Visitable) */
	public boolean skipChildren(QueryTreeNode node) {
		return false;
	}

	@Override
	public Visitable visit(Visitable node) throws StandardException {
		return visit((QueryTreeNode)node);
	}

	@Override
	public boolean visitChildrenFirst(Visitable node) {
		return visitChildrenFirst((QueryTreeNode)node);
	}

	@Override
	public boolean stopTraversal() {
		return false;
	}

	@Override
	public boolean skipChildren(Visitable node) throws StandardException {
		return skipChildren((QueryTreeNode)node);
	}
}