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
import java.util.Objects;

import edu.gatech.sqltutor.JDBCConstants.ColumnPositions.GetColumns;

/**
 * Column metadata.  Note that not every property returned by 
 * JDBC is necesarily stored, we will expand them as needed.
 * 
 * @see DatabaseMetaData#getColumns(String, String, String, String)
 */
public class ColumnInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String catalog;
	private String schema;
	private String table;
	private String name;
	private int type;
	private String typeName;
	private int size;
	private int nullable;
	private String defaultValue;
	private int position;

	public ColumnInfo() {
	}
	
	public ColumnInfo(ResultSet info) throws SQLException {
		catalog      = info.getString(GetColumns.TABLE_CAT);
		schema       = info.getString(GetColumns.TABLE_SCHEM);
		table        = info.getString(GetColumns.TABLE_NAME);
		name         = info.getString(GetColumns.COLUMN_NAME);
		type         = info.getInt(GetColumns.DATA_TYPE);
		typeName     = info.getString(GetColumns.TYPE_NAME);
		size         = info.getInt(GetColumns.COLUMN_SIZE);
		nullable     = info.getInt(GetColumns.NULLABLE);
		position     = info.getInt(GetColumns.ORDINAL_POSITION);
		defaultValue = info.getString(GetColumns.COLUMN_DEF);
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

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/** @see java.sql.Types */
	public int getType() {
		return type;
	}

	/** @see java.sql.Types */
	public void setType(int type) {
		this.type = type;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @see DatabaseMetaData#columnNoNulls
	 * @see DatabaseMetaData#columnNullable
	 * @see DatabaseMetaData#columnNullableUnknown
	 */
	public int getNullable() {
		return nullable;
	}

	/**
	 * @param nullable the nullability of this column
	 * @throws IllegalArgumentException if <code>nullable</code> is not one of <code>DatabaseMetaData.column(NoNulls|Nullable|NullableUnknown)</code>
	 */
	public void setNullable(int nullable) {
		switch( nullable ) {
		case DatabaseMetaData.columnNoNulls:
		case DatabaseMetaData.columnNullable:
		case DatabaseMetaData.columnNullableUnknown:
			this.nullable = nullable;
			break;
		default:
			throw new IllegalArgumentException("Invalid nullable value: " + nullable);
		}
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public int hashCode() {
		return Objects.hash(catalog, schema, table, name, type, typeName, size, nullable, position, defaultValue);
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == this )
			return true;
		if( obj == null || !obj.getClass().equals(this.getClass()) )
			return false;
		ColumnInfo that = (ColumnInfo)obj;
		return Objects.equals(this.catalog, that.catalog) &&
				Objects.equals(this.schema, that.schema) &&
				Objects.equals(this.table, that.table) &&
				Objects.equals(this.name, that.name) &&
				this.type == that.type &&
				Objects.equals(this.typeName, that.typeName) &&
				this.size == that.size &&
				this.nullable == that.nullable &&
				this.position == that.position &&
				Objects.equals(this.defaultValue, that.defaultValue);
	}
	
	@Override
	public String toString() {
		return String.format(
			"ColumnInfo{catalog=%s, schema=%s, table=%s, name=%s, type=%d, typeName=%s, size=%d, nullable=%d, position=%d, defaultValue=%s}",
		    catalog, schema, table, name, type, typeName, size, nullable, position, defaultValue
		);
	}
}
