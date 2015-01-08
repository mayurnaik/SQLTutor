/*
 *   Copyright (c) 2015 Program Analysis Group, Georgia Tech
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
package edu.gatech.sqltutor.sql;

import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SQLParserContext;

/**
 * <code>SQLParser</code> with some additional configuration options.
 */
public class ConfigurableSQLParser extends SQLParser implements
		SQLParserContext {
	protected IdentifierCase identifierCase = super.getIdentifierCase();

	public ConfigurableSQLParser() {
	}
	
	@Override
	public IdentifierCase getIdentifierCase() {
		return identifierCase;
	}
	
	public void setIdentifierCase(IdentifierCase identifierCase) {
		this.identifierCase = identifierCase;
	}
}
