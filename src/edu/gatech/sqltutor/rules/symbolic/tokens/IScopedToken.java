package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.QueryTreeNode;

public interface IScopedToken extends ISymbolicToken {
	public QueryTreeNode getConjunctScope();
	public void setConjunctScope(QueryTreeNode node);
}
