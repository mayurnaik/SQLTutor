package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class SQLToken extends ChildContainerToken 
		implements ISymbolicToken {
	private QueryTreeNode astNode;
	
	public SQLToken(QueryTreeNode astNode) {
		super((PartOfSpeech)null); // FIXME null ok?
		this.astNode = astNode;
	}
	
	public SQLToken(SQLToken token) {
		super(token);
		this.astNode = token.astNode;
	}
	
	@Override
	public SymbolicType getType() {
		return SymbolicType.SQL_AST;
	}
	
	public QueryTreeNode getAstNode() {
		return astNode;
	}
	
	public void setAstNode(QueryTreeNode astNode) {
		this.astNode = astNode;
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		b.append(", astNode=[").append(astNode.getClass().getSimpleName())
			.append("] ").append(QueryUtils.nodeToString(astNode));
		return b;
	}
}
