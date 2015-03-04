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
package edu.gatech.sqltutor.util;

import java.io.Serializable;
import java.sql.SQLException;

import edu.gatech.sqltutor.QueryResult;

public class QueryThread extends Thread implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String schemaName;
	private String query;
	private boolean dev;
	private DatabaseManager databaseManager;
	private SQLException exception;
	private QueryResult queryResult;
	
	public QueryThread(String schemaName, String query, boolean dev, DatabaseManager databaseManager) {
		setSchemaName(schemaName);
		setQuery(query);
		setDev(dev);
		setDatabaseManager(databaseManager);
	}
	
	public void run() {
		try {
			setQueryResult(databaseManager.getQueryResult(schemaName, query, dev));
		} catch (SQLException e) {
			e.printStackTrace();
			setException(e);
		} 
	}
	
	public String getSchemaName() {
		return schemaName;
	}
	
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public boolean isDev() {
		return dev;
	}
	
	public void setDev(boolean dev) {
		this.dev = dev;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public SQLException getException() {
		return exception;
	}

	public void setException(SQLException exception) {
		this.exception = exception;
	}

	public QueryResult getQueryResult() {
		return queryResult;
	}

	public void setQueryResult(QueryResult queryResult) {
		this.queryResult = queryResult;
	}
	
}
