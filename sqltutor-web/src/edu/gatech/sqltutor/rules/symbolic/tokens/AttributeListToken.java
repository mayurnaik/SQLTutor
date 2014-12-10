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

import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultColumn;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class AttributeListToken 
		extends ChildContainerToken implements ISymbolicToken {
	public AttributeListToken(AttributeListToken token) {
		super(token);
	}
	
	public AttributeListToken() {
		super(PartOfSpeech.NOUN_PHRASE);
	}
	
	@Override
	public SymbolicType getType() {
		return SymbolicType.ATTRIBUTE_LIST;
	}

	@Override
	protected boolean canAcceptChild(ISymbolicToken tok) {
		switch( tok.getType() ) {
			case ATTRIBUTE:
			case LITERAL:
			case ALL_ATTRIBUTES:
				return true;
			case SQL_AST: {
				SQLToken sqlToken = ((SQLToken)tok);
				QueryTreeNode astNode = sqlToken.getAstNode();
				return astNode instanceof ResultColumn || astNode instanceof ColumnReference;
			}
			default:
				return false;
		}
	}
}
