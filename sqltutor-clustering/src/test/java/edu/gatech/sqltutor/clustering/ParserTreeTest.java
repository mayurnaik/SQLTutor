package edu.gatech.sqltutor.clustering;

import static org.junit.Assert.*;

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
