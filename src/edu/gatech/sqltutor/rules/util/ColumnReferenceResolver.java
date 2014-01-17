package edu.gatech.sqltutor.rules.util;

import java.util.Collection;
import java.util.List;

import objects.DatabaseTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.FromList;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.TableName;
import com.akiban.sql.parser.Visitable;
import com.akiban.sql.parser.Visitor;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import edu.gatech.sqltutor.SQLTutorException;

/**
 * A resolver for unqualified column names.  Attempts to 
 * set the <code>TableName</code> based on the <code>FromList</code> 
 * entries and the schema metadata.
 */
public class ColumnReferenceResolver {
	private static final Logger log = LoggerFactory.getLogger(ColumnReferenceResolver.class);

	private class ASTVisitor implements Visitor {
		private FromList fromTables;
		private Multimap<String, FromTable> tableMap = LinkedHashMultimap.create();
		
		private ASTVisitor(SelectNode select) {
			this.fromTables = select.getFromList();
			
			try {
				this.fromTables.accept(new ParserVisitorAdapter() {
					@Override
					public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
						switch( node.getNodeType() ) {
							case NodeTypes.FROM_BASE_TABLE: {
								FromBaseTable table = (FromBaseTable)node;
								TableName origName = table.getOrigTableName();
								if( origName != null ) {
									tableMap.put(origName.getTableName(), table);
								} else {
									log.warn("No original table name (type={}): {}", table.getClass().getSimpleName(), table);
								}
								break;
							}
							case NodeTypes.FROM_SUBQUERY:
							case NodeTypes.FROM_VTI:
								log.warn("Cannot resolve against from-list type: {}", node.getClass().getSimpleName());
								break;
						}
						return node;
					}
				});
			} catch( StandardException e ) {
				throw new SQLTutorException(e);
			}
		}
		
		public void visitColumnReference(ColumnReference ref) throws StandardException {
			if( ref.getTableNameNode() != null )
				return;
			
			String name = ref.getColumnName();
			
			if( tables == null ) {
				// if we have no schema info and there is only one table, assume the column belongs to it
				if( fromTables.size() != 1 )
					throw new SQLTutorException("Could not resolve reference: " + ref);
				
				TableName tableName = fromTables.get(0).getTableName();
				ref.setTableNameNode(tableName);
				log.debug("Set tablename={} for ref={}", tableName, ref.getColumnName());
			} else {
				DatabaseTable colTable = null;
				for( DatabaseTable dbTable: tables ) {
					String dbTableName = dbTable.getTableName();
					
					// table must be in query and column must be in table
					if( !tableMap.containsKey(dbTableName) || 
							!dbTable.getColumns().contains(name) ) {
						continue;
					}
					
					// check for ambiguity
					if( colTable != null ) {
						throw new SQLTutorException(String.format(
							"Ambiguous name '%s', matches tables '%s' and '%s'",
							name, colTable.getTableName(), dbTable.getTableName()));
					}
					
					colTable = dbTable;
				}
				
				if( colTable == null ) {
					throw new SQLTutorException("Could not resolve ref: " + ref);
				} else {
					// make sure the reference is unambiguous (to only one from-list table)
					Collection<FromTable> fromTables = tableMap.get(colTable.getTableName());
					if( fromTables.size() > 1 )
						throw new SQLTutorException("Reference is ambiguous, multiple instances of the matching table: " + ref);
					
					TableName tableName = fromTables.iterator().next().getTableName();
					ref.setTableNameNode(tableName);
					log.debug("Set tablename={} for ref={}", tableName, ref.getColumnName());
				}
			}
			
		}
		
		@Override
		public Visitable visit(Visitable node) throws StandardException {
			QueryTreeNode astNode = (QueryTreeNode)node;
			switch( astNode.getNodeType() ) {
				case NodeTypes.COLUMN_REFERENCE: {
					ColumnReference ref = (ColumnReference)astNode;
					visitColumnReference(ref);
					break;
				}
			}
			return node;
		}
		
		@Override
		public boolean skipChildren(Visitable node) throws StandardException {
			return false;
		}
		
		@Override
		public boolean visitChildrenFirst(Visitable node) {
			return false;
		}
		
		@Override
		public boolean stopTraversal() {
			return false;
		}
	}
	
	private List<DatabaseTable> tables;
	
	public ColumnReferenceResolver(List<DatabaseTable> tables) {
		this.tables = tables;
	}
	
	public void resolve(SelectNode select) {
		try {
			select.accept(new ASTVisitor(select));
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
	}
}
