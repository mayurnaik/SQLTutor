package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.NumericConstantNode;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.symbolic.ValueType;

/**
 * SQL token holding a {@link NumericConstantNode}.
 */
public class SQLNumberToken extends SQLToken implements IHasValueType {
	
	protected ValueType valueType = ValueType.NUMBER;

	public SQLNumberToken(QueryTreeNode astNode) {
		super(astNode);
		if( !(astNode instanceof NumericConstantNode) )
			throw new SQLTutorException("Only accepts NumericConstantNode, not: " + astNode.getClass().getName());
	}

	public SQLNumberToken(SQLNumberToken token) {
		super(token);
		this.valueType = token.valueType;
	}
	
	@Override
	public ValueType getValueType() {
		return valueType;
	}
	
	@Override
	public void setValueType(ValueType valueType) {
		if( valueType == null ) throw new NullPointerException("valueType is null");
		switch( valueType ) {
		case NUMBER:
		case DOLLARS:
			this.valueType = valueType;
			break;
		default:
			throw new IllegalArgumentException("Number token cannot be value type: " + valueType);
		}
	}
	
	@Override
	public NumericConstantNode getAstNode() {
		return (NumericConstantNode)super.getAstNode();
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		return super.addPropertiesString(b)
			.append(", valueType=").append(valueType);
	}
}
