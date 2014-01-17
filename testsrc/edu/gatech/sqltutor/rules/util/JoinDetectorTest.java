package edu.gatech.sqltutor.rules.util;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.rules.util.JoinDetector.JoinResult;

@RunWith(Parameterized.class)
public class JoinDetectorTest {
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] queries = {
			{"SELECT * FROM employee e1 INNER JOIN employee e2 ON e2.ssn=e1.manager_ssn"},
			{"SELECT * FROM employee e2 INNER JOIN employee e1 ON e2.ssn=e1.manager_ssn"},
			{"SELECT * FROM employee e1 INNER JOIN employee e2 ON e1.ssn=e2.manager_ssn"},
			{"SELECT * FROM employee e1, employee e2 WHERE e2.ssn=e1.manager_ssn"},
			{"SELECT * FROM employee e1, employee e2 WHERE e1.ssn=e2.manager_ssn"},
			{"SELECT * FROM employee e2, employee e1 WHERE e2.ssn=e1.manager_ssn"}
		};
		return Arrays.asList(queries);
	}
	
	private String query;
	public JoinDetectorTest(String query) {
		this.query = query;
	}

	@Test
	public void testManagerDetect() throws Exception {
		JoinDetector det = new JoinDetector("employee.ssn", "employee.manager_ssn");
		SQLParser p = new SQLParser();
		StatementNode s = p.parseStatement(query);
		
		JoinResult result = det.detect(QueryUtils.extractSelectNode(s));
		Assert.assertNotNull("Failed to match query: " + query, result);
	}
}
