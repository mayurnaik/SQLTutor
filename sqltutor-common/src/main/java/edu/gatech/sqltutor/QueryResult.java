/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
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
	
	private List<String> columns = new ArrayList<String>();
    private List<List<String>> data = new ArrayList<List<String>>();
    private int originalSize;
    private boolean isTruncated;
    
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
		return isTruncated;
	}
	
	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
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
}
