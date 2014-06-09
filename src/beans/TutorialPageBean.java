package beans;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import objects.DatabaseTable;
import objects.QueryResult;
import objects.Question;
import objects.QuestionTuple;
import utilities.JDBC_Abstract_Connection;
import utilities.JDBC_MySQL_Connection;
import utilities.JDBC_PostgreSQL_Connection;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.gatech.sqltutor.DatabaseManager;

@ManagedBean
@ViewScoped
public class TutorialPageBean {
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	
	@ManagedProperty(value="#{databaseManager}")
	private DatabaseManager databaseManager;
	
	private JDBC_Abstract_Connection connection;
	private String selectedSchema;
	private List<DatabaseTable> tables;
	private List<String> questions = new ArrayList<String>();
	private HashMap<String, String> answers = new HashMap<String, String>();
	private int questionIndex;
	private String query;
	private String feedbackNLP;
	private String resultSetFeedback;
	private String userFeedback;
	private QueryResult queryResult;
	private QueryResult answerResult;
	private QueryResult queryDiffResult;
	private QueryResult answerDiffResult;
	private final boolean nlpDisabled = true;

	@PostConstruct
	public void init() {
		connection = new JDBC_PostgreSQL_Connection();
		selectedSchema = userBean.getSelectedSchema();
		tables = connection.getTables(selectedSchema);
		setQuestionsAndAnswers();
	}

	public void processSQL() {
		try {
			queryResult = connection.getQueryResult(selectedSchema, query);
			if(!nlpDisabled)
				feedbackNLP = "We determined the question that you actually answered was: \n\"" + (new Question(query, tables)).getQuestion() + "\"";
			else 
				feedbackNLP = "";
			setResultSetDiffs();
		} catch(SQLException e) {
			resultSetFeedback = "Incorrect. Your query was malformed. Please try again.\n" + e.getMessage();
		}
		connection.log(getSessionId(), getIpAddress(), userBean.getUsername(), selectedSchema, 
				questions.get(questionIndex), getAnswers().get(questions.get(questionIndex)), query, !isQueryMalformed(), getQueryIsCorrect());
	} 
	
	public String getIpAddress() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
		    ipAddress = request.getRemoteAddr();
		}
		return ipAddress;
	}
	
	public String getSessionId() {
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		return session.getId();
	}
	
	public void setResultSetDiffs() {
		try {
			answerResult = connection.getQueryResult(selectedSchema, getAnswers().get(questions.get(questionIndex)));
			queryDiffResult = new QueryResult(queryResult);
			queryDiffResult.getColumns().removeAll(answerResult.getColumns());
			queryDiffResult.getData().removeAll(answerResult.getData());
			answerDiffResult = new QueryResult(answerResult);
			answerDiffResult.getColumns().removeAll(queryResult.getColumns());
			answerDiffResult.getData().removeAll(queryResult.getData());
			
			if (getAnswers().get(questions.get(questionIndex)).toLowerCase().contains(" order by ")) {
				compareQueries(false, true); 
			} else {
				compareQueries(false, false); 
			} 
			
		} catch(SQLException e) {
			resultSetFeedback = "The stored answer was malformed." + e.getMessage();
		}
	}
	
	public void compareQueries(boolean columnOrderMatters, boolean rowOrderMatters) {
		
		if(!queryResult.getColumns().containsAll(answerResult.getColumns()) || !answerResult.getColumns().containsAll(queryResult.getColumns())) {
			resultSetFeedback = "Incorrect.";
		} else {
			if(columnOrderMatters && rowOrderMatters) {
				if(answerResult.equals(queryResult)) {
					resultSetFeedback = "Correct!";
				} else {
					resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's.";
					//FIXME perhaps more specific feedback? different row data/order, order by? conditionals? different attributes?
				}
			} else if(columnOrderMatters && !rowOrderMatters) {
				String queryDiffAnswer = query + " EXCEPT " + getAnswers().get(questions.get(questionIndex)) + ";";
				String answerDiffQuery = getAnswers().get(questions.get(questionIndex)) + " EXCEPT " + query + ";";
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
					answerTree.put(answerResult.getColumns().get(i), columnData);
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
		String feedbackMessagesId = FacesContext.getCurrentInstance().getViewRoot().findComponent(":feedbackForm:feedbackMessages").getClientId();
		FacesContext.getCurrentInstance().addMessage(feedbackMessagesId, new FacesMessage("We appreciate your submission."));
	}
	
	public void setQuestionsAndAnswers() {
		List<QuestionTuple> questionTuples = null;
		try {
			questionTuples = getDatabaseManager().getQuestions(selectedSchema);
		} catch (SQLException e) {
			e.getNextException().printStackTrace();
		}
		
		if(questionTuples.isEmpty()) {
			questions.add("There are no questions available for this schema.");
		} else {
			HashMap<String, Boolean> options = null;
			try {
				options = getDatabaseManager().getOptions(selectedSchema);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			for(QuestionTuple question : questionTuples) {
				questions.add(question.getQuestion());
				getAnswers().put(question.getQuestion(), question.getAnswer());
			}
			
			if(!options.get("in_order_questions")) {
				Collections.shuffle(questions, new Random(System.nanoTime()));
			}
			
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
	
	public boolean isNlpDisabled() {
		return nlpDisabled;
	}
	
	public boolean isQueryMalformed() {
		if(resultSetFeedback.toLowerCase().contains("malformed")) {
			return true;
		}
		return false;
	}

	public HashMap<String, String> getAnswers() {
		return answers;
	}

	public void setAnswers(HashMap<String, String> answers) {
		this.answers = answers;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}
}
