package edu.gatech.sqltutor.beans;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
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

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.IQueryTranslator;
import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.entities.UserQuery;

@ManagedBean
@ViewScoped
public class FreeEntryPageBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	private JDBC_Abstract_Connection connection;
	private String selectedDatabase;
	private List<DatabaseTable> tables;
	private String query;
	private String feedbackNLP;
	private QueryResult queryResult;
	private String userDescription;
	
	private UserQuery userQuery;
	
	private List<UserQuery> userQueries;

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
		
		userQueries = connection.getUserQueries();
	}
	
	public void devRedirect() throws IOException {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (!userBean.isLoggedIn() || !userBean.isDevUser()) {
	        externalContext.redirect(externalContext.getRequestContextPath() + "/HomePage.jsf");
	    }
	}
	
	public void addQuery() {
		SQLParser parser = new SQLParser();
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			// make sure it parses and is a SELECT
			query = QueryUtils.sanitize(query);
			StatementNode statement = parser.parseStatement(query);
			try {
				SelectNode selectNode = QueryUtils.extractSelectNode(statement);
			} catch( IllegalArgumentException e ) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Query must be a SELECT query.", null);
				context.addMessage(null, message);
				return;
			}
			
			UserQuery userQuery = new UserQuery();
			userQuery = new UserQuery();
			userQuery.setUsername(userBean.getUsername());
			userQuery.setTime(new Date());
			userQuery.setQuery(query);
			userQuery.setSchema(selectedDatabase);
			userQuery.setUserDescription(userDescription);
			
			connection.saveUserQuery(userQuery);
			userQueries.add(userQuery);
			
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Query saved successfully.", null);
			context.addMessage(null, message);
			
		} catch( StandardException e ) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
				"The query could not be parsed: " + e.getMessage(), null);
			context.addMessage(null, message);
		} catch( RuntimeException e ) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
				"Internal error saving the query.", null);
			context.addMessage(null, message);
			e.printStackTrace();
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
			userQuery.setUsername(userBean.getUsername());
			userQuery.setTime(new Date());
			userQuery.setQuery(query);
			userQuery.setNaturalLanguage(nlp);
			
			feedbackNLP = "The question you answered was: \n" + nlp;
		}
	}
	
	public List<UserQuery> getUserQueries() {
		return userQueries;
	}
	
	public void setUserQueries(List<UserQuery> userQueries) {
		this.userQueries = userQueries;
	}

	public String getUserDescription() {
		return userDescription;
	}
	
	public void setUserDescription(String userDescription) {
		this.userDescription = userDescription;
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
