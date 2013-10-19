package objects;

import java.util.ArrayList;
import java.util.List;

public class QueryResult {
    private List<String> columns = new ArrayList<String>();
    private List<List<String>> data = new ArrayList<List<String>>();
    
    public QueryResult() {}
    
    public QueryResult(String databaseName, List<String> columns, List<List<String>> data) {
    	this.columns = columns;
    	this.data = data;
    }

	public void setData(List<List<String>> data) {
		this.data = data;
	}

	public List<List<String>> getData() {
		return data;
	}
	

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public List<String> getColumns() {
		return columns;
	}
}
