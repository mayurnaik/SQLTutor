package edu.gatech.sqltutor.beans;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import objects.DatabaseTable;
import objects.QueryResult;
import objects.Question;
import utilities.JDBC_Abstract_Connection;
import utilities.JDBC_MySQL_Connection;
import utilities.JDBC_PostgreSQL_Connection;
import beans.UserBean;
import edu.gatech.sqltutor.IQueryTranslator;
import edu.gatech.sqltutor.entities.UserQuery;

@ManagedBean
@ViewScoped
public class FreeEntryPageBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	private JDBC_Abstract_Connection connection;
	private String selectedDatabase;
	private ArrayList<DatabaseTable> tables;
	private String query;
	private String feedbackNLP;
	private QueryResult queryResult;
	
	private UserQuery userQuery;

	@PostConstruct
	public void init() {
		String[] databaseAttributes;
		if(getUserBean().getSelectedDatabase() != null) {
			databaseAttributes = getUserBean().getSelectedDatabase().split(" ");
		} else {
			return; //eventually redirect to session expired page.
		}
		final String databaseConnector = databaseAttributes[0];
		if(databaseConnector.equalsIgnoreCase("PostgreSQL")) {	
			connection = new JDBC_PostgreSQL_Connection();
		} else if (databaseConnector.equalsIgnoreCase("MySQL")) {
			connection = new JDBC_MySQL_Connection();
		} else {
			return; //eventually redirect to message about connector not being supported
		}
		selectedDatabase = databaseAttributes[1];
		tables = connection.getTables(selectedDatabase);
	}
	
	public void devRedirect() throws IOException {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (!userBean.isLoggedIn() || !userBean.isDevUser()) {
	        externalContext.redirect(externalContext.getRequestContextPath() + "/HomePage.jsf");
	    }
	}
	
	public void processSQL() {
		queryResult = connection.getQueryResult(selectedDatabase, query);
		if(queryResult.isMalformed()) {
			feedbackNLP = "Your query was malformed. Please try again. Exception: \n" + queryResult.getExceptionMessage();
		} else {
			IQueryTranslator question = new Question(query, tables);
			String nlp = question.getTranslation();
			userQuery = new UserQuery();
			userQuery.setUser(userBean);
			userQuery.setTime(new Date());
			userQuery.setQuery(query);
			userQuery.setNaturalLanguage(nlp);
			
			feedbackNLP = "The question you answered was: \n" + nlp;
		}
	} 

	public UserQuery getUserQuery() {
		return userQuery;
	}
	
	public void setUserQuery(UserQuery userQuery) { 
		this.userQuery = userQuery;
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

	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public String getFeedbackNLP() {
		return feedbackNLP;
	}
	
	public String getSelectedDatabase() {
		return selectedDatabase;
	}

	public List<DatabaseTable> getTables() {
		return tables;
	}
}
