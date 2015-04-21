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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;

import edu.gatech.sqltutor.tuples.QuestionCommentTuple;
import edu.gatech.sqltutor.tuples.QuestionHardnessTuple;
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
	
	private boolean hasPermissions;
	
	// add a question
	private QuestionTuple question;
	
	// question list
	private List<QuestionTuple> questions;
	private List<QuestionTuple> selectedQuestions;
	
	// question comments
	private List<QuestionCommentTuple> comments;
	private List<QuestionCommentTuple> selectedComments;
	private String reply;
	
	// question statistics
	private List<QuestionHardnessTuple> hardnessTuples;
	private HashSet<String> leastUnderstoodConcepts;
	private HashSet<String> mostUnderstoodConcepts;
	private int maximumIncorrectCount;
	private int minimumAttemptCount;
	private int numberOfUsersAboveThreshold;
	private boolean useMedian;
	private boolean parsed;
	private int numberOfQuestionsForConcepts;
	
	public void preRenderSetup(ComponentSystemEvent event) throws IOException {
		if (!userBean.isLoggedIn())
			return; //TODO: this is to avoid both preRenderEvents firing, not sure if there is a better way.
		
		if (userBean.getSelectedTutorial() == null || userBean.getSelectedTutorial().isEmpty()) {
			BeanUtils.addErrorMessage(null, "To modify a tutorial, you must first select one.", true);
			BeanUtils.redirect("/AdminPage.jsf");
			return;
		}
		
		if(question == null) {
			question = new QuestionTuple();
			
			try {
				hasPermissions = getDatabaseManager().checkTutorialPermissions(userBean.getSelectedTutorialName(), userBean.getAdminCode());
			} catch(SQLException e) {
				for(Throwable t : e) {
					t.printStackTrace();
					logException(t, userBean.getHashedEmail());
				}
				BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
			}

			setupQuestionList();
			
			setupComments();
			
			// set default values for statistics
			// TODO: Magic numbers, a bit arbitrary
			minimumAttemptCount = questions != null ? questions.size() : 0; // perhaps should default to users who have made an attempt on each question
			maximumIncorrectCount = 10; // not sure here
			numberOfQuestionsForConcepts = questions != null ? (int) (Math.log(questions.size())/Math.log(2)) : 0; // this works pretty well to grab the upper end despite the number of questions
			calculateStatistics();
		}
	}
	
	public void setupQuestionList() {
		// setup the list of questions
		try {
			questions = getDatabaseManager().getQuestions(userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode());
			selectedQuestions = new LinkedList<QuestionTuple>();
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, "There was an internal database error trying to retrieve the list of questions. Please try again momentarily.");
		}
	}
	
	public void setupComments() {
		// setup the list of comments
		try {
			comments = getDatabaseManager().getComments(userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode());
			selectedComments = new LinkedList<QuestionCommentTuple>();
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, "There was an internal database error trying to retrieve the list of comments. Please try again momentarily.");
		}
	}
	
	public void recalculateStatistics() {
		calculateStatistics();
		BeanUtils.addInfoMessage(null, "Statistics have been recalculated.");
	}
	
	public void calculateStatistics() {
		// get the number of users who have made at least a number of attempts greater than the set threshold
//		try {
//			setNumberOfUsersAboveThreshold(getDatabaseManager().getNumberOfUsersAboveThreshold(userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode(), minimumAttemptCount));
//		} catch(SQLException e) {
//			for(Throwable t : e) {
//				t.printStackTrace();
//				logException(t, userBean.getHashedEmail());
//			}
//			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
//		}
		// use the formula to get the easiest and hardest questions:  
		// PER QUESTION: (SUM FOR EACH STUDENT(INCORRECT ATTEMPTS) / (MEDIAN OR AVG INCORRECT ATTEMPTS PER QUESTION ACROSS ALL QUESTIONS)
		try {
			setHardnessTuples(getDatabaseManager().getHardnessRatings(userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode(), maximumIncorrectCount, useMedian, parsed));
		} catch(SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
		// HARDEST CONCEPTS: { SET OF CONCEPTS IN HARDEST } - { SET OF CONCEPTS IN EASIEST }
		// EASIEST CONCEPTS: { SET OF CONCEPTS IN EASIEST } - { SET OF CONCEPTS IN HARDEST }
		// IF SET IS EMPTY, UNABLE TO DETERMINE
		HashSet<String> hardestConcepts = new HashSet<String>();
		HashSet<String> easiestConcepts = new HashSet<String>();
		
		if (hardnessTuples != null && hardnessTuples.size() > 0) {
			for(int i = 0; i < numberOfQuestionsForConcepts; i++) {
				if (questions.get(hardnessTuples.get(i).getOrder() - 1).getConcepts() != null)
					hardestConcepts.addAll(Arrays.asList(questions.get(hardnessTuples.get(i).getOrder()).getConcepts()));
			}
			
			for(int i = hardnessTuples.size() - 1; i > hardnessTuples.size() - 1 - numberOfQuestionsForConcepts; i--) {
				if (questions.get(hardnessTuples.get(i).getOrder() - 1).getConcepts() != null)
					easiestConcepts.addAll(Arrays.asList(questions.get(hardnessTuples.get(i).getOrder()).getConcepts()));
			}
		}
		
		leastUnderstoodConcepts = hardestConcepts;
		leastUnderstoodConcepts.removeAll(easiestConcepts);
		
		mostUnderstoodConcepts = easiestConcepts;
		mostUnderstoodConcepts.removeAll(hardestConcepts);
	}
	
	public void deleteComments() {
		if(!hasPermissions) {
			BeanUtils.addErrorMessage(null, PERMISSIONS_ERROR);
			return;
		}
		
		try {
			if(selectedComments.isEmpty()) {
				BeanUtils.addErrorMessage(null, "You must select comments to be deleted.");
				return;
			}
			getDatabaseManager().deleteComments(selectedComments);
			comments.removeAll(selectedComments);
			BeanUtils.addInfoMessage(null, "Successfully deleted the comment(s).");
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		} 
	}
	
	public void replyToComment() {
		// send replies to hashed emails (need to get plain text)
	}

	public void reorderQuestions() {
		orderQuestions();
		BeanUtils.addInfoMessage(null, REORDER_CONFIRMATION_MESSAGE);
	}
	
	public void orderQuestions() {
		if(!hasPermissions) {
			BeanUtils.addErrorMessage(null, PERMISSIONS_ERROR);
			return;
		}
		
		try {
			getDatabaseManager().reorderQuestions(questions);
			calculateStatistics();
			setupComments();
			selectedQuestions = new LinkedList<QuestionTuple>();
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		} 
	}
	
	public void deleteQuestions() {
		if(!hasPermissions) {
			BeanUtils.addErrorMessage(null, PERMISSIONS_ERROR);
			return;
		}
		
		try {
			if(selectedQuestions.isEmpty()) {
				BeanUtils.addErrorMessage(null, CHOOSE_QUESTION_ERROR);
				return;
			}
			getDatabaseManager().deleteQuestions(userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode(), selectedQuestions);
			questions.removeAll(selectedQuestions);
			orderQuestions();
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
		if(!hasPermissions) {
			BeanUtils.addErrorMessage(null, PERMISSIONS_ERROR);
			return;
		}
		
		// test whether the answer throws exceptions
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
			if (questions == null)
				questions = new LinkedList<QuestionTuple>();
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

	public List<QuestionCommentTuple> getComments() {
		return comments;
	}

	public void setComments(List<QuestionCommentTuple> comments) {
		this.comments = comments;
	}

	public List<QuestionCommentTuple> getSelectedComments() {
		return selectedComments;
	}

	public void setSelectedComments(List<QuestionCommentTuple> selectedComments) {
		this.selectedComments = selectedComments;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}
	
	public List<QuestionHardnessTuple> getHardnessTuples() {
		return hardnessTuples;
	}

	public void setHardnessTuples(List<QuestionHardnessTuple> hardnessTuples) {
		this.hardnessTuples = hardnessTuples;
	}

	public Set<String> getLeastUnderstoodConcepts() {
		return leastUnderstoodConcepts;
	}

	public void setLeastUnderstoodConcepts(HashSet<String> leastUnderstoodConcepts) {
		this.leastUnderstoodConcepts = leastUnderstoodConcepts;
	}

	public Set<String> getMostUnderstoodConcepts() {
		return mostUnderstoodConcepts;
	}

	public void setMostUnderstoodConcepts(HashSet<String> mostUnderstoodConcepts) {
		this.mostUnderstoodConcepts = mostUnderstoodConcepts;
	}

	public int getMaximumIncorrectCount() {
		return maximumIncorrectCount;
	}

	public void setMaximumIncorrectCount(int maximumIncorrectCount) {
		this.maximumIncorrectCount = maximumIncorrectCount;
	}

	public int getMinimumAttemptCount() {
		return minimumAttemptCount;
	}

	public void setMinimumAttemptCount(int minimumAttemptCount) {
		this.minimumAttemptCount = minimumAttemptCount;
	}

	public int getNumberOfUsersAboveThreshold() {
		return numberOfUsersAboveThreshold;
	}

	public void setNumberOfUsersAboveThreshold(int numberOfUsersAboveThreshold) {
		this.numberOfUsersAboveThreshold = numberOfUsersAboveThreshold;
	}
	
	public boolean isUseMedian() {
		return useMedian;
	}

	public void setUseMedian(boolean useMedian) {
		this.useMedian = useMedian;
	}

	public boolean isParsed() {
		return parsed;
	}

	public void setParsed(boolean parsed) {
		this.parsed = parsed;
	}
}