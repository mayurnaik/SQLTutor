package edu.gatech.sqltutor.rules.datalog.iris;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.compiler.Parser;
import org.deri.iris.storage.IRelation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class ParseEvaluateTest {
	private static final Logger _log = LoggerFactory.getLogger(ParseEvaluateTest.class);
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			{"/testdata/datalog/parsetest.dlog"}
		};
		return Arrays.asList(params);
	}
	
	private String resourcePath;
	
	public ParseEvaluateTest(String resourcePath) {
		this.resourcePath = resourcePath;
	}
	
	@Test
	public void test() throws Exception {
		Parser parser = IrisUtil.newParser();
		
		InputStream inStream = ParseEvaluateTest.class.getResourceAsStream(resourcePath);
		parser.parse(new InputStreamReader(inStream, Charsets.UTF_8));
		
		IKnowledgeBase kb = KnowledgeBaseFactory.createKnowledgeBase(parser.getFacts(), parser.getRules());
		
		List<IVariable> bindings = Lists.newArrayList();
		for( IQuery query: parser.getQueries() ) {
			bindings.clear();
			_log.info("Evaluating query: {}", query);
			IRelation result = kb.execute(query, bindings);
			_log.info("Variables: {}", bindings);
			for( int i = 0; i < result.size(); ++i ) {
				ITuple row = result.get(i);
				_log.info("{}", row);
			}
		}
	}

}
