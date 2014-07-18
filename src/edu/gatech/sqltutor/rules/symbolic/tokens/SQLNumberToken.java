package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.NumericConstantNode;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.symbolic.tokens.NumberToken.NumericType;

/**
 * SQL token holding a {@link NumericConstantNode}.
 */
public class SQLNumberToken extends SQLToken {
	private NumericType numericType = NumericType.GENERAL;

	public SQLNumberToken(QueryTreeNode astNode) {
		super(astNode);
		if( !(astNode instanceof NumericConstantNode) )
			throw new SQLTutorException("Only accepts NumericConstantNode, not: " + astNode.getClass().getName());
	}

	public SQLNumberToken(SQLNumberToken token) {
		super(token);
		this.numericType = token.numericType;
	}
	
	public NumericType getNumericType() {
		return numericType;
	}
	
	public void setNumericType(NumericType numericType) {
		this.numericType = numericType;
	}
	
	@Override
	public NumericConstantNode getAstNode() {
		return (NumericConstantNode)super.getAstNode();
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		return super.addPropertiesString(b)
			.append(", numericType=").append(numericType);
	}
}
