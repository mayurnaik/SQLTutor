package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.CharConstantNode;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.rules.symbolic.SymbolicException;

public class SQLStringToken extends SQLToken {
	public static enum StringType {
		STRING,
		DATETIME;
	}

	private StringType stringType = StringType.STRING;
	
	public SQLStringToken(QueryTreeNode ast) {
		super(ast);
		if( !(ast instanceof CharConstantNode) )
			throw new SymbolicException("CharConstantNode required, not: " + ast);
	}
	
	public StringType getStringType() {
		return stringType;
	}
	
	public void setStringType(StringType stringType) {
		this.stringType = stringType;
	}

	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		return super.addPropertiesString(b).append(", stringType=").append(stringType);
	}
}
