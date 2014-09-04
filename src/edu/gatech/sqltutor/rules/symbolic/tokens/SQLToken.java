package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.Utils;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class SQLToken extends ChildContainerToken 
		implements IScopedToken {
	private QueryTreeNode astNode;
	
	private QueryTreeNode conjunctScope;
	
	public SQLToken(QueryTreeNode astNode) {
		super(PartOfSpeech.UNKNOWN);
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
	public QueryTreeNode getConjunctScope() {
		return conjunctScope;
	}
	
	@Override
	public void setConjunctScope(QueryTreeNode conjunctScope) {
		this.conjunctScope = conjunctScope;
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		super.addPropertiesString(b);
		b.append(", astNode=[").append(astNode.getClass().getSimpleName())
			.append("] ").append(QueryUtils.nodeToString(astNode))
			.append(", cscope=").append(Utils.ellided(QueryUtils.nodeToString(conjunctScope)));
		return b;
	}
}
