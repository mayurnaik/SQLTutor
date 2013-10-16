package objects;

import java.util.ArrayList;
import java.util.List;

public class QueryResult {
	private boolean malformed = false;
	private String exceptionMessage;
    private List<String> columnNameList = new ArrayList<String>();
    private List<List<String>> data = new ArrayList<List<String>>();
    
    public QueryResult() {}
    
    public QueryResult(String databaseName, List<String> columnNameList, List<List<String>> data) {
    	this.columnNameList = columnNameList;
    	this.data = data;
    }

	public void setData(List<List<String>> data) {
		this.data = data;
	}

	public List<List<String>> getData() {
		return data;
	}
	

	public void setColumnNames(List<String> columnNameList) {
		this.columnNameList = columnNameList;
	}

	public List<String> getColumnNames() {
		return columnNameList;
	}
	
	@Deprecated
	public void setColumnNameList(List<String> columnNameList) {
		this.columnNameList = columnNameList;
	}

	@Deprecated
	public List<String> getColumnNameList() {
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
