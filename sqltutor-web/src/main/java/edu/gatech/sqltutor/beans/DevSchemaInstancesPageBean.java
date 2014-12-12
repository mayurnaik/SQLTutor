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
package edu.gatech.sqltutor.beans;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.context.RequestContext;

import edu.gatech.sqltutor.DatabaseTable;
import edu.gatech.sqltutor.QueryResult;

@ManagedBean
@ViewScoped
public class DevSchemaInstancesPageBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;

	private List<DatabaseTable> tables;
	private HashMap<String, QueryResult> tableData;

	private String query;
	private QueryResult queryResult;
	
	@PostConstruct
	public void init() {
		setupTables();
	}
	
	public void setupTables() {
		try {
			tables = getDatabaseManager().getDevTables();
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR);
		}

		List<String> tableNames = new ArrayList<String>();
		
		for(DatabaseTable table : tables) {
			tableNames.add(table.getTableName());
		}
		
		try {
			setTableData(getDatabaseManager().getAllDevData());
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR);
		}
	}
	
	public void processSQL() {
		try {
			queryResult = getDatabaseManager().getDevQueryResult(query, userBean.isDeveloper());
		} catch(SQLException e) {
			queryResult = null;
			String message = e.getMessage();
			
			if(message.equals("No results were returned by the query.")) {
				RequestContext requestContext = RequestContext.getCurrentInstance();  
				requestContext.execute("window.location.replace(window.location.href);");
				BeanUtils.addInfoMessage(null, message);
			} else
				BeanUtils.addErrorMessage(null, message);
		}
	} 

	public List<DatabaseTable> getTables() {
		return tables;
	}

	public void setTables(List<DatabaseTable> tables) {
		this.tables = tables;
	}
	
	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public QueryResult getQueryResult() {
		return queryResult;
	}

	public void setQueryResult(QueryResult queryResult) {
		this.queryResult = queryResult;
	}

	public HashMap<String, QueryResult> getTableData() {
		return tableData;
	}

	public void setTableData(HashMap<String, QueryResult> tableData) {
		this.tableData = tableData;
	}
}
