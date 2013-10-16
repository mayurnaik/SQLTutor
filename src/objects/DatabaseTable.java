package objects;

import java.util.List;

public class DatabaseTable {
	private String tableName;	
	private List<String> columnNameList;
	
	public DatabaseTable(String tableName) {
		this.tableName = tableName;
	}
	
	public DatabaseTable(String tableName, List<String> columnNameList) {
		this.tableName = tableName;
		this.columnNameList = columnNameList;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getTableName() {
		return tableName;
	}
	

	public List<String> getColumnNames() {
		return columnNameList;
	}

	public void setColumnNames(List<String> columnNameList) {
		this.columnNameList = columnNameList;
	}	

	@Deprecated
	public List<String> getColumnNameList() {
		return columnNameList;
	}

	@Deprecated
	public void setColumnNameList(List<String> columnNameList) {
		this.columnNameList = columnNameList;
	}
}
