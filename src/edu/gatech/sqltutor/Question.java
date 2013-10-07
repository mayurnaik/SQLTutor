package edu.gatech.sqltutor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.AllResultColumn;
import com.akiban.sql.parser.CursorNode;
import com.akiban.sql.parser.FromList;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.ResultColumnList;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.ValueNode;

public class Question {
	/** Alternate verb phrases for the act of selecting. */
	private static final List<String> selectVerbs = 
		Arrays.asList("List the", "Retrieve the", "Output the");
	/** RNG */
	private static final Random random = new Random();
	/** Return a random verb phrase for the act of selecting. */
	private static String getSelectVerb() {
		return selectVerbs.get(random.nextInt(selectVerbs.size()));
	}
	
	private static final Map<String, String> operatorTranslations;
	static {
		HashMap<String, String> ops = new HashMap<String, String>();
		ops.put("=",  "is");
		ops.put("<",  "is less than");
		ops.put("<=", "is no more than");
		ops.put(">",  "is greater than");
		ops.put(">=", "is no less than");
		ops.put("<>", "is not");
		ops.put("!=", "is not");
		operatorTranslations = ops;
	}
	
	private static final Pattern sanitizer =
		Pattern.compile("[;\\s]+$");
	
	/** Sanitize a query statement for processing. */
	private static String sanitize(String query) {
		return sanitizer.matcher(query).replaceAll("");
	}
	
	public static void main(String[] args) {
		for( String arg: args ) {
			try {
				Question q = new Question(arg);
			} catch( IllegalArgumentException e ) {
				System.err.println("Failed to parse: " + arg);
				e.printStackTrace();
			}
		}
	}
	
	// TODO probabyl don't need the statement and it's select node
	/** The parsed SQL statement / query. */
	private StatementNode statement;
	
	/** The select node of the statement. */
	private SelectNode selectNode;
	
	private String naturalLang;
	
	public Question(String query) {
		query = sanitize(query);
		SQLParser parser = new SQLParser();
		try {
			statement = parser.parseStatement(query);
		} catch (StandardException e) {
			throw new IllegalArgumentException("Failed to parse: " + query, e);
		}
		
		if( !(statement instanceof CursorNode) )
			throw new IllegalArgumentException("Expecting a SELECT statement, got: " + query);
		CursorNode c = (CursorNode)statement;
		if( c.getName() != null )
			throw new IllegalArgumentException("Expecting a SELECT statement, got named cursor: " + query);
		
		SelectNode select = (SelectNode)c.getResultSetNode();
		
		selectNode = select;
		produceEnglish(select);
		
		// FIXME testing
		System.out.println("Query: " + query);
		System.out.println("English: " + naturalLang);
	}

	public void produceEnglish(SelectNode select) {
		StringBuilder result = new StringBuilder(getSelectVerb());
		
		if( select.isDistinct() )
			result.append(" distinct");
		
		processResultColumns(result, select.getResultColumns());
		processFromList(result, select.getFromList());
		
		naturalLang = result.toString();
	}
	
	private void processResultColumns(StringBuilder result, ResultColumnList resultColumns) {
		int nCols = resultColumns.size();
		// SELECT * FROM ...
		if( nCols == 1 && resultColumns.get(0) instanceof AllResultColumn ) {
			result.append(" attributes");
			return;
		}
		
		LinkedHashMap<String, List<String>> tableToExpressions = 
			new LinkedHashMap<String, List<String>>();
		// TODO want to re-order these by referenced table name
		
		// 1: <attr>
		// 2: <attr1> and <attr2>
		// 3+: <attr1>, <attr2>, ..., and <attrN> 
		for( int i = 0; i < nCols; ++i ) {
			ResultColumn col = resultColumns.get(i);
			if( i != 0 ) {
				if( i == nCols - 1 )
					result.append(" and");
				else
					result.append(',');
			}
			// for "<x> AS <y>", col.getName() is the <y> and col.getExpression() is the <x>
			result.append(' ').append(col.getName()).append('s'); // FIXME expressions? plural column names?
			
		}
	}
	
	private void processFromList(StringBuilder result, FromList fromList) {
		if( fromList.size() == 1 ) {
			result.append(" of all ").append(fromList.get(0).getOrigTableName()).append('s');
			return;
		}
		
		// TODO finish this
	}
	
	public String getNaturalLanguage() {
		return naturalLang;
	}
}
