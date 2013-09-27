package beans;

import javax.annotation.PostConstruct;
import javax.faces.bean.*;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.ArrayList;

import objects.DatabaseSchema;
import objects.QueryResult;
import objects.Question;
import utilities.JDBC_Abstract_Connection;
import utilities.JDBC_MySQL_Connection;
import utilities.JDBC_PostgreSQL_Connection;

@ManagedBean
@ViewScoped
public class TutorialBean {
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	private JDBC_Abstract_Connection connection;
	private String selectedDatabase = "";
	private DatabaseSchema databaseSchema;
	private ArrayList<String> questions = new ArrayList<String>();
	private ArrayList<String> answers = new ArrayList<String>();
	private int questionIndex;
	private String query;
	private QueryResult queryResult;
	private String resultSetFeedback;
	private String feedbackNLP;

	@PostConstruct
	public void init() {
		String[] databaseAttributes;
		if(getUserBean().getSelectedDatabase() != null) {
			databaseAttributes = getUserBean().getSelectedDatabase().split(" ");
		} else {
			return; //redirect to session expired page.
		}
		final String databaseConnector = databaseAttributes[0];
		if(databaseConnector.equalsIgnoreCase("PostgreSQL")) {		// this may be replaced for a switch statement in Java 1.7
			connection = new JDBC_PostgreSQL_Connection();
		} else if (databaseConnector.equalsIgnoreCase("MySQL")) {
			connection = new JDBC_MySQL_Connection();
		} else {
			return; //redirect to message about connector not being supported
		}
		selectedDatabase += databaseAttributes[1];
		databaseSchema = new DatabaseSchema(connection, selectedDatabase);
		setQuestionsAndAnswers();
	}
	
	public void loginRedirect() throws IOException {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
	    if (!userBean.isLoggedIn()) {
	        externalContext.redirect(externalContext.getRequestContextPath() + "/LoginPage.jsf");
	    } 
	}
	
	public void processSQL() {
		resultSetFeedback = connection.getQueryFeedback(selectedDatabase, query, answers.get(questionIndex));
		// if the feedback contains an SQL exception, we do not render the table (we empty it).
		if(!resultSetFeedback.substring(0,16).equalsIgnoreCase("Query malformed.")) {
			// produce NLP feedback for the user.
			feedbackNLP = "The question you answered was: \n" + (new Question(query)).getQuestion();
			// fill the query's resulting table
			queryResult = new QueryResult(selectedDatabase, 
					connection.getQueryColumns(selectedDatabase, query), 
					connection.getQueryData(selectedDatabase, query));
		} else {
			feedbackNLP = "Your query was malformed. Please try again. \n";	// perhaps add better exception handling here
			queryResult = null;
		}
	} 
	
	public void setQuestionsAndAnswers() {
		// currently hard coded answers (which get converted to questions). This will be phased out.
		if (selectedDatabase.equalsIgnoreCase("company")) {
			answers.clear();
			questions.clear();
			answers.add("SELECT * FROM department");
			answers.add("SELECT first_name, last_name FROM employee");
			answers.add("SELECT salary FROM employee WHERE first_name = 'Ahmad'");
			Question question;
			for(int i = 0; i < answers.size(); i++ ) {
				question = new Question(answers.get(i));
				questions.add(question.getQuestion());
			}
		} else {	// just a place holder for every other database.
			answers.add("");
			questions.add("");
		}
	}
	
	public void nextQuestion() {	// reset everything and move to the next question.
		questionIndex++;
		if(questionIndex >= questions.size()) {
			questionIndex = 0;
		}
		queryResult=null;
		resultSetFeedback = "";
		feedbackNLP = "";
	}

	public String getQuestion() {
		return questions.get(questionIndex);
	}
	
	public String getResultSetFeedback() {
		return resultSetFeedback;
	}

	public void setResultSetFeedback(String resultSetFeedback) {
		this.resultSetFeedback = resultSetFeedback;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setQueryResult(QueryResult queryResult) {
		this.queryResult = queryResult;
	}

	public QueryResult getQueryResult() {
		return queryResult;
	}

	public void setAnswers(ArrayList<String> answers) {
		this.answers = answers;
	}

	public ArrayList<String> getAnswers() {
		return answers;
	}

	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public void setFeedbackNLP(String feedbackNLP) {
		this.feedbackNLP = feedbackNLP;
	}

	public String getFeedbackNLP() {
		return feedbackNLP;
	}
	
	public String getSelectedDatabase() {
		return selectedDatabase;
	}

	public void setSelectedDatabase(String selectedDatabase) {
		this.selectedDatabase = selectedDatabase;
	}

	public void setDatabaseSchema(DatabaseSchema databaseSchema) {
		this.databaseSchema = databaseSchema;
	}

	public DatabaseSchema getDatabaseSchema() {
		return databaseSchema;
	}
}
