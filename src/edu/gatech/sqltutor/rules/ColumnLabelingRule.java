package edu.gatech.sqltutor.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.rules.graph.TranslationGraph;

/**
 * Applies a non-default label to 1 or more columns.
 * <p>
 * If more than one column is specified, the label is given 
 * to the first column and the remaining columns are marked 
 * as being excluded from the output.
 * </p>
 */
public class ColumnLabelingRule implements ITranslationRule {
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
	
	private String label;
	private List<ColumnDescription> columns = new ArrayList<ColumnDescription>();
	
	public ColumnLabelingRule(String label, String... columns) {
		this.label = label;
		for( String c: columns ) {
			this.columns.add(new ColumnDescription(c));
		}
	}

	@Override
	public int getPrecedence() {
		return 90;
	}

	@Override
	public boolean apply(TranslationGraph graph, StatementNode statement) {
		SelectNode select = QueryUtils.extractSelectNode(statement);
		
		Set<ColumnDescription> toMatch = new HashSet<ColumnDescription>(this.columns);
		
		// TODO Auto-generated method stub
		return false;
	}

}
