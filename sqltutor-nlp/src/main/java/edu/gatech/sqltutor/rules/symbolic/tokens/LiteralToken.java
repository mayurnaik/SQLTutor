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

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;


/**
 * A language fragment that is a literal expression 
 * rather than symbolic.
 */
public class LiteralToken extends AbstractSymbolicToken {
	protected String expression;
	
	public LiteralToken(LiteralToken token) {
		super(token);
		this.expression = token.expression;
	}
	
	public LiteralToken(String expression, PartOfSpeech pos) {
		super(pos);
		if( pos == null ) 
			throw new IllegalArgumentException("Literal fragments must have a part of speech.");
		if( expression == null ) throw new NullPointerException("expression is null");
		this.expression = expression;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	public String getExpression() {
		return expression;
	}
	
	@Override
	public SymbolicType getType() {
		return SymbolicType.LITERAL;
	}
	
	@Override
	public String toString() {
		return "{" + getType() + "/" + getPartOfSpeech() + ": \"" + expression + "\"}";
	}
}
