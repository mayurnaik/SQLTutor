/*
 *   Copyright (c) 2015 Program Analysis Group, Georgia Tech
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
import java.util.ArrayList;
import java.util.List;

public class QueryResult implements Serializable {
	private static final long serialVersionUID = 1L;
	/** 
	 * The maximum results that should be stored in a <code>QueryResult</code> before setting 
	 * the truncated flag.
	 */
	public static final int QUERY_SIZE_LIMIT = 50_000;
	/**
	 * The maximum results that should be read for the original size of a <code>QueryResult</code> 
	 * before setting the read limit flag.
	 */
	public static final int QUERY_READ_LIMIT = 500_000;
	
	private List<String> columns = new ArrayList<String>();
    private List<List<String>> data = new ArrayList<List<String>>();
    private int originalSize;
    private boolean truncated;
    private boolean readLimitExceeded;
    private long executionTime = -1L;
    private long totalTime = -1L;
    
    public QueryResult() {}
    
    @SuppressWarnings("unchecked")
	public QueryResult(QueryResult queryResult) {
    	this.columns = (List<String>) ((ArrayList<String>)queryResult.getColumns()).clone();
    	this.data = (List<List<String>>) ((ArrayList<List<String>>)queryResult.getData()).clone();
    }
    
    public QueryResult(List<String> columns, List<List<String>> data) {
    	this.columns = columns;
    	this.data = data;
    	if (data != null)
    		originalSize = data.size();
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
	
	/**
	 * Indicates whether this result was truncated due to query limits.
	 * @return if the data is truncated
	 */
	public boolean isTruncated() {
		return truncated;
	}
	
	public void setTruncated(boolean truncated) {
		this.truncated = truncated;
	}
	
	/**
	 * Returns the original size of this result.  This will be the same 
	 * as the size of the data if <code>isTruncated()</code> returns <code>false</code>.
	 * @return the original size of this result
	 */
	public int getOriginalSize() {
		return originalSize;
	}
	
	public void setOriginalSize(int originalSize) {
		this.originalSize = originalSize;
	}
	
	/**
	 * Returns whether the read limit was exceeded for this result.
	 * @return
	 */
	public boolean isReadLimitExceeded() {
		return readLimitExceeded;
	}
	
	public void setReadLimitExceeded(boolean readLimitExceeded) {
		this.readLimitExceeded = readLimitExceeded;
	}
	
	/**
	 * Returns the query execution time in milliseconds.  This is the time taken 
	 * before results started being returned.
	 * @return
	 */
	public long getExecutionTime() {
		return executionTime;
	}
	
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
	
	/**
	 * Returns the total query time in milliseconds.  This is the query execution 
	 * time plus time spent reading results.
	 * @return
	 */
	public long getTotalTime() {
		return totalTime;
	}
	
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}
}
