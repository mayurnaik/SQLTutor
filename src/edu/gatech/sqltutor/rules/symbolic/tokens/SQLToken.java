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

import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.Utils;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class SQLToken extends ChildContainerToken 
		implements IScopedToken {
	private QueryTreeNode astNode;
	
	private QueryTreeNode conjunctScope;
	
	public SQLToken(QueryTreeNode astNode) {
		super(PartOfSpeech.UNKNOWN);
		this.astNode = astNode;
	}
	
	public SQLToken(SQLToken token) {
		super(token);
		this.astNode = token.astNode;
	}
	
	@Override
	public SymbolicType getType() {
		return SymbolicType.SQL_AST;
	}
	
	public QueryTreeNode getAstNode() {
		return astNode;
	}
	
	public void setAstNode(QueryTreeNode astNode) {
		this.astNode = astNode;
	}
	
	@Override
	public QueryTreeNode getConjunctScope() {
		return conjunctScope;
	}
	
	@Override
	public void setConjunctScope(QueryTreeNode conjunctScope) {
		this.conjunctScope = conjunctScope;
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		super.addPropertiesString(b);
		b.append(", astNode=[").append(astNode.getClass().getSimpleName())
			.append("] ").append(QueryUtils.nodeToString(astNode))
			.append(", cscope=").append(Utils.ellided(QueryUtils.nodeToString(conjunctScope)));
		return b;
	}
}
