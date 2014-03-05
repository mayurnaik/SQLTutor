package edu.gatech.sqltutor.rules.util;

import java.util.List;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.QueryTreeNode;
import com.google.common.collect.Lists;

import edu.gatech.sqltutor.SQLTutorException;

/**
 * AST visitor that collects immediate children of the 
 * accepting node.
 */
public class GetChildrenVisitor extends ParserVisitorAdapter {
	protected QueryTreeNode parent;
	protected List<QueryTreeNode> children;
	
	public GetChildrenVisitor() {
	}
	
	/** Reset, collect and then return the children of <code>parent</code>. */
	public List<QueryTreeNode> getChildren(QueryTreeNode parent) {
		this.reset();
		try {
			parent.accept(this);
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
		return getChildren();
	}
	
	@Override
	public boolean skipChildren(QueryTreeNode node) {
		if( parent == null ) {
			parent = node;
			children = Lists.newArrayList();
			return false;
		}
		return true;
	}

	@Override
	public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
		if( node != parent )
			children.add(node);
		return node;
	}
	
	/** Resets the visitor so it can be reused. */
	public void reset() {
		parent = null;
		children = null;
	}

	public QueryTreeNode getParent() {
		return parent;
	}
	
	public List<QueryTreeNode> getChildren() {
		return children;
	}
}
