package edu.gatech.sqltutor.clustering;

import com.akiban.sql.StandardException;
import com.akiban.sql.compiler.BooleanNormalizer;
import com.akiban.sql.compiler.TypeComputer;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SQLParserContext;
import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;

public class QueryNormalizer {

	public QueryNormalizer() {
	}

	public String normalize(String query) throws SQLTutorException {
		SQLParser parser = new SQLParser();
		try {
			StatementNode statement = parser.parseStatement(query);
			statement = normalize(statement);
			return QueryUtils.nodeToString(statement);
		} catch (StandardException e) {
			throw new SQLTutorException(e);
		}
	}
	
	public StatementNode normalize(StatementNode node) {
		SQLParserContext context = node.getParserContext();
		try {
			node.accept(new BooleanNormalizer(context));
			node.accept(new TypeComputer());
			node.accept(new ComparisonDirectionNormalizer(context));
			node.accept(new RemoveDanglingBooleansNormalizer(context));
			node.accept(new ExpressionSorter(context));
		} catch (StandardException e ) {
			throw new SQLTutorException(e);
		}
		return node;
	}
}
