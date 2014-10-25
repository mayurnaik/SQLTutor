package edu.gatech.sqltutor.beans;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import edu.gatech.sqltutor.DatabaseTable;
import edu.gatech.sqltutor.QuestionTuple;

@ManagedBean
@ViewScoped
public class SchemaQuestionsPageBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	private List<DatabaseTable> tables;
	
	private String selectedSchema;
	
	private String question;
	private String answer;
	
	private List<QuestionTuple> questions;
	private List<QuestionTuple> selectedQuestions;
	
	
	@PostConstruct
	public void init() {
		selectedSchema = userBean.getSelectedSchema();
		try {
			tables = getDatabaseManager().getTables(selectedSchema);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getEmail());
			}
		}
		
		setupQuestionList();
	}
	
	public void setupQuestionList() {
		try {
			questions = getDatabaseManager().getQuestions(selectedSchema);
			selectedQuestions = new ArrayList<QuestionTuple>();
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getEmail());
			}
		}
	}

	public void reorderQuestions() {
		try {
			boolean hasPermissions = getDatabaseManager().checkSchemaPermissions(userBean.getHashedEmail(), userBean.getSelectedSchema());
			if(!hasPermissions) {
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"You do not have permissions for this schema.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			getDatabaseManager().reorderQuestions(questions);
			
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Successfully reordered the questions.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getEmail());
			}
		} 
	}
	
	public void deleteQuestions() {
		try {
			boolean hasPermissions = getDatabaseManager().checkSchemaPermissions(userBean.getHashedEmail(), userBean.getSelectedSchema());
			if(!hasPermissions) {
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"You do not have permissions for this schema.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			if(selectedQuestions.isEmpty()) {
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"You must select questions to be deleted.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			getDatabaseManager().deleteQuestions(selectedQuestions);
			setupQuestionList();
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Successfully deleted the questions.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getEmail());
			}
		} 
	}

	public void addQuestion() {
		try {
			boolean hasPermissions = getDatabaseManager().checkSchemaPermissions(userBean.getHashedEmail(), userBean.getSelectedSchema());
			if(!hasPermissions) {
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"You do not have permissions for this schema.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			getDatabaseManager().addQuestion(selectedSchema, getQuestion(), getAnswer());
			setupQuestionList();
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Successfully added this question.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getEmail());
			}
		} 
	}

	public List<DatabaseTable> getTables() {
		return tables;
	}

	public void setTables(List<DatabaseTable> tables) {
		this.tables = tables;
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

	public List<QuestionTuple> getSelectedQuestions() {
		return selectedQuestions;
	}

	public void setSelectedQuestions(List<QuestionTuple> selectedQuestions) {
		this.selectedQuestions = selectedQuestions;
	}
}
