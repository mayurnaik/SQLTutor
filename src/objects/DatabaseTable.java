package objects;

import java.util.ArrayList;
import utilities.JDBC_Abstract_Connection;

public class DatabaseTable {
	private String tableName;	
	private ArrayList<String> columnNameList;	// list of column names
	private String columnNames;	//column names as a single string
	
	public DatabaseTable(String tableName, JDBC_Abstract_Connection connection, String selectedDatabase) {
		this.tableName = tableName;
		// get the attributes for said table.
		columnNameList = connection.getTableColumns(selectedDatabase, getTableName());
		columnNames = getColumnNameList().toString();
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

	public String getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}
}
