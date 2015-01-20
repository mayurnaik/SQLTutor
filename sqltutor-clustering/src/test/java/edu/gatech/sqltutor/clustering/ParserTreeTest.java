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
package edu.gatech.sqltutor.clustering;

import org.junit.Test;

import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.QueryUtils;

public class ParserTreeTest {

	@Test
	public void test() throws Exception {
		SQLParser parser = new SQLParser();
		
		String sql = "SELECT * FROM t WHERE a OR b OR c";
		StatementNode statement = parser.parseStatement(sql);
		statement = new QueryNormalizer().normalize(statement);
		System.out.println(QueryUtils.nodeToString(statement));
		statement.treePrint();
	}

}
