/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package edu.gatech.sqltutor;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.CursorNode;
import com.akiban.sql.parser.FromTable;
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
	
	/**
	 * Generates a random correlation name and adds it to the FromTable.
	 * @param fromTable
	 * @return	true if an alias was generated and added to the FromTable, else false.
	 */
	public static boolean generateCorrelationName(FromTable fromTable) {
		boolean generated = false;
		if (fromTable.getCorrelationName() == null) {
			// To avoid overlap, make it random
			final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			final Random rand = new Random();
			final int len = 5;
			StringBuilder sb = new StringBuilder(len);
			for (int i = 0; i < len; i++)
				sb.append(alphabet.charAt(rand.nextInt(alphabet.length())));
			try {
				String correlationName = fromTable.getTableName()
						.getTableName().substring(0, 1)
						+ sb.toString();
				fromTable.setCorrelationName(correlationName);
			} catch (StandardException e) {
				e.printStackTrace();
			}
			generated = true;
		}
		return generated;
	}
	
	/**
	 * Reads the database metadata for a set of tables.
	 * 
	 * @param meta            the database connection metadata
	 * @param catalog        a catalog restriction or <code>null</code>
	 * @param schemaPattern  a schema restriction or <code>null</code>
	 * @param tablePattern   a table restriction or <code>null</code>
	 * @param types          table type restrictions or <code>null</code>
	 * @return the table information
	 * @throws SQLException if thrown by the JDBC API
	 */
	public static List<DatabaseTable> readTableInfo(DatabaseMetaData meta, String catalog, 
			String schemaPattern, String tablePattern, String[] types)
			throws SQLException {
		List<DatabaseTable> tables = new ArrayList<>();
		try (ResultSet rs = meta.getTables(catalog, schemaPattern, tablePattern, types)) {
			while (rs.next())
				tables.add(new DatabaseTable(rs, meta));
		}
		return tables;
	}
	
	/**
	 * Reads the database metadata for a set of tables.  This version 
	 * filters the schema by <code>schemaPattern</code> and the 
	 * types to be either <code>TABLE</code> or <code>VIEW</code>.
	 * 
	 * @param meta            the database connection metadata
	 * @param schemaPattern  a schema restriction or <code>null</code>
	 * @return the table information
	 * @throws SQLException if thrown by the JDBC API
	 */
	public static List<DatabaseTable> readTableInfo(DatabaseMetaData meta, String schemaPattern) throws SQLException {
		return readTableInfo(meta, null, schemaPattern, null, new String[] {"TABLE", "VIEW"});
	}
}
