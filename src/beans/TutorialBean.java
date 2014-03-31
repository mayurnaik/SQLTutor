package beans;

import java.sql.SQLException;
import java.util.ArrayList;
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
	private final boolean nlpDisabled = true;

	@PostConstruct
	public void init() {
		String[] schemaAttributes;
		//if(getUserBean().getSelectedSchema() != null) {
		//	schemaAttributes = getUserBean().getSelectedSchema().split(" ");
		//} else {
			//default to company
			schemaAttributes = new String[2];
			schemaAttributes[0] = "PostgreSQL";
			schemaAttributes[1] = "company";
		//}
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
			if(!nlpDisabled)
				feedbackNLP = "We determined the question that you actually answered was: \n\"" + (new Question(query, tables)).getQuestion() + "\"";
			else 
				feedbackNLP = "";
			setResultSetDiffs();
		} catch(SQLException e) {
			//feedbackNLP = "Your query was malformed. Please try again.\n" + e.getMessage();
			//resultSetFeedback = "Incorrect";
			resultSetFeedback = "Incorrect." + "Your query was malformed. Please try again.\n" + e.getMessage();
		}
		connection.log(getSessionId(), getIpAddress(), userBean.getUsername(), selectedSchema, 
				questions.get(questionIndex), answers.get(questionIndex), query, isQueryMalformed(), getQueryIsCorrect());
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
			answerResult = connection.getQueryResult(selectedSchema, answers.get(questionIndex));
			queryDiffResult = new QueryResult(queryResult);
			queryDiffResult.getColumns().removeAll(answerResult.getColumns());
			queryDiffResult.getData().removeAll(answerResult.getData());
			answerDiffResult = new QueryResult(answerResult);
			answerDiffResult.getColumns().removeAll(queryResult.getColumns());
			answerDiffResult.getData().removeAll(queryResult.getData());
			
			if (answers.get(questionIndex).toLowerCase().contains(" order by ")) {
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
			resultSetFeedback = "Incorrect. Check your attributes.";
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
		// currently hard coded answers (which get converted to questions). This will be phased out.
		if (selectedSchema.equalsIgnoreCase("company")) {
			answers.clear();
			questions.clear();

			questions.add("Retrieve the salary of the employee(s) named 'Ahmad'.");
			answers.add("SELECT salary FROM employee WHERE first_name = 'Ahmad'");
			
			questions.add("Select all employee SSNs in the database.");
			answers.add("SELECT ssn FROM employee");
			
			questions.add("Retrieve all distinct salary values.");
			answers.add("SELECT DISTINCT salary FROM employee");
			
			questions.add("Retrieve the name of each employee.");
			answers.add("SELECT first_name, last_name FROM employee");
			
			questions.add("Retrieve the birth date and address of the employee(s) whose name is ‘John B. Smith’.");
			answers.add("SELECT birthdate, address FROM employee WHERE first_name = 'John' AND middle_initial = 'B' AND last_name = 'Smith'");

			questions.add("Retrieve the name and address of all employees who work for the ‘Research’ department.");
			answers.add("SELECT first_name, last_name, address FROM employee, department WHERE name = 'Research' AND id = department_id");
			
			questions.add("For each employee, retrieve the employee’s first and last name and the first and last name of his or her immediate supervisor.");
			answers.add("SELECT E.first_name, E.last_name, S.first_name, S.last_name FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn;");
			
			questions.add("Retrieve all employees whose address is in Houston, Texas.");
			answers.add("SELECT first_name, last_name FROM employee WHERE address LIKE '%Houston, TX%'");
			
			questions.add("Retrieve all employees in department 5 whose salary is between $30,000 and $40,000.");
			answers.add("SELECT * FROM employee WHERE (salary BETWEEN 30000 AND 40000) AND department_id = 5");
			
			questions.add("Retrieve the names of all employees who do not have supervisors.");
			answers.add("SELECT first_name, last_name FROM employee WHERE manager_ssn IS NULL;");
			
			questions.add("Retrieve the last name of each employee and his or her supervisor.");
			answers.add("SELECT E.last_name AS employee_name, S.last_name AS supervisor_name FROM employee AS E, employee AS S WHERE E.manager_SSN = S.SSN");
			
			questions.add("For every project located in ‘Stafford’, list the project number, the controlling department number, and the department manager’s last name, address, and birthdate.");
			answers.add("SELECT project.id, department.id, last_name, address, birthdate FROM project, department, employee WHERE project.department_id = department.id AND department.manager_ssn = ssn AND location = 'Stafford'");
			
			questions.add("Retrieve the employees whose salary is greater than the salary of the manager of the department that the employee works for.");
			answers.add("SELECT e.first_name, e.last_name FROM employee E, employee M, department D WHERE E.salary > M.salary AND E.department_id = D.id AND D.manager_ssn = M.ssn");
			
			questions.add("Retrieve the last name and first name of all employees who work on a project.");
			answers.add("SELECT e.first_name, e.last_name FROM employee e, project p, works_on WHERE e.ssn=employee_ssn AND id = project_id");
			
			questions.add("Find the names of all employees who are directly supervised by ‘Franklin Wong’.");
			answers.add("SELECT e.first_name, e.last_name FROM employee e, employee s WHERE s.first_name = 'Franklin' AND s.last_name = 'Wong' AND e.manager_ssn = s.ssn");

			/*
			Question question;
			for(int i = 0; i < answers.size(); i++ ) {
				question = new Question(answers.get(i), tables);
				questions.add(question.getQuestion());
			}*/
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
	
	public boolean isNlpDisabled() {
		return nlpDisabled;
	}
	
	public boolean isQueryMalformed() {
		if(feedbackNLP.contains("malformed")) {
			return true;
		}
		return false;
	}
}
