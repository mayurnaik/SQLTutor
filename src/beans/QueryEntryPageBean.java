package beans;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import objects.DatabaseTable;
import objects.QueryResult;
import utilities.JDBC_Abstract_Connection;
import utilities.JDBC_MySQL_Connection;
import utilities.JDBC_PostgreSQL_Connection;

@ManagedBean
@ViewScoped
public class QueryEntryPageBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	private JDBC_Abstract_Connection connection;
	private String selectedDatabase;
	private List<DatabaseTable> tables;
	private String query;
	private String feedbackNLP;
	private QueryResult queryResult;
	private boolean queryMalformed;

	@PostConstruct
	public void init() {
		String[] databaseAttributes;
		if(getUserBean().getSelectedSchema() != null) {
			databaseAttributes = getUserBean().getSelectedSchema().split(" ");
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
		setPrepopulatedQuery();
		queryMalformed = false;
	}
	
	public void processSQL() {
		try {
			queryResult = connection.getQueryResult(selectedDatabase, query);
			queryMalformed = false;
		} catch(SQLException e) {
			feedbackNLP = "Your query was malformed. Please try again.\n" + e.getMessage();
			queryMalformed = true;
		}
	}
	
	public void setPrepopulatedQuery() {
		if(selectedDatabase.equals("company")) {
			query = "SELECT first_name, last_name, salary FROM employee";
		} else if(selectedDatabase.equals("sales")) {
			query = "SELECT CUST_NUM FROM customers";
		} else {
			query = "";
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

	public List<DatabaseTable> getTables() {
		return tables;
	}
	
	public boolean getQueryMalformed() {
		return queryMalformed;
	}
	
	public boolean highColumnCount() {
		if(queryResult != null && queryResult.getColumns().size() > 8)
			return true;
		return false;
	}
}
