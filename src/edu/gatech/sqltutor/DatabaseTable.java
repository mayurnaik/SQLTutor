package edu.gatech.sqltutor;

import java.util.List;

public class DatabaseTable {
	private String tableName;	
	private List<String> columns;
	
	public DatabaseTable(String tableName) {
		this.tableName = tableName;
	}
	
	public DatabaseTable(String tableName, List<String> columns) {
		this.tableName = tableName;
		this.columns = columns;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getTableName() {
		return tableName;
	}
	

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}	
}
