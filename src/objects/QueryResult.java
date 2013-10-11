package objects;

import java.util.ArrayList;

public class QueryResult {
	private boolean malformed = false;
	private String exceptionMessage;
    private ArrayList<String> columnNames = new ArrayList<String>();
    private ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    
    public QueryResult() {}
    
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

	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}

	public boolean isMalformed() {
		return malformed;
	}

	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	
}
