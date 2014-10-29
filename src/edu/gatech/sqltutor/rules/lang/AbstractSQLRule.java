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
package edu.gatech.sqltutor.rules.lang;

import java.util.Collections;
import java.util.List;

import org.deri.iris.api.basics.IRule;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISQLTranslationRule;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.MetaruleUtils;
import edu.gatech.sqltutor.rules.SQLState;

public abstract class AbstractSQLRule implements ISQLTranslationRule {
	protected SQLState state;

	public AbstractSQLRule() {
	}
	
	@Override
	public String getRuleId() {
		return MetaruleUtils.getDefaultRuleId(this.getClass());
	}
	
	@Override
	public int getPrecedence() {
		return DefaultPrecedence.DESTRUCTIVE_UPDATE;
	}
	
	@Override
	public int getType() {
		return ITranslationRule.TYPE_SQL;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return Collections.emptyList();
	}
	
	public SQLState getState() {
		return state;
	}
}
