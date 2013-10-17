package edu.gatech.sqltutor;

import java.util.regex.Pattern;

import com.akiban.sql.parser.CursorNode;
import com.akiban.sql.parser.ResultSetNode;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;

/**
 * Static utility functions related to SQL queries.
 */
public class QueryUtils {
	private static final Pattern sanitizer = 
		Pattern.compile("[;\\s]+$");
	
	/**
	 * Prepare a single statement query for execution.
	 * <p>Currently this removes any trailing semi-colons.
	 * It does not do any escaping or other security-related 
	 * processing.
	 * </p>
	 * 
	 * @param query the query to sanitize
	 * @return the sanitized query
	 */
	public static String sanitize(String query) {
		return sanitizer.matcher(query).replaceAll("");
	}
	
	/**
	 * Extracts the embedded <code>SelectNode</code> from a statement.
	 * 
	 * @param statement the statement to parse
	 * @return the select node
	 * @throws IllegalArgumentException if a select node cannot be extracted
	 */
	public static SelectNode extractSelectNode(StatementNode statement) {
		if( statement == null )
			throw new IllegalArgumentException("statement is null");
		if( !(statement instanceof CursorNode) )
			throw new IllegalArgumentException("statement is not a cursor node");
		

		CursorNode c = (CursorNode)statement;
		if( c.getName() != null )
			throw new IllegalArgumentException("statement is an un-named cursor");
		ResultSetNode resultSet = c.getResultSetNode();
		if( !(resultSet instanceof SelectNode) )
			throw new IllegalArgumentException("statement is not a select node");
		
		return (SelectNode)resultSet;
	}
}
