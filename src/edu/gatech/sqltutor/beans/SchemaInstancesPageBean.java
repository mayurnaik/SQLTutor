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

import org.primefaces.model.DualListModel;

import objects.DatabaseTable;
import objects.QueryResult;
import objects.QuestionTuple;
import utilities.JDBC_Abstract_Connection;
import utilities.JDBC_PostgreSQL_Connection;
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
	
	private JDBC_Abstract_Connection connection;
	
	private List<DatabaseTable> tables;
	
	private String selectedSchema;

	private String query;
	private QueryResult queryResult;
	
	@PostConstruct
	public void init() {
		selectedSchema = userBean.getSelectedSchema();
		connection = new JDBC_PostgreSQL_Connection();
		tables = connection.getTables(selectedSchema);
	}
	
	public void processSQL() {
		try {
			queryResult = connection.getQueryResult(selectedSchema, query, userBean.isDevUser());
		} catch(SQLException e) {
			queryResult = null;
			String message = e.getMessage();
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					message, "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			
			if(message.equals("No results were returned by the query.")) {
				tables = connection.getTables(selectedSchema);
			}
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
}
