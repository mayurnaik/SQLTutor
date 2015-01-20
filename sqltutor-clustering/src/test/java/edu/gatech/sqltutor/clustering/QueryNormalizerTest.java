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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class QueryNormalizerTest {
	private static final Logger _log = LoggerFactory.getLogger(QueryNormalizerTest.class); 
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			{"SELECT * FROM t1 WHERE t1.a > 0 OR t1.b < 10", null},
			{"SELECT * FROM t1 WHERE NOT (t1.a > 0 OR t1.b < 10)", null},
			{"SELECT * FROM t1 WHERE NOT t1.a OR NOT t1.b", null},
			{"SELECT * FROM t1 WHERE NOT (t1.a AND t1.b)", null},
			{"SELECT * FROM t1 WHERE t1.a='f'", null},
			{"SELECT * FROM table1 AS t1 WHERE NOT (t1.attr IN (SELECT * FROM table2) OR NOT EXISTS (SELECT * FROM table1 t1alias WHERE t1alias.fk=t1.pk))", null},
			{"SELECT * FROM table1 t1, table1 t2 WHERE t1.name = t2.name AND t1.ssn = '123123123'", null}
		};
		return Arrays.asList(params);
	}
	
	private String query;
	private String expectedResult;
	
	public QueryNormalizerTest(String query, String result) {
		this.query = query;
		this.expectedResult = result;
	}

	@Test
	public void test() {
		_log.info("Query:\n{}", query);
		QueryNormalizer normalizer = new QueryNormalizer();
		String result = normalizer.normalize(this.query);
		_log.info("Result:\n{}", result);
		if( expectedResult != null ) {
			Assert.assertEquals(expectedResult, result);
		}
	}

}
