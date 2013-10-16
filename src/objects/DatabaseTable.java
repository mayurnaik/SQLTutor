package objects;

import java.util.ArrayList;

public class DatabaseTable {
	private String tableName;	
	private ArrayList<String> columnNameList;
	
	public DatabaseTable(String tableName) {
		this.tableName = tableName;
	}
	
	public DatabaseTable(String tableName, ArrayList<String> columnNameList) {
		this.tableName = tableName;
		this.columnNameList = columnNameList;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getTableName() {
		return tableName;
	}

	public ArrayList<String> getColumnNameList() {
		return columnNameList;
	}

	public void setColumnNameList(ArrayList<String> columnNameList) {
		this.columnNameList = columnNameList;
	}
}
