package objects;

import java.util.ArrayList;

public class QueryResult {
    private ArrayList<String> columnNames = new ArrayList<String>();
    private ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    
    public QueryResult(String databaseName, ArrayList<String> columnNames, ArrayList<ArrayList<String>> data) {
    	this.columnNames = columnNames;
    	this.data = data;
    }

	public void setData(ArrayList<ArrayList<String>> data) {
		this.data = data;
	}

	public ArrayList<ArrayList<String>> getData() {
		return data;
	}

	public void setColumnNames(ArrayList<String> columnNames) {
		this.columnNames = columnNames;
	}

	public ArrayList<String> getColumnNames() {
		return columnNames;
	}

	
}
