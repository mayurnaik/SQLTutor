package edu.gatech.sqltutor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.CursorNode;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultSetNode;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.RuleMetaData;

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
		
		if( NodeTypes.CURSOR_NODE != statement.getNodeType() )
			throw new IllegalArgumentException("statement is not a cursor node");
		
		ResultSetNode select = ((CursorNode)statement).getResultSetNode();
		if( NodeTypes.SELECT_NODE != select.getNodeType() )
			throw new IllegalArgumentException("statement is not a select node");
		
		return (SelectNode)select;
	}
	
	/**
	 * Checks if a node has already been handled.
	 * 
	 * @param node the node to check, must not be <code>null</code>
	 * @return if the node is handled already
	 */
	public static boolean isHandled(QueryTreeNode node) {
		RuleMetaData meta = (RuleMetaData)node.getUserData();
		return meta != null && meta.isHandled();
	}
	
	/**
	 * Get a node's metadata, creating and attaching it if necessary.
	 * 
	 * @param node
	 * @return the new or existing metadata
	 */
	public static RuleMetaData getOrInitMetaData(QueryTreeNode node) {
		RuleMetaData meta = (RuleMetaData)node.getUserData();
		if( meta == null )
			node.setUserData(meta = new RuleMetaData());
		return meta;
	}
	
	/**
	 * Indicates whether <code>rule</code> has already contributed to <code>node</code>.
	 * 
	 * @param rule the rule to check
	 * @param node the node to check
	 * @return if the rule contributed already
	 */
	public static boolean hasContributed(ITranslationRule rule, QueryTreeNode node) {
		RuleMetaData meta = (RuleMetaData)node.getUserData();
		if( meta == null )
			return false;
		return meta.getContributors().contains(rule);
	}
	
	/**
	 * Build a map from exposed table names to the from-list elements they reference.
	 * 
	 * <p>
	 * Given:<br/>
	 * <code>SELECT ... FROM foo f, bar, (SELECT ...) AS baz</code><br />
	 * The map would be:<br />
	 * "f" =&gt; foo (FromBaseTable instance)<br />
	 * "bar" =&gt; bar (FromBaseTable instance)<br />
	 * "baz" =&gt; FromSubquery instance for the subquery<br />
	 * </p>
	 * 
	 * @param select the select node to build a map for
	 * @return the alias map
	 * @throws StandardException
	 */
	public static Map<String, FromTable> buildTableAliasMap(SelectNode select) 
			throws StandardException {
		Map<String, FromTable> aliasMap = new HashMap<String, FromTable>();
		for( FromTable fromTable: select.getFromList() ) {
			aliasMap.put(fromTable.getExposedName(), fromTable);
		}
		return aliasMap;
	}
}
