package edu.gatech.sqltutor.beans;

import java.io.IOException;
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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.context.RequestContext;
import org.primefaces.model.DualListModel;

import objects.DatabaseTable;
import objects.QueryResult;
import objects.QuestionTuple;

import beans.UserBean;
import edu.gatech.sqltutor.DatabaseManager;

@ManagedBean
@ViewScoped
public class SchemaInstancesPageBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	@ManagedProperty(value="#{databaseManager}")
	private DatabaseManager databaseManager;

	private List<DatabaseTable> tables;
	private HashMap<String, QueryResult> tableData;
	
	private String selectedSchema;

	private String query;
	private QueryResult queryResult;
	
	@PostConstruct
	public void init() {
		selectedSchema = userBean.getSelectedSchema();
		setupTables();
	}
	
	public void setupTables() {
		try {
			tables = databaseManager.getTables(selectedSchema);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		List<String> tableNames = new ArrayList<String>();
		
		for(DatabaseTable table : tables) {
			tableNames.add(table.getTableName());
		}
		
		try {
			setTableData(databaseManager.getAllData(selectedSchema, tableNames));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void processSQL() {
		try {
			boolean hasPermissions = databaseManager.checkSchemaPermissions(userBean.getEmail(), userBean.getSelectedSchema());

			if(!hasPermissions) {
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"You do not have permissions for this schema.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			queryResult = databaseManager.getQueryResult(selectedSchema, query, userBean.isDevUser());
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

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(DatabaseManager dbManager) {
		this.databaseManager = dbManager;
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
