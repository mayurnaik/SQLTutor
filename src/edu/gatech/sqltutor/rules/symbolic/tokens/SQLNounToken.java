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

import edu.gatech.sqltutor.Utils;

/**
 * An SQL token that has a noun part of speech, 
 * such as columns or table references.
 */
public class SQLNounToken extends SQLToken implements INounToken {
	protected String singular;
	protected String plural;
	protected boolean definite;

	public SQLNounToken(QueryTreeNode astNode) {
		super(astNode);
	}

	public SQLNounToken(SQLNounToken token) {
		super(token);
		this.singular = token.singular;
		this.plural = token.plural;
		this.definite = token.definite;
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		b = super.addPropertiesString(b);
		b.append(", singular=\"").append(Utils.escapeChars(singular, "\""))
			.append("\", plural=\"").append(Utils.escapeChars(plural, "\""))
			.append("\"");
		return b;
	}

	@Override
	public String getSingularLabel() {
		return singular;
	}

	@Override
	public void setSingularLabel(String singular) {
		this.singular = singular;
	}

	@Override
	public String getPluralLabel() {
		return plural;
	}

	@Override
	public void setPluralLabel(String plural) {
		this.plural = plural;
	}
	
	@Override
	public boolean isDefinite() {
		return definite;
	}

	@Override
	public void setDefinite(boolean definite) {
		this.definite = definite;
	}
}
