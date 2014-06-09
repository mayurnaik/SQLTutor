package edu.gatech.sqltutor.beans;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.DualListModel;

import objects.DatabaseTable;
import objects.QueryResult;
import objects.QuestionTuple;
import utilities.JDBC_Abstract_Connection;
import utilities.JDBC_PostgreSQL_Connection;
import beans.UserBean;
import edu.gatech.sqltutor.DatabaseManager;

@ManagedBean
@ViewScoped
public class SchemaQuestionsPageBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	@ManagedProperty(value="#{databaseManager}")
	private DatabaseManager databaseManager;
	
	private JDBC_Abstract_Connection connection;
	
	private List<DatabaseTable> tables;
	
	private String selectedSchema;
	
	private String question;
	private String answer;
	
	private List<QuestionTuple> questions;
	private DualListModel<QuestionTuple> questionDualList;
	
	
	@PostConstruct
	public void init() {
		selectedSchema = userBean.getSelectedSchema();
		connection = new JDBC_PostgreSQL_Connection();
		tables = connection.getTables(selectedSchema);
		
		setupQuestionList();
	}
	
	public void setupQuestionList() {
		try {
			questions = databaseManager.getQuestions(selectedSchema);
			questionDualList = new DualListModel<QuestionTuple>(questions, new ArrayList<QuestionTuple>());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void reorderQuestions() {
		try {
			databaseManager.reorderQuestions(questionDualList.getSource());
			setupQuestionList();
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Successfully reordered the questions.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (SQLException e) {
			e.getNextException().printStackTrace();
		}
	}
	
	public void deleteQuestions() {
		if(questionDualList.getTarget().isEmpty()) {
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"You must select questions to be deleted.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return;
		}
		try {
			databaseManager.deleteQuestions(questionDualList.getTarget());
			setupQuestionList();
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Successfully deleted the questions.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addQuestion() {
		try {
			databaseManager.addQuestion(selectedSchema, getQuestion(), getAnswer());
			setupQuestionList();
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Successfully added this question.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<DatabaseTable> getTables() {
		return tables;
	}

	public void setTables(List<DatabaseTable> tables) {
		this.tables = tables;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(DatabaseManager dbManager) {
		this.databaseManager = dbManager;
	}
	
	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public List<QuestionTuple> getQuestions() {
		return questions;
	}

	public void setQuestions(List<QuestionTuple> questions) {
		this.questions = questions;
	}

	public DualListModel<QuestionTuple> getQuestionDualList() {
		return questionDualList;
	}

	public void setQuestionDualList(DualListModel<QuestionTuple> questionDualList) {
		this.questionDualList = questionDualList;
	}
}
