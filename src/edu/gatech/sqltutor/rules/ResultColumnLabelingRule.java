package edu.gatech.sqltutor.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.graph.LabelNode;
import edu.gatech.sqltutor.rules.graph.TranslationEdge;
import edu.gatech.sqltutor.rules.graph.TranslationGraph;
import edu.gatech.sqltutor.rules.util.ParserVisitorAdapter;

/**
 * Applies a non-default label to 1 or more columns.
 * <p>
 * If more than one column is specified, the label is given 
 * to the first column and the remaining columns are marked 
 * as being excluded from the output.
 * </p>
 */
public class ResultColumnLabelingRule implements ITranslationRule {
	/**
	 * Description of columns to match.
	 */
	public static class ColumnDescription {
		private final String tableName;
		private final String columnName;
		
		public ColumnDescription(String tableName, String columnName) {
			if( tableName == null ) throw new NullPointerException("tableName is null");
			if( columnName == null ) throw new NullPointerException("columnName is null");
			this.tableName = tableName;
			this.columnName = columnName;
		}
		
		public ColumnDescription(String fullColumnName) {
			if( fullColumnName == null ) throw new NullPointerException("fullColumnName is null");
			String[] parts = fullColumnName.split("\\.");
			if( parts.length != 2 )
				throw new IllegalArgumentException("Column name must be in <table>.<column> format: " + fullColumnName);
			this.tableName = parts[0];
			this.columnName = parts[1];
		}
		
		public String getColumnName() {
			return columnName;
		}
		
		public String getTableName() {
			return tableName;
		}
		
		public boolean matches() {
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
			result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if( this == obj )
				return true;
			if( obj == null )
				return false;
			if( getClass() != obj.getClass() )
				return false;
			ColumnDescription other = (ColumnDescription)obj;
			if( columnName == null ) {
				if( other.columnName != null )
					return false;
			} else if( !columnName.equals(other.columnName) )
				return false;
			if( tableName == null ) {
				if( other.tableName != null )
					return false;
			} else if( !tableName.equals(other.tableName) )
				return false;
			return true;
		}
	}
	
	private List<String> labels;
	private List<ColumnDescription> columns = new ArrayList<ColumnDescription>();

	/**
	 * Create a rule that applies a single label to a set of columns.
	 * @param label
	 * @param columns
	 */
	public ResultColumnLabelingRule(String label, Collection<String> columns) {
		this(Collections.singletonList(label), columns);
	}
	
	/**
	 * Create a rule that applies a series of labels to a set of columns.
	 * @param labels
	 * @param columns
	 */
	public ResultColumnLabelingRule(Collection<String> labels, Collection<String> columns) {
		if( columns == null || columns.size() < 1 )
			throw new IllegalArgumentException("Must have at least one column");
		
		this.labels = new ArrayList<String>(labels);
		String tableName = null;
		for( String c: columns ) {
			ColumnDescription desc = new ColumnDescription(c);
			if( tableName == null )
				tableName = desc.getTableName();
			else if( !tableName.equals(desc.getTableName()) )
				throw new IllegalArgumentException("FIXME: All columns must be from the same table.");
			this.columns.add(desc);
		}
	}

	@Override
	public int getPrecedence() {
		return 90;
	}

	@Override
	public boolean apply(final TranslationGraph graph, StatementNode statement) {
		SelectNode select = QueryUtils.extractSelectNode(statement);
		
		final Set<ColumnDescription> toMatch = new HashSet<ColumnDescription>(columns);
		final List<ColumnReference> matched = new ArrayList<ColumnReference>(columns.size());
		
		try {
			// look for the set of matches
			select.getResultColumns().accept(new ParserVisitorAdapter() {
				@Override
				public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
					if( node.getNodeType() != NodeTypes.COLUMN_REFERENCE )
						return node;
					
					if( QueryUtils.hasContributed(ResultColumnLabelingRule.this, node) )
						return node;
					
					ColumnReference ref = (ColumnReference)node;
					ColumnDescription desc = getDescriptionForReference(ref);
					
					if( toMatch.remove(desc) )
						matched.add(ref);
					
					return node;
				}
				
				@Override
				public boolean stopTraversal() {
					return toMatch.isEmpty();
				}
				
				private ColumnDescription getDescriptionForReference(ColumnReference ref) 
						throws StandardException {
					String tableName = ref.getTableName();
					LabelNode tableNode = graph.getTableVertex(tableName);
					if( tableNode == null )
						throw new SQLTutorException("Could not resolve table vertex for name: " + tableName);
					FromBaseTable fromTable = (FromBaseTable)tableNode.getAstNode();
					String baseTableName = fromTable.getOrigTableName().getTableName();
					
					return new ColumnDescription(baseTableName, ref.getColumnName());
				}
			});
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
		
		if( !toMatch.isEmpty() )
			return false;
		
		for( ColumnReference ref : matched ) {
			QueryUtils.getOrInitMetaData(ref).addContributor(this);
		}
		
		if( matched.size() == 1 ) {
			// special case, just expand local choices
			graph.getVertexForAST(matched.get(0)).addLocalChoices(this.labels);
		} else {
			LabelNode parentNode = new LabelNode();
			parentNode.setChildSuppressing(true);
			parentNode.addLocalChoices(this.labels);
			graph.addVertex(parentNode);
			
			for( ColumnReference ref : matched ) {
				LabelNode refNode = graph.getVertexForAST(ref);
				if( refNode == null )
					throw new SQLTutorException("No node for column reference: " + ref);
				
				// make new node the child of all parents of the referenced nodes
				Collection<TranslationEdge> incoming = new ArrayList<TranslationEdge>(graph.incomingEdgesOf(refNode));
				for( TranslationEdge inEdge: incoming ) {
					if( !inEdge.isChildEdge() )
						continue;
					graph.removeEdge(inEdge);
					graph.addEdge(inEdge.getSource(), parentNode, inEdge);
				}
				
				// make referenced nodes the child of the new node
				TranslationEdge edge = new TranslationEdge(true);
				graph.addEdge(parentNode, refNode, edge);
			}
		}
		return true;
	}

}
