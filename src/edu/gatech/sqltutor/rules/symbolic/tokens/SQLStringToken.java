package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.CharConstantNode;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.ValueType;

public class SQLStringToken extends SQLToken implements IHasValueType {
	public static enum StringType {
		STRING,
		DATETIME;
	}
	
	protected ValueType valueType = ValueType.STRING;

	private StringType stringType = StringType.STRING;
	
	public SQLStringToken(QueryTreeNode ast) {
		super(ast);
		if( !(ast instanceof CharConstantNode) )
			throw new SymbolicException("CharConstantNode required, not: " + ast);
	}
	
	@Override
	public ValueType getValueType() {
		return valueType;
	}
	
	@Override
	public void setValueType(ValueType valueType) {
		if( valueType == null ) throw new NullPointerException("valueType is null");
		switch( valueType ) {
		case STRING:
		case DATETIME:
			this.valueType = valueType;
			break;
		default:
			throw new IllegalArgumentException("String token cannot have value type: " + valueType);
		}
	}
	
	public StringType getStringType() {
		return stringType;
	}
	
	public void setStringType(StringType stringType) {
		this.stringType = stringType;
	}

	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		return super.addPropertiesString(b)
			.append(", valueType=").append(valueType)
			.append(", stringType=").append(stringType);
	}
}
