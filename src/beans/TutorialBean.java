package beans;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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

@ManagedBean
@ViewScoped
public class TutorialBean {
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	private JDBC_Abstract_Connection connection;
	private String selectedSchema;
	private List<DatabaseTable> tables;
	private List<String> questions = new ArrayList<String>();
	private List<String> answers = new ArrayList<String>();
	private int questionIndex;
	private String query;
	private String feedbackNLP;
	private String resultSetFeedback;
	private String userFeedback;
	private QueryResult queryResult;
	private QueryResult answerResult;
	private QueryResult queryDiffResult;
	private QueryResult answerDiffResult;

	@PostConstruct
	public void init() {
		String[] schemaAttributes;
		if(getUserBean().getSelectedSchema() != null) {
			schemaAttributes = getUserBean().getSelectedSchema().split(" ");
		} else {
			//default to company
			schemaAttributes = new String[2];
			schemaAttributes[0] = "PostgreSQL";
			schemaAttributes[1] = "company";
		}
		final String databaseConnector = schemaAttributes[0];
		if(databaseConnector.equalsIgnoreCase("PostgreSQL")) {	
			connection = new JDBC_PostgreSQL_Connection();
		} else if (databaseConnector.equalsIgnoreCase("MySQL")) {
			connection = new JDBC_MySQL_Connection();
		} else {
			return; //eventually redirect to message about connector not being supported
		}
		selectedSchema = schemaAttributes[1];
		tables = connection.getTables(selectedSchema);
		setQuestionsAndAnswers();
	}

	public void processSQL() {
		try {
			queryResult = connection.getQueryResult(selectedSchema, query);
			feedbackNLP = "We determined the question that you actually answered was: \n\"" + (new Question(query, tables)).getQuestion() + "\"";
			setResultSetDiffs();
			
		} catch(SQLException e) {
			feedbackNLP = "Your query was malformed. Please try again.\n" + e.getMessage();
			resultSetFeedback = "Incorrect.";
		}
	} 
	
	public void setResultSetDiffs() {
		QueryResult[] resultSetDiffs = null;
		if (answers.get(questionIndex).toLowerCase().contains(" order by ")) {
			resultSetDiffs = compareQueries(true, true); //false true when it is ready
		} else {
			resultSetDiffs = compareQueries(true, false); //false, false when it is ready
		} 
		//queryDiffResult = resultSetDiffs[0]; 
		//answerDiffResult = resultSetDiffs[1];
	}
	
	public QueryResult[] compareQueries(boolean columnOrderMatters, boolean rowOrderMatters) {
		QueryResult[] resultSetDiffs = null;
		
		try {
			answerResult = connection.getQueryResult(selectedSchema, answers.get(questionIndex));
			
			if(columnOrderMatters && rowOrderMatters) {
				if(answerResult.getColumns().size() == queryResult.getColumns().size()) {
					if(answerResult.equals(queryResult)) {
						resultSetFeedback = "Correct!";
					} else {
						resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's.";
						//FIXME perhaps more specific feedback? different row data/order, order by? conditionals? different attributes?
					}
				} else {
					resultSetFeedback = "Incorrect. Your query's number of columns did not match the stored answer's. Check your attributes.";
				}
				//FIXME Can now tell if they are equal or not... but how to return their differences?
			} else if(columnOrderMatters && !rowOrderMatters) {
				//    (Q1 EXCEPT Q2) UNION ALL (Q2 EXCEPT Q1)
				String queryDiffAnswer = query + " EXCEPT " + answers.get(questionIndex) + ";";
				String answerDiffQuery = answers.get(questionIndex) + " EXCEPT " + query + ";";
				queryDiffResult = connection.getQueryResult(selectedSchema, queryDiffAnswer);
				answerDiffResult = connection.getQueryResult(selectedSchema, answerDiffQuery);
				if(queryDiffResult.getData().isEmpty() && answerDiffResult.getData().isEmpty()) {
					resultSetFeedback = "Correct.";
				} else {
					//column size and type difference is handled by the exception
					resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's.";
					// FIXME find queryDiffResult in queryResult and mark green. append answerDiffResult to the bottom in red.
				}
			} else if(!columnOrderMatters && rowOrderMatters) {
				//FIXME For this we use a map structure that automatically sorts its keys to hold the entity names and a list to hold the data, thus: 
				//new TreeMap<String, List<String>>(); 
				//q1MapOfLists.equals(q2MapOfLists)
			} else {
				//FIXME Put the data of the result sets into two separate multisets/bags (Google's Guava can be used to get this structure in Java). This structure will count the appearance of the data, making things simple:
				//q1Bag.equals(q2Bag)
				// For my implementation I needed to see their differences. To do so, for each entry of the 
				//second bag you will check if the first bag contains it. If so, remove one count of that entry 
				//from both bags. In the end, the first bag will contain all entries which the second result set 
				//did not contain, and the second bag will contain all entries which the first did not contain. 
			}
		} catch(SQLException e) {
			if(e.getMessage().contains("columns")) {
				resultSetFeedback = "Incorrect. The number of columns in your result did not match the answer.";
			} else if(e.getMessage().contains("type")) {
				resultSetFeedback = "Incorrect. One or more of your result's data types did not match the answer.";
			} else {
				resultSetFeedback = "The stored answer was malformed." + e.getMessage();
			}
		}
		return resultSetDiffs;
	}
	
	public void submitFeedback() {
		// FIXME We'll need to decide how we're going to store this.
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("We appreciate your submission."));
	}
	
	public void setQuestionsAndAnswers() {
		// currently hard coded answers (which get converted to questions). This will be phased out.
		if (selectedSchema.equalsIgnoreCase("company")) {
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
		} else if (selectedSchema.equalsIgnoreCase("sales")) {
			answers.clear();
			questions.clear();
			answers.add("SELECT NAME, REP_OFFICE FROM salesreps");
			Question question;
			for(int i = 0; i < answers.size(); i++ ) {
				question = new Question(answers.get(i), tables);
				questions.add(question.getQuestion());
			}
		} else {	// just a place holder for every other schema.
			answers.clear();
			questions.clear();
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
	
	public String getSelectedSchema() {
		return selectedSchema;
	}

	public List<DatabaseTable> getTables() {
		return tables;
	}

	public QueryResult getQueryDiffAnswer() {
		return queryDiffResult;
	}
	
	public QueryResult getAnswerResult() {
		return answerResult;
	}

	public QueryResult getAnswerDiffQuery() {
		return answerDiffResult;
	}

	public String getResultSetFeedback() {
		return resultSetFeedback;
	}
	
	public boolean getQueryIsCorrect() {
		if(resultSetFeedback == null || !resultSetFeedback.toLowerCase().contains("incorrect")) {
			return true;
		}
		return false;
	}

	public void setUserFeedback(String userFeedback) {
		this.userFeedback = userFeedback;
	}

	public String getUserFeedback() {
		return userFeedback;
	}
}
