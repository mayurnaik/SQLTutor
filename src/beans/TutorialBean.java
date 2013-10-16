package beans;

import javax.annotation.PostConstruct;
import javax.faces.bean.*;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.ArrayList;

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
	private ArrayList<DatabaseTable> tables;
	private ArrayList<String> questions = new ArrayList<String>();
	private ArrayList<String> answers = new ArrayList<String>();
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
		queryResult = connection.getQueryResult(selectedDatabase, query);
		if(queryResult.isMalformed()) {
			feedbackNLP = "Your query was malformed. Please try again. Exception: \n" + queryResult.getExceptionMessage();
			resultSetFeedback = "Incorrect.";
		} else {
			feedbackNLP = "The question you answered was: \n" + (new Question(query, tables)).getQuestion();
			if (answers.get(questionIndex).toLowerCase().contains(" order by ")) {
				answerResult = connection.getQueryResult(selectedDatabase, answers.get(questionIndex));
				if(queryResult.getData().equals(answerResult.getData())) {
					resultSetFeedback = "Correct.";
				} else {
					resultSetFeedback = "Incorrect. The result's data differed.";
					// find a way to mark where the queryResult did not equal the answerResult
				}
			} else {
				// Columns must be ordered correctly by the user.
				String queryDiffAnswer = query + " EXCEPT " + answers.get(questionIndex) + ";";
				String answerDiffQuery = answers.get(questionIndex) + " EXCEPT " + query + ";";
				// The result set of all ADDITIONAL data gathered by the user's query.
				queryDiffResult = connection.getQueryResult(selectedDatabase, queryDiffAnswer);
				if(queryDiffResult.isMalformed()) {
					resultSetFeedback = "Incorrect. Either the number of columns did not match, or their types differed.";
					return;
				}
				// The result set of all MISSED data, not gathered by the user's query.
				answerDiffResult = connection.getQueryResult(selectedDatabase, answerDiffQuery);
				if(queryDiffResult.getData().isEmpty() && answerDiffResult.getData().isEmpty()) {
					resultSetFeedback = "Correct.";
				} else {
					resultSetFeedback = "Incorrect. The result's data differed.";
					// find queryDiffResult in queryResult and mark green
					// append answerDiffResult to the bottom in red
				}
			}
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

	public ArrayList<String> getAnswers() {
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

	public ArrayList<DatabaseTable> getTables() {
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
