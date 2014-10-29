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
