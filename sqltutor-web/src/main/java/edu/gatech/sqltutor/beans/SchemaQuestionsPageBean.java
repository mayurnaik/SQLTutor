/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package edu.gatech.sqltutor.beans;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import edu.gatech.sqltutor.DatabaseTable;
import edu.gatech.sqltutor.QuestionTuple;

@ManagedBean
@ViewScoped
public class SchemaQuestionsPageBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	private static final String PERMISSIONS_ERROR = "You do not have permissions for this schema.";
	private static final String REORDER_CONFIRMATION_MESSAGE = "Successfully reordered the questions.";
	private static final String CHOOSE_QUESTION_ERROR = "You must select questions to be deleted.";
	private static final String DELETE_CONFIRMATION_MESSAGE = "Successfully deleted the questions.";
	private static final String ADD_CONFIRMATION_MESSAGE = "Successfully added this question.";
	
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
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR);
		}
		
		setupQuestionList();
	}
	
	public void setupQuestionList() {
		try {
			questions = getDatabaseManager().getQuestions(selectedSchema);
			selectedQuestions = new LinkedList<QuestionTuple>();
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR);
		}
	}

	public void reorderQuestions() {
		if(!hasPermissions())
			return;
		
		try {
			getDatabaseManager().reorderQuestions(questions);
			BeanUtils.addInfoMessage(null, REORDER_CONFIRMATION_MESSAGE);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR);
		} 
	}
	
	public void deleteQuestions() {
		if(!hasPermissions())
			return;
		
		try {
			if(selectedQuestions.isEmpty()) {
				BeanUtils.addErrorMessage(null, CHOOSE_QUESTION_ERROR);
				return;
			}
			getDatabaseManager().deleteQuestions(selectedQuestions);
			setupQuestionList();
			BeanUtils.addErrorMessage(null, DELETE_CONFIRMATION_MESSAGE);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR);
		} 
	}

	public void addQuestion() {
		if(!hasPermissions())
			return;
		
		try {
			getDatabaseManager().verifyQuery(selectedSchema, getAnswer());
		} catch(SQLException e) {
			for(Throwable t : e) {
				BeanUtils.addErrorMessage(null, t.getMessage());
			}
			return;
		}
		
		try {
			getDatabaseManager().addQuestion(selectedSchema, getQuestion(), getAnswer());
			setupQuestionList();
			BeanUtils.addInfoMessage(null, ADD_CONFIRMATION_MESSAGE);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR);
		} 
	}
	
	private boolean hasPermissions() {
		boolean hasPermissions = false;
		try {
			hasPermissions = getDatabaseManager().checkSchemaPermissions(userBean.getHashedEmail(), userBean.getSelectedSchema());

			if(!hasPermissions) 
				BeanUtils.addErrorMessage(null, PERMISSIONS_ERROR);
		} catch(SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR);
		}
		return hasPermissions;
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
