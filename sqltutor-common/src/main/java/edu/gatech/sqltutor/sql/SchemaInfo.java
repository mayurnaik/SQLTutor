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
package edu.gatech.sqltutor.sql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.gatech.sqltutor.DatabaseTable;
import edu.gatech.sqltutor.JDBCConstants.ColumnPositions.GetSchemas;
import edu.gatech.sqltutor.QueryUtils;

/**
 * Metadata about a particular schema.
 */
public class SchemaInfo {
	
	private String catalog;
	private String name;
	private List<DatabaseTable> tables = new ArrayList<>();

	public SchemaInfo() {
	}
	
	public SchemaInfo(String name) {
		this.name = name;
	}
	
	public SchemaInfo(ResultSet rs) throws SQLException {
		this.name = rs.getString(GetSchemas.TABLE_SCHEM);
		this.catalog = rs.getString(GetSchemas.TABLE_CAT);
	}
	
	/**
	 * Loads the table info from a database connection.
	 * Note this uses the schema name to load the tables, use 
	 * {@link #loadTableInfo(ResultSet, DatabaseMetaData)} if you need more control.
	 * 
	 * @param meta  the database metadata
	 * @throws SQLException if thrown by the underlying API
	 */
	public void loadTableInfo(DatabaseMetaData meta) throws SQLException {
		setTables(QueryUtils.readTableInfo(meta, name));
	}
	
	/**
	 * Loads the table info from a database connection.
	 * 
	 * @param rs  the result of {@link DatabaseMetaData#getTables(String, String, String, String[])} for this schema
	 * @throws SQLException if thrown by the underlying API
	 */
	public void loadTableInfo(ResultSet rs, DatabaseMetaData meta) throws SQLException {
		tables.clear();
		while( rs.next() ) {
			tables.add(new DatabaseTable(rs, meta));
		}
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DatabaseTable> getTables() {
		return tables;
	}

	public void setTables(List<DatabaseTable> tables) {
		this.tables = tables;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(catalog, name, tables);
	}
	
	@Override
	public boolean equals(Object obj) {
		if( this == obj )
			return true;
		if( obj == null || !obj.getClass().equals(this.getClass()) )
			return false;
		SchemaInfo that = (SchemaInfo)obj;
		return Objects.equals(this.catalog, that.catalog) &&
				Objects.equals(this.name, that.name) &&
				Objects.equals(this.tables, that.tables);
	}
	
	@Override
	public String toString() {
		return String.format(
			"SchemaInfo{catalog=%s, name=%s, tables=%s}",
			catalog, name, tables
		);
	}
}
