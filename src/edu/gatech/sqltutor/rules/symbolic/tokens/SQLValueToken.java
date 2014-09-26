package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.rules.symbolic.ValueType;

public class SQLValueToken extends SQLToken implements IHasValueType {
	protected ValueType valueType = ValueType.UNKNOWN;

	public SQLValueToken(QueryTreeNode astNode) {
		super(astNode);
	}

	public SQLValueToken(SQLToken token) {
		super(token);
	}

	@Override
	public ValueType getValueType() {
		return valueType;
	}

	@Override
	public void setValueType(ValueType valueType) {
		if( valueType == null ) throw new NullPointerException("valueType is null");
		this.valueType = valueType;
	}

}
