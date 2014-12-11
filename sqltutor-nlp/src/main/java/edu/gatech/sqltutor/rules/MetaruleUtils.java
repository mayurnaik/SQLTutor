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
package edu.gatech.sqltutor.rules;

public class MetaruleUtils {
	/**
	 * Returns the default rule id format for the given class.
	 * @param ruleClass the rule class
	 * @return the default rule id
	 */
	public static String getDefaultRuleId(Class<?> ruleClass) {
		if( ruleClass == null ) throw new NullPointerException("ruleClass is null");
		return ruleClass.getSimpleName();
	}
}
