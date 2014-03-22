package edu.gatech.sqltutor.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.AllResultColumn;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.SelectNode;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.util.ParserVisitorAdapter;

/** Container for maps of SQL AST structure used by translators. */ 
public class SQLMaps {
	private static final Logger _log = LoggerFactory.getLogger(SQLMaps.class);
	
	protected Map<String, FromTable> tableAliases;
	protected Multimap<FromTable, ResultColumn> fromToResult;

	public SQLMaps() { }
	
	/**
	 * Build the maps.
	 * @param select the select to build from
	 * @throws SQLTutorException
	 */
	public void buildMaps(SelectNode select) {
		if( select == null ) throw new NullPointerException("select is null");
		tableAliases = buildTableAliasMap(select);
		fromToResult = buildResultToFromMap(select);
	}
	
	public void clear() {
		tableAliases = null;
		fromToResult = null;
	}
	
	/**
	 * Build and return a map from exposed table names to the from-list elements they reference.
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
	 * @throws SQLTutorException if an error occurs
	 */
	protected Map<String, FromTable> buildTableAliasMap(SelectNode select) 
			throws SQLTutorException {
		if( select == null ) throw new NullPointerException("select is null");
		final Map<String, FromTable> aliasMap = new HashMap<String, FromTable>();
		try {
			select.getFromList().accept(new ParserVisitorAdapter() {
				@Override
				public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
					switch( node.getNodeType() ) {
						case NodeTypes.FROM_BASE_TABLE:
						case NodeTypes.FROM_SUBQUERY: {
							FromTable fromTable = (FromTable)node;
							aliasMap.put(fromTable.getExposedName(), fromTable);
							break;
						}
					}
					return node;
				}
			});
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
		return aliasMap;
	}	
	
	/**
	 * Builds and returns a map from table references to result columns.
	 * @param select the node to build based on
	 * @return the resulting map
	 */
	protected Multimap<FromTable, ResultColumn> buildResultToFromMap(SelectNode select) {
		if( select == null ) throw new NullPointerException("select is null");
		
		Multimap<FromTable, ResultColumn> fromToResult = LinkedListMultimap.create();
		for( ResultColumn resultColumn: select.getResultColumns() ) {
			String tableName;
			FromTable fromTable;
			// @see com.akiban.sql.parser.AllResultColumn.java
			if(resultColumn.getNodeType() == NodeTypes.ALL_RESULT_COLUMN) {
				// Asterisk detected:
				tableName = ((AllResultColumn)resultColumn).getFullTableName();
				if( tableName == null ) {
					// It was not tied to a particular table:
					Iterator<FromTable> iterator = tableAliases.values().iterator();
					for( ; iterator.hasNext(); ) {
						fromTable = iterator.next();
						fromToResult.put(fromTable, resultColumn);
					}
				} else {
					fromTable = tableAliases.get(tableName);
					if( fromTable != null ) {
						fromToResult.put(fromTable, resultColumn);
					} else {
						_log.error("No table is aliased by {} for col: {}", tableName, resultColumn);
					}
				}
			} else {
				tableName = resultColumn.getTableName();
				if( tableName == null ) {
					_log.error("Result column does not have a table name: {}", resultColumn);
					continue;
				}
				fromTable = tableAliases.get(tableName);
				if( fromTable != null ) {
					fromToResult.put(fromTable, resultColumn);
				} else {
					_log.error("No table is aliased by {} for col: {}", tableName, resultColumn);
				}
			}
		}
		return fromToResult;
	}
	
	/**
	 * Gets the map from exposed table names to the from-list elements they reference.
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
	 * @return the table alias map
	 * @throws SQLTutorException if no map has been built yet
	 */
	public Map<String, FromTable> getTableAliases() {
		if( tableAliases == null )
			throw new SQLTutorException("No alias map established.");
		return tableAliases;
	}

	/**
	 * Returns a mapping from table references to result columns.
	 * @throws SQLTutorException if no map has been built yet
	 */
	public Multimap<FromTable, ResultColumn> getFromToResult() {
		if( fromToResult == null )
			throw new SQLTutorException("No from-to-result map established.");
		return fromToResult;
	}
}
