package edu.gatech.sqltutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;

import objects.DatabaseTable;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.AllResultColumn;
import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.ConstantNode;
import com.akiban.sql.parser.CursorNode;
import com.akiban.sql.parser.FromList;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.ResultColumnList;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.TableName;
import com.akiban.sql.parser.ValueNode;

public class Question {
	/** Alternate verb phrases for the act of selecting. */
	private static final List<String> selectVerbs = 
		Arrays.asList("List the", "Retrieve the", "Output the", "Fetch the");
	/** RNG */
	private static final Random random = new Random();
	/** Return a random verb phrase for the act of selecting. */
	private static String getSelectVerb() {
		return selectVerbs.get(random.nextInt(selectVerbs.size()));
	}
	
	private static final Map<String, String> operatorTranslations;
	static {
		HashMap<String, String> ops = new HashMap<String, String>();
		ops.put("and", "and");
		ops.put("or",  "or");
		ops.put("=",   "is");
		ops.put("<",   "is less than");
		ops.put("<=",  "is no more than");
		ops.put(">",   "is greater than");
		ops.put(">=",  "is no less than");
		ops.put("<>",  "is not");
		ops.put("!=",  "is not");
		ops.put("+",   "plus");
		ops.put("-",   "minus");
		ops.put("*",   "times");
		ops.put("/",   "divided by");
		ops.put("%",   "modulo");
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
	
	// TODO probably don't need the statement and it's select node
	/** The parsed SQL statement / query. */
	private StatementNode statement;
	
	/** The select node of the statement. */
	private SelectNode selectNode;
	
	private String naturalLang;
	
	/** Optional database schema. */
	private ArrayList<DatabaseTable> tables;
	
	private SQLParser parser;
	
	public Question(String query) {
		this(query, null);
	}
	
	public Question(String query, ArrayList<DatabaseTable> tables) {
		System.out.println("Query: " + query); // FIXME testing
		
		this.tables = tables;
		
		query = sanitize(query);
		parser = new SQLParser();
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
		try {
			produceEnglish(select);
		} catch( StandardException e ) {
			throw new IllegalArgumentException("Could not handle query: " + query, e);
		}
		
		// FIXME testing
		System.out.println("English: " + naturalLang);
	}

	public void produceEnglish(SelectNode select) throws StandardException {
		StringBuilder result = new StringBuilder(getSelectVerb());
		
		if( select.isDistinct() )
			result.append(" distinct");
		
		processResultColumns(result, select);
		processFromList(result, select.getFromList());
		processWhereClause(result, select);
		
		result.append('.');
		
		String nlp = result.toString();
		
		// Removal of underscores; does not touch anything bounded by apostrophes.
		nlp = nlp.replaceAll("(?x)_(?=(?:[^']*'[^']*')*[^']*$)", " ");
		
		// FIXME use Wordnet stemmer or similar to produce valid plurals
		
		naturalLang = nlp;
	}
	
	private Map<String, String> getTableAliases(FromList fromList) throws StandardException {
		HashMap<String, String> aliases = new LinkedHashMap<String, String>();
		for( FromTable from: fromList ) {
			String origNameStr = null, corName = from.getCorrelationName();
			TableName origName = from.getOrigTableName();
			if( origName != null )
				origNameStr = origName.getFullTableName();
			if( corName == null ) {
				if( origNameStr == null ) {
					System.err.println(String.format(
						"No alias for from entry. (orig=%s, correlation=%s)", origNameStr, corName
					));
				} else {
					aliases.put(origNameStr, origNameStr);
				}
			} else {
				aliases.put(corName, origNameStr);
			}
//			String coName = from.getCorrelationName(),
//					tName = from.getTableName().getFullTableName(),
//					tName2 = from.getTableName().getTableName(),
//					eName = from.getExposedName();
//			System.out.println(String.format("from: %s%ncoName=%s, tName=%s, tName2=%s, eName=%s", from, coName, tName, tName2, eName));
		}
		return aliases;
	}
	
	/**
	 * Finds the table name for a bare column name.
	 * 
	 * @param name the name
	 * @return the tabel name
	 * @throws IllegalStateException if the name cannot be resolved
	 */
	private String findTableForColumn(String name) {
		if( tables == null ) {
			// if no schema info and only one table, assume column belongs to it
			FromList fromList = selectNode.getFromList();
			if( fromList.size() == 1 )
				return fromList.get(0).getOrigTableName().getFullTableName();
			throw new IllegalStateException("No schema info, could not resolve column: " + name);
		} else {
			DatabaseTable colTable = null;
			for( DatabaseTable table: tables ) {
				if( !table.getColumnNameList().contains(name) )
					continue;
				
				// check for ambiguity
				if( colTable != null ) {
					throw new IllegalStateException(String.format(
						"Ambiguous name '%s', matches tables '%s' and '%s'",
						name, colTable.getTableName(), table.getTableName()));
				}
				
				colTable = table;
			}
			
			if( colTable == null )
				throw new IllegalStateException("Column name does not resolve to any table: " + name);
			return colTable.getTableName();
		}
	}
	
	private void processResultColumns(StringBuilder result, SelectNode select) 
			throws StandardException {
		ResultColumnList resultColumns = select.getResultColumns();
		int nCols = resultColumns.size();
		// SELECT * FROM ...
		if( nCols == 1 && resultColumns.get(0) instanceof AllResultColumn ) {
			result.append(" attributes");
			return;
		}
		
		FromList fromList = select.getFromList();
		Map<String, String> aliases = getTableAliases(fromList);
		
		// organize column references by table name
		Map<String, List<String>> tableToColumns = new LinkedHashMap<String, List<String>>();
		List<ValueNode> extraExpressions = new ArrayList<ValueNode>();
		for( ResultColumn col: resultColumns ) {
			ValueNode expr = col.getExpression();
			if( !(expr instanceof ColumnReference) ) {
				System.err.println("WARN: Unhandled result column expression: " + expr);
				extraExpressions.add(expr);
			} else {
				ColumnReference ref = (ColumnReference)expr;
				String tableName = ref.getTableName(),
						colName = ref.getColumnName();
				if( tableName == null )
					tableName = findTableForColumn(colName);
				
				List<String> columns = tableToColumns.get(tableName);
				if( columns == null ) {
					columns = new ArrayList<String>();
					tableToColumns.put(tableName, columns);
				}
				columns.add(colName);
			}
		}
		
		// now output grouped by table
		for( Entry<String, List<String>> entry: tableToColumns.entrySet() ) {
			String tableName = entry.getKey();
			List<String> cols = entry.getValue();
			conjunctTerms(result, cols);
			
			result.append(" of all ").append(aliases.get(tableName)).append("s and ");
		}
		
		// and any unhandled expressions
		String query = parser.getSQLText();
		for( ValueNode expr: extraExpressions ) {
			// FIXME handle operators, functions, etc
			int begin = expr.getBeginOffset(), end = expr.getEndOffset();
			if( begin == -1 || end == -1 ) {
				// FIXME this is just a quick hack for simple expressions
				if( expr instanceof BinaryOperatorNode ) {
					BinaryOperatorNode op = (BinaryOperatorNode)expr;
					begin = op.getLeftOperand().getBeginOffset();
					end = op.getRightOperand().getEndOffset();
				}
			}
			
			if( begin == -1 || end == -1 ) {
				System.err.println("Expr offset not known, b=" + begin + ", e=" + end + ", expr=" + expr);
			} else {
				result.append(query.substring(begin, end + 1)).append(" and ");
			}
		}
		
		result.setLength(result.length() - " and ".length()); // delete trailing " and "
	}
	
	/**
	 * Append the English conjunction of a list of terms to <code>result</code>.
	 * <p>Produces:<br />
	 * 1: &lt;attr&gt; <br />
	 *	2: &lt;attr1&gt; and &lt;attr2&gt;<br />
	 *	3+: &lt;attr1&gt;, &lt;attr2&gt;, ..., and &lt;attrN&gt;<br />
	 * </p>
	 * 
	 * @param result
	 * @param terms
	 */
	private void conjunctTerms(StringBuilder result, List<String> terms) {
		// 1: <attr>
		// 2: <attr1> and <attr2>
		// 3+: <attr1>, <attr2>, ..., and <attrN> 
		for( int i = 0, ilen = terms.size(); i < ilen; ++i ) {
			String term = terms.get(i);
			if( i != 0 ) {
				if( i != ilen - 1 ) {
					result.append(',');
				} else {
					if( i != 1 )
						result.append(',');
					result.append(" and");
				}
			}
			result.append(' ').append(term);
		}
	}
	
	private void processFromList(StringBuilder result, FromList fromList) {
		// result column processing outputs the from info
	}
	
	private void processWhereClause(StringBuilder result, SelectNode select) {
		ValueNode whereClause = select.getWhereClause();
		if( whereClause == null ) return;
		
		result.append(" such that");
		
		processWhereExpression(result, whereClause);
	}
	
	private void processWhereExpression(StringBuilder result, ValueNode expression) {
		if( expression.isConstantExpression() ) {
			ConstantNode node = (ConstantNode)expression;
			Object value = node.getValue();
			result.append(' ');
			if( value instanceof String ) {
				// FIXME does escaping value make sense here?
				result.append('"').append(value).append('"');
			} else {
				result.append(value);
			}
		} else if( expression instanceof BinaryOperatorNode ) {
			BinaryOperatorNode node = (BinaryOperatorNode)expression;
			ValueNode left = node.getLeftOperand(), right = node.getRightOperand();
			
			processWhereExpression(result, left);
			
			String opEnglish = operatorTranslations.get(node.getOperator());
			if( opEnglish == null ) {
				System.err.println("No translation for operator: " + node.getOperator());
				result.append(' ').append(node.getOperator());
			} else {
				result.append(' ').append(opEnglish);
			}
			
			processWhereExpression(result, right);
		} else if( expression instanceof ColumnReference ) {
			ColumnReference ref = (ColumnReference)expression;
			result.append(" the ").append(ref.getColumnName());
		} else {
			System.err.println("WARN: Unhandled expression: " + expression);
		}
	}
	
	public String getNaturalLanguage() {
		return naturalLang;
	}
}
