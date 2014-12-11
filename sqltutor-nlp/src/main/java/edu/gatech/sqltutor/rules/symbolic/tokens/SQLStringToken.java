/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
