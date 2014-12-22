package edu.gatech.sqltutor.clustering;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

@RunWith(Parameterized.class)
public class QueryNormalizerListTest {
	private static final Logger _log = LoggerFactory.getLogger(QueryNormalizerTest.class); 
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			{Arrays.asList("SELECT * FROM t1 WHERE t1.a='f'", 
					"SELECT * FROM t1 WHERE (t1.a = 'f')", 
					"SELECT * FROM t1 WHERE NOT (t1.a <> 'f')",
					"SELECT * FROM t1 WHERE t1.a=false", 
					"SELECT * FROM t1 WHERE t1.a <> 't'",
					"SELECT t2.* FROM t1 AS t2 WHERE t2.a = 'f'"), null}
		};
		return Arrays.asList(params);
	}
	
	private List<String> queries;
	private List<String> expectedResults;

	public QueryNormalizerListTest(List<String> queries, List<String> expectedResults) {
		this.queries = queries;
		this.expectedResults = expectedResults;
	}

	@Test
	public void test() {
		QueryNormalizer normalizer = new QueryNormalizer();
		Multiset<String> results = normalizer.getNormalizedSet(this.queries);
		Set<String> elementSet = Multisets.copyHighestCountFirst(results).elementSet();
		for(String result : elementSet)
			_log.info("Result ({} occurences):\n{}", results.count(result), result);
		_log.info("Reduced list from size {} to size {}", queries.size(), elementSet.size());
		if( expectedResults != null ) {
			Assert.assertEquals(expectedResults, results);
		}
	}

}
