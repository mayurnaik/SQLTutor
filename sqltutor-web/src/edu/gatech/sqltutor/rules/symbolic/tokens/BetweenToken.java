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

public class BetweenToken extends ChildContainerToken {
	public BetweenToken(BetweenToken token) {
		super(token);
	}

	public BetweenToken(PartOfSpeech pos) {
		super(pos);
	}
	
	public BetweenToken() {
		super(PartOfSpeech.QUANTIFIER_PHRASE);
	}
	
	public ISymbolicToken getObjectToken() {
		if( children.size() < 1 )
			return null;
		return children.get(0); 
	}
	
	public ISymbolicToken getLowerBoundToken() {
		if( children.size() < 2 )
			return null;
		return children.get(1);
	}
	
	public ISymbolicToken getUpperBoundToken() {
		if( children.size() < 3 )
			return null;
		return children.get(2);
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.BETWEEN;
	}
	
	@Override
	protected boolean canAcceptChild(ISymbolicToken tok) {
		return tok != null && children.size() < 3;
	}
}
