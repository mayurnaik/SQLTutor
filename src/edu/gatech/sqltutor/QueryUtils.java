package edu.gatech.sqltutor;

import java.util.regex.Pattern;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.CursorNode;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultSetNode;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.Visitable;
import com.akiban.sql.unparser.NodeToString;

import edu.gatech.sqltutor.rules.util.ParserVisitorAdapter;
import edu.gatech.sqltutor.util.Pair;

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
	 * Find the parent of <code>child</code> by searching the AST from <code>root</code>.
	 * 
	 * @param root   the AST root node or a known ancestor of <code>child</code>
	 * @param child  the child node
	 * @return the parent node if found or <code>null</code>
	 * @throws SQLTutorException if an error occurs
	 */
	public static QueryTreeNode findParent(QueryTreeNode root, final QueryTreeNode child) 
			throws SQLTutorException {
		
		class HasChildVisitor extends ParserVisitorAdapter {
			QueryTreeNode parent;
			QueryTreeNode child;
			boolean found;
			
			public HasChildVisitor(QueryTreeNode child) {
				this.child = child;
			}
			
			@Override
			public boolean stopTraversal() {
				return found;
			}
			
			@Override
			public boolean skipChildren(Visitable node) throws StandardException {
				if( parent == null ) {
					parent = (QueryTreeNode)node;
					return false;
				}
				return true;
			}
			
			@Override
			public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
				if( parent == node )
					return node;
				if( node.equals(child) )
					found = true;
				return node;
			}
			
			public void reset() { parent = null; found = false; }
			
			public QueryTreeNode getParent() { return parent; }
			public boolean isFound() { return found; }
		}
		
		final HasChildVisitor hasChild = new HasChildVisitor(child);
		try {
			root.accept(new ParserVisitorAdapter() {
				boolean found;
				@Override
				public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
					hasChild.reset();
					node.accept(hasChild);
					if( hasChild.isFound() )
						found = true;
					return node;
				}
				
				@Override
				public boolean stopTraversal() {
					return found;
				}
			});
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
		return hasChild.getParent();
	}

	/**
	 * Splits <code>key</code> into table and column parts.
	 * 
	 * @param key the key
	 * @return the table and column parts
	 * @throws IllegalArgumentException if <code>key</code> is not of the form <code>table.column</code>
	 */
	public static Pair<String, String> splitKeyParts(String key) {
		if( key == null ) throw new NullPointerException("key is null");
		String[] parts = key.split("\\.", 2);
		if( parts.length != 2 )
			throw new IllegalArgumentException("Key must be fully qualified, got: " + key);
		return Pair.make(parts[0], parts[1]);
	}
	
	public static String nodeToString(QueryTreeNode node) {
		if( node == null ) return null;
		try {
			return new NodeToString().toString(node);
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
	}
}
