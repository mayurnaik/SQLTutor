package edu.gatech.sqltutor.beans;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

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
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		List<String> tableNames = new ArrayList<String>();
		
		for(DatabaseTable table : tables) {
			tableNames.add(table.getTableName());
		}
		
		try {
			setTableData(getDatabaseManager().getAllDevData(tableNames));
		} catch (SQLException e) {
			e.printStackTrace();
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
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						message, "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					message, "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
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
