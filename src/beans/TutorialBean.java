package beans;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import objects.DatabaseTable;
import objects.QueryResult;
import objects.Question;
import utilities.JDBC_Abstract_Connection;
import utilities.JDBC_MySQL_Connection;
import utilities.JDBC_PostgreSQL_Connection;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

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
		try {
			answerResult = connection.getQueryResult(selectedSchema, answers.get(questionIndex));
			queryDiffResult = new QueryResult(queryResult);
			queryDiffResult.getColumns().removeAll(answerResult.getColumns());
			queryDiffResult.getData().removeAll(answerResult.getData());
			answerDiffResult = new QueryResult(answerResult);
			answerDiffResult.getColumns().removeAll(queryResult.getColumns());
			answerDiffResult.getData().removeAll(queryResult.getData());
			
			if (answers.get(questionIndex).toLowerCase().contains(" order by ")) {
				compareQueries(false, true); //false true when it is ready
			} else {
				compareQueries(false, false); //false, false when it is ready
			} 
			
		} catch(SQLException e) {
			resultSetFeedback = "The stored answer was malformed." + e.getMessage();
		}
	}
	
	public void compareQueries(boolean columnOrderMatters, boolean rowOrderMatters) {
		
		if(!queryResult.getColumns().containsAll(answerResult.getColumns())) {
			resultSetFeedback = "Incorrect. Your query's columns did not match the stored answer's. Check your attributes.";
		} else {
			if(columnOrderMatters && rowOrderMatters) {
				if(answerResult.equals(queryResult)) {
					resultSetFeedback = "Correct!";
				} else {
					resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's.";
					//FIXME perhaps more specific feedback? different row data/order, order by? conditionals? different attributes?
				}
			} else if(columnOrderMatters && !rowOrderMatters) {
				String queryDiffAnswer = query + " EXCEPT " + answers.get(questionIndex) + ";";
				String answerDiffQuery = answers.get(questionIndex) + " EXCEPT " + query + ";";
				try {
					queryDiffResult = connection.getQueryResult(selectedSchema, queryDiffAnswer);
					answerDiffResult = connection.getQueryResult(selectedSchema, answerDiffQuery);
					if(queryDiffResult.getData().isEmpty() && answerDiffResult.getData().isEmpty()) {
						resultSetFeedback = "Correct.";
					} else {
						resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's.";			
					}
				} catch(SQLException e) {
					if(e.getMessage().contains("columns")) {
						resultSetFeedback = "Incorrect. The number of columns in your result did not match the answer.";
					} else if(e.getMessage().contains("type")) {
						resultSetFeedback = "Incorrect. One or more of your result's data types did not match the answer.";
					} 
				}
			} else if(!columnOrderMatters && rowOrderMatters) {
				Map<String, List<String>> queryTree = new TreeMap<String, List<String>>();
				Map<String, List<String>> answerTree = new TreeMap<String, List<String>>();
				for(int i = 0; i < queryResult.getColumns().size(); i++) {
					List<String> columnData = new ArrayList<String>();
					for(int j = 0; j < queryResult.getData().size(); j++) {
						columnData.add(queryResult.getData().get(j).get(i));
					}
					queryTree.put(queryResult.getColumns().get(i), columnData);
				}
				for(int i = 0; i < answerResult.getColumns().size(); i++) {
					List<String> columnData = new ArrayList<String>();
					for(int j = 0; j < answerResult.getData().size(); j++) {
						columnData.add(answerResult.getData().get(j).get(i));
					}
					queryTree.put(answerResult.getColumns().get(i), columnData);
				}
				if(queryTree.equals(answerTree)) {
					resultSetFeedback = "Correct.";
				} else {
					resultSetFeedback = "Incorrect. Your query's data or order differed from the stored answer's.";
				}
			} else {
				Multiset<String> queryBag = HashMultiset.create();
				Multiset<String> answerBag = HashMultiset.create();
				for(int i = 0; i < queryResult.getColumns().size(); i++) {
					for(int j = 0; j < queryResult.getData().size(); j++) {
						queryBag.add(queryResult.getData().get(j).get(i));
					}
				}
				for(int i = 0; i < answerResult.getColumns().size(); i++) {
					for(int j = 0; j < answerResult.getData().size(); j++) {
						answerBag.add(answerResult.getData().get(j).get(i));
					}
				}
				if(queryBag.equals(answerBag)) {
					resultSetFeedback = "Correct.";
				} else {
					resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's.";
				} 
			}
		}
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

	public QueryResult getAnswerResult() {
		return answerResult;
	}

	public QueryResult getQueryDiffResult() {
		return queryDiffResult;
	}
	
	public QueryResult getAnswerDiffResult() {
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
