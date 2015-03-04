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

import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.gatech.sqltutor.JDBCConstants.ColumnPositions.GetTables;

public class DatabaseTable implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String catalog;
	private String schema;
	private String tableName;
	private String type;
	
	private ColumnInfoList columns = new ColumnInfoList();
	
	/**
	 * Fully loads the table metadata.
	 * 
	 * @param tableInfo the result of <code>DatabaseMetaData.getTables</code> pointing to the 
	 *                  row to load 
	 * @param meta      the metadata object used to load column metadata
	 * @throws SQLException if thrown by the underlying API
	 * @see DatabaseMetaData#getTables(String, String, String, String[])
	 */
	public DatabaseTable(ResultSet tableInfo, DatabaseMetaData meta) throws SQLException {
		if( tableInfo == null ) throw new NullPointerException("tableInfo is null");
		catalog = tableInfo.getString(GetTables.TABLE_CAT);
		schema = tableInfo.getString(GetTables.TABLE_SCHEM);
		tableName = tableInfo.getString(GetTables.TABLE_NAME);
		type = tableInfo.getString(GetTables.TABLE_TYPE);
		
		if( meta != null )
			loadColumnMetaData(meta);
	}
	
	/**
	 * Loads the column metadata for this table.  This clears any 
	 * current column metadata.
	 * 
	 * @param meta the database metadata source
	 * @throws SQLException thrown by underlying API
	 */
	public void loadColumnMetaData(DatabaseMetaData meta) throws SQLException {
		if( meta == null ) throw new NullPointerException("meta is null");
		columns.clear();
		try (ResultSet rs = meta.getColumns(catalog, schema, tableName, null)) {
			while( rs.next() ) {
				columns.add(new ColumnInfo(rs));
			}
		}
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ColumnInfoList getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnInfo> columns) {
		if( columns instanceof ColumnInfoList )
			this.columns = (ColumnInfoList)columns;
		else
			this.columns = new ColumnInfoList(columns);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(catalog, schema, tableName, type, columns);
	}
	
	@Override
	public boolean equals(Object obj) {
		if( this == obj )
			return true;
		if( obj == null || !obj.getClass().equals(this.getClass()) )
			return false;
		DatabaseTable that = (DatabaseTable)obj;
		return Objects.equals(this.catalog, that.catalog) &&
				Objects.equals(this.schema, that.schema) &&
				Objects.equals(this.tableName, that.tableName) &&
				Objects.equals(this.type, that.type) &&
				Objects.equals(this.columns, that.columns);
	}
	
	@Override
	public String toString() {
		return String.format(
			"DatabaseTable{catalog=%s, schema=%s, tableName=%s, type=%s, columns=%s}", 
			catalog, schema, tableName, type, columns
		);
	}
	
	public List<String> getColumnNames() {
		List<String> names = new ArrayList<>(getColumns().size());
		for(ColumnInfo col : getColumns())
			names.add(col.getName());
		return names;
	}
}
