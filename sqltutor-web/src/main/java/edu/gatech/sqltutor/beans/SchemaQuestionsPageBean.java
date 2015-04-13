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

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;

import edu.gatech.sqltutor.DatabaseTable;
import edu.gatech.sqltutor.tuples.QuestionTuple;

@ManagedBean
@ViewScoped
public class SchemaQuestionsPageBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	private static final String PERMISSIONS_ERROR = "You do not have permissions for this schema.";
	private static final String REORDER_CONFIRMATION_MESSAGE = "Successfully reordered the question(s).";
	private static final String CHOOSE_QUESTION_ERROR = "You must select questions to be deleted.";
	private static final String DELETE_CONFIRMATION_MESSAGE = "Successfully deleted the question(s).";
	private static final String ADD_CONFIRMATION_MESSAGE = "Successfully added this question.";
	
	private List<DatabaseTable> tables;
	
	private QuestionTuple question;
	
	private List<QuestionTuple> questions;
	private List<QuestionTuple> selectedQuestions;
	
	
	public void preRenderSetup(ComponentSystemEvent event) throws IOException {
		if (!userBean.isLoggedIn())
			return; //TODO: this is to avoid both preRenderEvents firing, not sure if there is a better way.
		
		if (userBean.getSelectedTutorial() == null || userBean.getSelectedTutorial().isEmpty()) {
			BeanUtils.addErrorMessage(null, "To modify a tutorial, you must first select one.", true);
			BeanUtils.redirect("/AdminPage.jsf");
			return;
		}
		
		try {
			tables = getDatabaseManager().getSchemaTables(userBean.getSelectedTutorial());
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
		
		question = new QuestionTuple();
		setupQuestionList();
	}
	
	public void setupQuestionList() {
		try {
			questions = getDatabaseManager().getQuestions(userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode());
			selectedQuestions = new LinkedList<QuestionTuple>();
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
	}

	public void reorderQuestions() {
		if(!hasPermissions())
			return;
		
		try {
			getDatabaseManager().reorderQuestions(questions);
			selectedQuestions = new LinkedList<QuestionTuple>();
			BeanUtils.addInfoMessage(null, REORDER_CONFIRMATION_MESSAGE);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
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
			questions.removeAll(selectedQuestions);
			reorderQuestions();
			BeanUtils.addInfoMessage(null, DELETE_CONFIRMATION_MESSAGE);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		} 
	}

	public void addQuestion() {
		if(!hasPermissions())
			return;
		
		try {
			getDatabaseManager().getQueryResult(userBean.getSelectedTutorial(), question.getAnswer(), true);
		} catch(SQLException e) {
			String message = e.getMessage();
			if(message.contains("getNextException"))
				message = e.getNextException().getMessage();
			BeanUtils.addErrorMessage(null, message);
			return;
		}
		
		try {
			questions.add(question);
			question.setOrder(questions.size());
			getDatabaseManager().addQuestion(userBean.getSelectedTutorialName(), question, userBean.getSelectedTutorialAdminCode());
			BeanUtils.addInfoMessage(null, ADD_CONFIRMATION_MESSAGE);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		} 
	}
	
	private boolean hasPermissions() {
		boolean hasPermissions = false;
		try {
			hasPermissions = getDatabaseManager().checkTutorialPermissions(userBean.getSelectedTutorialName(), userBean.getAdminCode());

			if(!hasPermissions) 
				BeanUtils.addErrorMessage(null, PERMISSIONS_ERROR);
		} catch(SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
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
	
	public QuestionTuple getQuestion() {
		return question;
	}
}
