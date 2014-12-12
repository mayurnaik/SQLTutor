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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryResult implements Serializable {
	private static final long serialVersionUID = 1L;

    private Map<String, List<String>> columnDataMap = new HashMap<String, List<String>>();
    
    public QueryResult() {}

	public QueryResult(QueryResult queryResult) {
    	this.columnDataMap = new HashMap<String, List<String>>(queryResult.getColumnDataMap());
    }
    
    public QueryResult(Map<String, List<String>> columnDataMap) {
    	this.columnDataMap = new HashMap<String, List<String>>(columnDataMap);
    }
    
    public boolean isEmpty() {
    	for( String c : columnDataMap.keySet() ) {
    		if( !columnDataMap.get(c).isEmpty() )
    			return false;
    	}
    	return true;
    }
    
	public List<String> getData(String column) {
		return columnDataMap.get(column);
	}
	
	public List<List<String>> getData() {
		List<List<String>> data = new ArrayList<List<String>>();
		for(String c : columnDataMap.keySet()) 
			data.add(columnDataMap.get(c));
		return data;
	}

	public List<String> getColumns() {
		return new ArrayList<String>(columnDataMap.keySet());
	}

	public Map<String, List<String>> getColumnDataMap() {
		return columnDataMap;
	}
}
