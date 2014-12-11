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

import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class AllAttributesToken extends AttributeToken {

	public AllAttributesToken(AttributeToken token) {
		super(token);
		this.definite = false;
	}
	
	public AllAttributesToken(ERAttribute attr) {
		super(attr, PartOfSpeech.NOUN_PLURAL);
	}
	
	public AllAttributesToken(String tableName) {
		super(new ERAttribute(tableName + "." + "*"), PartOfSpeech.NOUN_PLURAL);
	}
	
	@Override
	public SymbolicType getType() {
		return SymbolicType.ALL_ATTRIBUTES;
	}

}
