package beans;

import javax.annotation.PostConstruct;
import javax.faces.bean.*;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import objects.DatabaseSchema;
import objects.QueryResult;
import objects.Question;
import utilities.JDBC_Abstract_Connection;
import utilities.JDBC_MySQL_Connection;
import utilities.JDBC_PostgreSQL_Connection;

@ManagedBean
@ViewScoped
public class DevTutorialBean {
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	private JDBC_Abstract_Connection connection;
	private String selectedDatabase;
	private DatabaseSchema databaseSchema;
	private String query;
	private String feedbackNLP;
	private QueryResult queryResult;

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
		databaseSchema = new DatabaseSchema(connection, selectedDatabase);
	}
	
	public void devRedirect() throws IOException {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (!userBean.isLoggedIn() || !userBean.getUsername().equalsIgnoreCase("dev")) {
	    	externalContext.redirect(externalContext.getRequestContextPath() + "/LoginPage.jsf");
	    }
	}
	
	public void processSQL() {
		queryResult = connection.getQueryResult(selectedDatabase, query);
		if(queryResult.isMalformed()) {
			feedbackNLP = "Your query was malformed. Please try again. Exception: \n" + queryResult.getExceptionMessage();
		} else {
			feedbackNLP = "The question you answered was: \n" + (new Question(query, databaseSchema)).getQuestion();
		}
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

	public DatabaseSchema getDatabaseSchema() {
		return databaseSchema;
	}
}
