package objects;

import java.util.ArrayList;

public class QueryResult {
	private boolean malformed = false;
	private String exceptionMessage;
    private ArrayList<String> columnNameList = new ArrayList<String>();
    private ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    
    public QueryResult() {}
    
    public QueryResult(String databaseName, ArrayList<String> columnNameList, ArrayList<ArrayList<String>> data) {
    	this.columnNameList = columnNameList;
    	this.data = data;
    }

	public void setData(ArrayList<ArrayList<String>> data) {
		this.data = data;
	}

	public ArrayList<ArrayList<String>> getData() {
		return data;
	}

	public void setColumnNameList(ArrayList<String> columnNameList) {
		this.columnNameList = columnNameList;
	}

	public ArrayList<String> getColumnNameList() {
		return columnNameList;
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
