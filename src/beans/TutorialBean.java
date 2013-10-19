package beans;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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

@ManagedBean
@ViewScoped
public class TutorialBean {
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	private JDBC_Abstract_Connection connection;
	private String selectedDatabase;
	private List<DatabaseTable> tables;
	private List<String> questions = new ArrayList<String>();
	private List<String> answers = new ArrayList<String>();
	private int questionIndex;
	private String query;
	private String feedbackNLP;
	private String resultSetFeedback;
	private QueryResult queryResult;
	private QueryResult answerResult;
	private QueryResult queryDiffResult;
	private QueryResult answerDiffResult;


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
		setQuestionsAndAnswers();
	}
	
	public void loginRedirect() throws IOException {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
	    if (!userBean.isLoggedIn()) {
	        externalContext.redirect(externalContext.getRequestContextPath() + "/HomePage.jsf");
	    } 
	}
	
	public void processSQL() {
		try {
			queryResult = connection.getQueryResult(selectedDatabase, query);
			feedbackNLP = "The question you answered was: \n" + (new Question(query, tables)).getQuestion();
			if (answers.get(questionIndex).toLowerCase().contains(" order by ")) {
				queryEquivalenceCheck();
			} else {
				queryDifferenceCheck();
			} 
		} catch(Exception e) {
			feedbackNLP = "Your query was malformed. Please try again.\n" + e.getMessage();
			resultSetFeedback = "Incorrect.";
		}
	} 
	
	public void queryEquivalenceCheck() {
		try {
			answerResult = connection.getQueryResult(selectedDatabase, answers.get(questionIndex));
			if(queryResult.getData().equals(answerResult.getData())) {
				resultSetFeedback = "Correct.";
			} else {
				resultSetFeedback = "Incorrect. The result's data differed.";
				// FIXME find a way to mark where the queryResult did not equal the answerResult
			}
		} catch(SQLException e) {
			resultSetFeedback = "The stored answer was malformed.";
		}
	}
	
	public void queryDifferenceCheck() {
		// Columns must be ordered correctly by the user.
		String queryDiffAnswer = query + " EXCEPT " + answers.get(questionIndex) + ";";
		String answerDiffQuery = answers.get(questionIndex) + " EXCEPT " + query + ";";
		try {
			// The result set of all ADDITIONAL data gathered by the user's query.
			queryDiffResult = connection.getQueryResult(selectedDatabase, queryDiffAnswer);
			answerDiffResult = connection.getQueryResult(selectedDatabase, answerDiffQuery);
		} catch(Exception e) {
			System.out.println(e.getMessage());
			if(e.getMessage().contains("columns")) {
				resultSetFeedback = "Incorrect. The number of columns of your result did not match the answer.";
			} else if(e.getMessage().contains("type")){
				resultSetFeedback = "Incorrect. One or more of your result's data types did not match the answer.";
			}
			return;
		}
		// The result set of all MISSED data, not gathered by the user's query.
		if(queryDiffResult.getData().isEmpty() && answerDiffResult.getData().isEmpty()) {
			resultSetFeedback = "Correct.";
		} else {
			resultSetFeedback = "Incorrect. The result's data differed.";
			// FIXME find queryDiffResult in queryResult and mark green
			// append answerDiffResult to the bottom in red
		}
	}
	
	public void setQuestionsAndAnswers() {
		// currently hard coded answers (which get converted to questions). This will be phased out.
		if (selectedDatabase.equalsIgnoreCase("company")) {
			answers.clear();
			questions.clear();
			answers.add("SELECT id, first_name FROM employee, department");
			answers.add("SELECT first_name, last_name FROM employee");
			answers.add("SELECT salary FROM employee WHERE first_name = 'Ahmad'");
			answers.add("SELECT first_name FROM employee ORDER BY first_name DESC");
			Question question;
			for(int i = 0; i < answers.size(); i++ ) {
				question = new Question(answers.get(i), tables);
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
		queryResult = null;
		answerResult = null;
		queryDiffResult = null;
		answerDiffResult = null;
		feedbackNLP = "";
		resultSetFeedback = "";
	}
	
	public String getQuestion() {
		return questions.get(questionIndex);
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

	public List<String> getAnswers() {
		return answers;
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

	public QueryResult getQueryDiffAnswer() {
		return queryDiffResult;
	}

	public QueryResult getAnswerDiffQuery() {
		return answerDiffResult;
	}

	public String getResultSetFeedback() {
		return resultSetFeedback;
	}
}
