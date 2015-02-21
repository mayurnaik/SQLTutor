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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.gatech.sqltutor.DatabaseManager;
import edu.gatech.sqltutor.DatabaseTable;
import edu.gatech.sqltutor.QueryResult;
import edu.gatech.sqltutor.QuestionTuple;
import edu.gatech.sqltutor.entities.SchemaOptionsTuple;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.ERSerializer;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.rules.lang.SymbolicFragmentTranslator;

@ManagedBean
@ViewScoped
public class TutorialPageBean extends AbstractDatabaseBean implements
		Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(TutorialPageBean.class);

	@ManagedProperty(value = "#{userBean}")
	private UserBean userBean;

	private List<DatabaseTable> tables;
	private List<QuestionTuple> questionTuples;
	private int questionIndex;
	private String query;
	private String feedbackNLP;
	private String resultSetFeedback;
	private QueryResult queryResult;
	private QueryResult answerResult;
	private String link;
	private String schema;
	private SchemaOptionsTuple schemaOptions;
	
	public static final String WEAKLY_CORRECT_MESSAGE = "Your answer is \"weakly correct\". It works for the small set of instances we have available.";
	public static final String ANSWER_MALFORMED_MESSAGE = "We are unable to give feedback for this question, the stored answer is malformed.";
	public static final String NO_PERMISSIONS_MESSAGE = "You do not have permission to run this query.";
	public static final String NO_QUESTIONS_MESSAGE = "No questions are available for this schema.";
	public static final String TRUNCATED_QUERY_MESSAGE = "Your query produced a result that was unreasonably large.";
	public static final int RESULT_ROW_LIMIT = 50;
	
	public void preRenderSetup(ComponentSystemEvent event) throws IOException {
		if (!userBean.isLoggedIn())
			return; //TODO: this is to avoid both preRenderEvents firing, not sure if there is a better way.
		
		if (link != null) {
			try {
				schema = getDatabaseManager().getUserSchema(link);
				if (schema == null) {
					log.error("Invalid schema link used: {}", link);
					BeanUtils.addErrorMessage(null, "Unable to find a schema with that link!", true);
					BeanUtils.redirect("/HomePage.jsf");
					return;
				}
			} catch (SQLException e) {
				for (Throwable t : e) {
					t.printStackTrace();
					logException(t, userBean.getHashedEmail());
				}
				BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
			}
		} else {
			schema = userBean.getSelectedSchema();
		}
		
		try {
			schemaOptions = getDatabaseManager().getOptions(schema);

			boolean accessible = true;
			final Calendar now = Calendar.getInstance(TimeZone.getTimeZone("EST"));

			if (schemaOptions.getOpenAccess() != null) {
				final Calendar calOpen = Calendar.getInstance(TimeZone.getTimeZone("EST"));
				calOpen.setTime(schemaOptions.getOpenAccess());
				calOpen.set(Calendar.MILLISECOND, 0);
				if(!now.after(calOpen)) {
					SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
					BeanUtils.addErrorMessage(null, "That tutorial opens " + sdf.format(calOpen.getTime()), true);
					accessible = false;
				}
			} 
			
			if (schemaOptions.getCloseAccess() != null) {
				final Calendar calClose = Calendar.getInstance(TimeZone.getTimeZone("EST"));
				calClose.setTime(schemaOptions.getCloseAccess());
				calClose.set(Calendar.MILLISECOND, 0);
				if(!now.before(calClose)) {
					SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy");
					BeanUtils.addErrorMessage(null, "That tutorial closed " + sdf.format(calClose.getTime()), true);
					accessible = false;
				}
			}
			
			if(!accessible) {
				BeanUtils.redirect("/HomePage.jsf");
				return;
			}
		} catch (SQLException e) {
			for (Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
		}
		
		try {
			tables = getDatabaseManager().getTables(schema);
		} catch (SQLException e) {
			for (Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
		setupQuestionsAndAnswers();
	}

	public void processSQL() {
		// check if we have a question
		if (!StringUtils.isEmpty(query) && !questionTuples.isEmpty()) {
			// reset all of the feedback fields
			reset();
	
			final String answer = questionTuples.get(questionIndex).getAnswer();
	
			String nlpResult = null;
			// let the user know if their query is restricted
			if (isRestricted(query)) {
				resultSetFeedback = NO_PERMISSIONS_MESSAGE;
			} else {
				// check answer
				setResultSetFeedback(answer);
				// generate nlp (if the question is incorrect and parsed
				if (!getQueryIsCorrect()
						&& !resultSetFeedback.contains("malformed"))
					nlpResult = setNLPFeedback();
			}
			
			// log
			try {
				getDatabaseManager().log(BeanUtils.getSessionId(),
						userBean.getHashedEmail(), schema,
						questionTuples.get(questionIndex).getQuestion(), answer,
						query, !isQueryMalformed(), getQueryIsCorrect(), nlpResult);
			} catch (SQLException e) {
				for (Throwable t : e) {
					t.printStackTrace();
					logException(t, userBean.getHashedEmail());
				}
			}
			
			BeanUtils.addInfoMessage(null, "Query completed!");
		} else 
			BeanUtils.addErrorMessage(null, "Cannot run queries when there are no questions.");
		
	}

	private boolean isRestricted(String query) {
		return query.toLowerCase().contains("schema_information")
				|| query.toLowerCase().contains("pg_catalog");
	}

	private String setNLPFeedback() {
		String result = null;
		if (schema.equals("company") || schema.equals("business_trip")) {
			// generate NLP feedback
			final ERSerializer serializer = new ERSerializer();
			final Class<?> c = this.getClass();

			// FIXME: hard coded for now
			ERDiagram erDiagram = (ERDiagram) serializer.deserialize(c
					.getResourceAsStream("/testdata/" + schema + ".er.xml"));
			ERMapping erMapping = (ERMapping) serializer
					.deserialize(c.getResourceAsStream("/testdata/" + schema
							+ ".mapping.xml"));

			try {
				final SymbolicFragmentTranslator queryTranslator = new SymbolicFragmentTranslator();
				queryTranslator.setQuery(query);
				queryTranslator.setSchemaMetaData(tables);
				queryTranslator.setERDiagram(erDiagram);
				queryTranslator.setERMapping(erMapping);
				resultSetFeedback += " We determined the question that you actually answered was: ";
				result = queryTranslator.getTranslation();
				feedbackNLP = "\" " + format(result) + " \"";
			} catch (Exception e) {
				resultSetFeedback += " (Sorry, we were unable to produce a sound English translation for your query.)";
				if (e instanceof SQLException) {
					for (Throwable t : (SQLException) e) {
						t.printStackTrace();
					}
				} else {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	private String format(String translation) {
		final Pattern pattern = Pattern.compile("(_.*?_)");
		final Matcher matcher = pattern.matcher(translation);
		while (matcher.find()) {
			final String oldString = matcher.group(1);
			final String newString = oldString.replaceFirst("_", "<i>")
					.replaceFirst("_", "</i>");
			translation = translation.replace(oldString, newString);
		}
		return translation;
	}

	public String getQueryResultHeader() {
		return "Query Result" 
				+ (queryResult != null && queryResult.getData().size() >= RESULT_ROW_LIMIT ? " (Showing " + RESULT_ROW_LIMIT + " out of " + queryResult.getOriginalSize() + ")" : "");
	}
	
	public String getQueryResultExampleHeader() {
		return "Your answer should resemble this example"  
				+ (answerResult != null && answerResult.getData().size() >= RESULT_ROW_LIMIT ? " (Showing " + RESULT_ROW_LIMIT + " out of " + answerResult.getOriginalSize() + ")" : "") 
				+ ":";	
	}
	
	private void setResultSetFeedback(String answer) {
		// calculate the student's query's result set, and let them know if it
		// was malformed.
		try {
			queryResult = getDatabaseManager().getQueryResult(
					schema, query, false);
		} catch (SQLException e) {
			resultSetFeedback = "Incorrect. Your query was malformed. Please try again.\n"
					+ e.getMessage();
			return;
		}
		
		// check for "strong" correctness
		// naive check.
		// TODO: Move in the clustering classes to do a better job of this.
		final String normalizedAnswer = normalize(answer);
		final String normalizedQuery = normalize(query);
		if (normalizedAnswer.equals(normalizedQuery)) {
			resultSetFeedback = "Correct! Your answer matched for all possible instances!";
		} else {
			
			// calculate the answer's result set, return and let the user know if
			// the answer is malformed
			try {
				answerResult = getDatabaseManager().getQueryResult(
						schema, answer, false);
			} catch (SQLException e) {
				resultSetFeedback = ANSWER_MALFORMED_MESSAGE;
				log.warn("Error in stored answer for {} question #{}.\nStored query: {}\nException: {}", 
						schema, questionIndex, answer, e);
				return;
			}
			
			// check if we truncated the result, we never consider this correct and assume the instructor answer is smaller
			if (queryResult.isTruncated()) {
				log.warn("User query was truncated at {} out of {} rows: {}", 
						queryResult.getData().size(), queryResult.getOriginalSize(), query);
				resultSetFeedback = TRUNCATED_QUERY_MESSAGE;
				return;
			}

			if (!queryResult.getColumns()
					.containsAll(answerResult.getColumns())
					|| !answerResult.getColumns().containsAll(
							queryResult.getColumns())) {
				resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's.";
			} else {
				boolean columnOrderMatters = false;
				boolean rowOrderMatters = false;
		
				if (answer.contains(" order by "))
					rowOrderMatters = true;
				
				if (columnOrderMatters && rowOrderMatters) {
					if (answerResult.equals(queryResult)) {
						resultSetFeedback = WEAKLY_CORRECT_MESSAGE;
					} else {
						resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's.";
					}
				} else if (!columnOrderMatters && rowOrderMatters) {
					final Map<String, List<String>> queryTree = new TreeMap<String, List<String>>();
					final Map<String, List<String>> answerTree = new TreeMap<String, List<String>>();
		
					for (int i = 0; i < queryResult.getColumns().size(); i++) {
						final List<String> columnData = new ArrayList<String>(queryResult.getData().size());
						for (int j = 0; j < queryResult.getData().size(); j++) {
							columnData.add(queryResult.getData().get(j).get(i));
						}
						queryTree.put(queryResult.getColumns().get(i), columnData);
					}
		
					for (int i = 0; i < answerResult.getColumns().size(); i++) {
						final List<String> columnData = new ArrayList<String>(answerResult.getData().size());
						for (int j = 0; j < answerResult.getData().size(); j++) {
							columnData.add(answerResult.getData().get(j).get(i));
						}
						answerTree.put(answerResult.getColumns().get(i), columnData);
					}
		
					if (queryTree.equals(answerTree)) {
						resultSetFeedback = WEAKLY_CORRECT_MESSAGE;
					} else {
						resultSetFeedback = "Incorrect. Your query's data or order differed from the stored answer's.";
					}
				} else if (columnOrderMatters && !rowOrderMatters) {
					final String diff = "SELECT count(*) FROM (" + normalizedQuery + " EXCEPT " + normalizedAnswer 
							+ " UNION ALL " + normalizedAnswer + " EXCEPT " + normalizedQuery + ") t";
					try {
						final QueryResult diffResult = getDatabaseManager().getQueryResult(schema, diff, false);
						if ("0".equals(diffResult.getData().get(0).get(0))) {
							resultSetFeedback = WEAKLY_CORRECT_MESSAGE;
						} else {
							resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's.";
						}
					} catch (SQLException e) {
						if (e.getMessage().contains("columns")) {
							resultSetFeedback = "Incorrect. The number of columns in your result did not match the answer.";
						} else if (e.getMessage().contains("type")) {
							resultSetFeedback = "Incorrect. One or more of your result's data types did not match the answer.";
						} else {
							for (Throwable t : e) {
								t.printStackTrace();
								logException(t, userBean.getHashedEmail());
							}
						}
					}
				} else {
					final Multiset<String> queryBag = HashMultiset.create();
					final Multiset<String> answerBag = HashMultiset.create();
		
					for (int i = 0; i < queryResult.getColumns().size(); i++) {
						for (int j = 0; j < queryResult.getData().size(); j++) {
							queryBag.add(queryResult.getData().get(j).get(i));
						}
					}
		
					for (int i = 0; i < answerResult.getColumns().size(); i++) {
						for (int j = 0; j < answerResult.getData().size(); j++) {
							answerBag.add(answerResult.getData().get(j).get(i));
						}
					}
		
					if (queryBag.equals(answerBag)) {
						resultSetFeedback = WEAKLY_CORRECT_MESSAGE;
					} else {
						resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's.";
					}
				}
			}	
		}
	}
	
	/**
	 * Trims the query and removes all semicolons. Lowercases all characters outside of parenthesis and apostrophes.
	 * @param query		the query to be normalized
	 * @return			a normalized query
	 */
	public String normalize(final String query) {
		// trim leading and trailing whitespaces and remove all semicolons
		StringBuilder sb = new StringBuilder(query.trim());
		
		// remove trailing semicolon
		if(sb.charAt(sb.length()-1) == ';') {
			sb.deleteCharAt(sb.length()-1);
			// get rid of any trailing spaces that were before the semicolon
			sb = new StringBuilder(sb.toString().trim());
		}
		
		// remove all new lines and lower case and single space everything outside of parenthesis and apostrophes
		boolean openParen = false;
		boolean openApos = false;
		boolean lastCharIsSpace = false;
		for(int i = 0; i < sb.length(); i++) {
			final char c = sb.charAt(i);
			
			if (c != ' ')
				lastCharIsSpace = false;
			
			if (c == '"' && !openApos) 
				openParen = !openParen;
			else if (c == '\'' && !openParen) 
				openApos = !openApos;
			else if (!openApos && !openParen) {
				if (c == '\n' || c == '\r') {
					sb.deleteCharAt(i--);
					i -= 1;
				} else if (c == ' ') {
					if (lastCharIsSpace) 
						sb.deleteCharAt(i--);
					else
						lastCharIsSpace = true;
				} else
					sb.setCharAt(i, Character.toLowerCase(c));
			}
		}
		
		return sb.toString(); 
	}

	public void submitFeedback() {
		// FIXME We'll need to decide how we're going to store this.
		final String feedbackMessagesId = FacesContext.getCurrentInstance()
				.getViewRoot().findComponent(":feedbackForm:feedbackMessages")
				.getClientId();
		FacesContext.getCurrentInstance().addMessage(feedbackMessagesId,
				new FacesMessage("We appreciate your submission."));
	}

	public void setupQuestionsAndAnswers() {
		questionTuples = null;
		final DatabaseManager databaseManager = getDatabaseManager();
		try {
			questionTuples = databaseManager.getQuestions(schema);
		} catch (SQLException e) {
			for (Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
			return;
		}

		if (!questionTuples.isEmpty()) {
			SchemaOptionsTuple options = null;
			try {
				options = databaseManager.getOptions(schema);
			} catch (SQLException e) {
				for (Throwable t : e) {
					t.printStackTrace();
					logException(t, userBean.getHashedEmail());
				}
				BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
				return;
			}

			if (!options.isInOrderQuestions()) {
				Collections.shuffle(questionTuples,
						new Random(System.nanoTime()));
			}
		}
	}

	public void nextQuestion() {
		setQuestionIndex(questionIndex+1);
	}

	private void reset() {
		queryResult = null;
		answerResult = null;
		feedbackNLP = "";
		resultSetFeedback = "";
	}

	public String getQuestion() {
		// Log that a question was retrieved
		if(questionTuples == null || questionTuples.isEmpty())
			return NO_QUESTIONS_MESSAGE;
		try {
			getDatabaseManager().logQuestionPresentation(
					BeanUtils.getSessionId(), userBean.getHashedEmail(),
					schema,
					questionTuples.get(questionIndex).getQuestion());
		} catch (SQLException e) {
			for (Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
		return questionTuples.get(questionIndex).getQuestion();
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

	public List<DatabaseTable> getTables() {
		return tables;
	}

	public QueryResult getAnswerResult() {
		return answerResult;
	}

	public String getResultSetFeedback() {
		return resultSetFeedback;
	}

	public boolean getQueryIsCorrect() {
		if (resultSetFeedback == null
				|| !resultSetFeedback.toLowerCase().contains("incorrect")) {
			return true;
		}
		return false;
	}

	public boolean isQueryMalformed() {
		if (resultSetFeedback.toLowerCase().contains("malformed")) {
			return true;
		}
		return false;
	}

	public int getQuestionNumber(QuestionTuple question) {
		return questionTuples.indexOf(question) + 1;
	}

	/**
	 * Question numbering is not 0-indexed, so we must convert it before
	 * returning it.
	 * 
	 * @return non-0-indexed question number.
	 */
	public int getQuestionNumber() {
		return questionIndex + 1;
	}

	/**
	 * Question numbering is not 0-indexed, so it must be converted before the
	 * index is set.
	 * 
	 * @param questionNumber
	 *            non-0-indexed question number.
	 */
	public void setQuestionNumber(int questionNumber) {
		setQuestionIndex(questionNumber - 1);
	}

	public void setQuestionIndex(int questionIndex) {
		if(questionIndex != this.questionIndex)	{
			if (questionIndex >= questionTuples.size())  {
				this.questionIndex = 0;
				BeanUtils.addInfoMessage(null, "End of questions. Changed to question number " + getQuestionNumber() + ".");
			} else {
				this.questionIndex = questionIndex;
				BeanUtils.addInfoMessage(null, "Changed to question number " + getQuestionNumber() + ".");
			}
			reset();
		}
	}

	public List<QuestionTuple> getQuestionTuples() {
		return questionTuples;
	}

	public String getQuestionDescription(QuestionTuple question) {
		return "Question Number " + getQuestionNumber(question);
		// return question.isAttempted() ? "Attempted." : "Not attempted.";
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	public int getResultRowLimit() {
		return RESULT_ROW_LIMIT;
	}

	public SchemaOptionsTuple getOptions() {
		return schemaOptions;
	}

	public void setOptions(SchemaOptionsTuple options) {
		this.schemaOptions = options;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}
}
