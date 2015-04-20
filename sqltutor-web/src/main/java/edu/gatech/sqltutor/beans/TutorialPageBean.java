/*
 *   Copyright (c) 2015 Program Analysis Group, Georgia Tech
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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
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

import edu.gatech.sqltutor.DatabaseTable;
import edu.gatech.sqltutor.QueryResult;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.ERSerializer;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.rules.lang.SymbolicFragmentTranslator;
import edu.gatech.sqltutor.tuples.QuestionTuple;
import edu.gatech.sqltutor.tuples.TutorialOptionsTuple;
import edu.gatech.sqltutor.util.DatabaseManager;
import edu.gatech.sqltutor.util.QueryThread;

@ManagedBean
@ViewScoped
public class TutorialPageBean extends AbstractDatabaseBean implements
Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(TutorialPageBean.class);

	public static final String WEAKLY_CORRECT_MESSAGE = "Correct.  Your answer returns the correct results for the instance data.";
	public static final String ANSWER_MALFORMED_MESSAGE = "We are unable to give feedback for this question, the stored answer is malformed.";
	public static final String NO_PERMISSIONS_MESSAGE = "You do not have permission to run this query.";
	public static final String NO_QUESTIONS_MESSAGE = "There are no questions available for this schema.";
	public static final String TRUNCATED_QUERY_MESSAGE = "Your query produced a result that was unreasonably large.";
	public static final String TIMEOUT_MESSAGE = "Your query took too much time and was aborted.";
	public static final int RESULT_ROW_LIMIT = 50;
	public static final int TIMEOUT_SECONDS = 45;

	/**
	 * Checks whether this exception is from a query timeout.
	 * @param e the exception to check
	 * @return if this is a timeout exception
	 */
	private static boolean isTimeoutException(SQLException e) {
		String sqlState = e.getSQLState();
		if ("57014".equals(sqlState)) // "Processing was canceled as requested." Triggered by statement timeout.
			return true;
		return false;
	}

	@ManagedProperty(value = "#{userBean}")
	private UserBean userBean;

	private List<QuestionTuple> questionTuples;
	private int questionIndex;
	private String query;
	private String feedbackNLP;
	private String resultSetFeedback;
	private QueryResult queryResult;
	private QueryResult answerResult;
	/**
	 * This is the URL link which correlates to a particular schema
	 */
	private String link;
	private TutorialOptionsTuple schemaOptions;
	private boolean isQueryCorrect;
	private transient QueryThread answerThread;
	private int numberOfAttempts;
	private String comment;
	
	private SymbolicFragmentTranslator queryTranslator;
	private ERDiagram erDiagram;
	private ERMapping erMapping;

	public void preRenderSetup(ComponentSystemEvent event) throws IOException {
		if (!userBean.isLoggedIn())
			return; //TODO: this is to avoid both preRenderEvents firing, not sure if there is a better way.

		if ((userBean.getSelectedTutorial() == null || userBean.getSelectedTutorial().isEmpty()) && (link == null || link.isEmpty())) {
			BeanUtils.addErrorMessage(null, "To access the tutorial page, you must first select a tutorial.", true);
			BeanUtils.redirect("/TutorialMenuPage.jsf");
			return;
		}
			
		
		// if the page hasn't been loaded yet, load it
		if (schemaOptions == null) {
			if (link != null) {
				try {
					final String schema = getDatabaseManager().getUserSchema(link);
					if (schema == null) {
						log.error("Invalid userBean.getSelectedSchema() link used: {}", link);
						BeanUtils.addErrorMessage(null, "Unable to find a userBean.getSelectedSchema() with that link!", true);
						BeanUtils.redirect("/HomePage.jsf");
						return;
					} else {
						// if the link was valid, temporarily add it to the list of available schemas for the student
						// and set their current userBean.getSelectedSchema() to it, so if they refresh they will still be on the linked tutorial
						userBean.setSelectedTutorial(schema);
						userBean.addSelectedTutorialTemporarily();
					}
				} catch (SQLException e) {
					for (Throwable t : e) {
						t.printStackTrace();
						logException(t, userBean.getHashedEmail());
					}
					BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
				}
			} 

			try {
				schemaOptions = getDatabaseManager().getOptions(userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode());
			} catch (SQLException e) {
				for (Throwable t : e) {
					t.printStackTrace();
					logException(t, userBean.getHashedEmail());
				}
			}

			// check if the tutorial is closed before any more setup is completed
			if(!isSchemaAccessible(schemaOptions)) {
				BeanUtils.redirect("/HomePage.jsf");
				return;
			}
			
			setupQuestionsAndAnswers();
			
			try {
				// This will give the "question order", which is not 0-indexed, so we must subtract 1
				setQuestionIndex(getDatabaseManager().getMostRecentlyPresentedQuestion(userBean.getHashedEmail(), userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode()) - 1);
			} catch (SQLException e) {
				for (Throwable t : e) {
					t.printStackTrace();
					logException(t, userBean.getHashedEmail());
				}
			}
			// setup the query translator 
			// FIXME: hardcoded, need to fix this
			if (userBean.getSelectedTutorialName().equals("company") || userBean.getSelectedTutorialName().equals("business_trip")) {
				final ERSerializer serializer = new ERSerializer();
				final Class<?> c = this.getClass();
				erDiagram = (ERDiagram) serializer.deserialize(c
						.getResourceAsStream("/testdata/" + userBean.getSelectedTutorialName() + ".er.xml"));
				erMapping = (ERMapping) serializer
						.deserialize(c.getResourceAsStream("/testdata/" + userBean.getSelectedTutorialName()
								+ ".mapping.xml"));
				queryTranslator = new SymbolicFragmentTranslator();
				queryTranslator.setERDiagram(erDiagram);
				queryTranslator.setERMapping(erMapping);
				try {
					queryTranslator.setSchemaMetaData(getDatabaseManager().getDevSchemaTables());
				} catch (SQLException e) {
					for (Throwable t : e) {
						t.printStackTrace();
						logException(t, userBean.getHashedEmail());
					}
				}
			}
		} else {
			// check if the tutorial closed after the student loaded the page
			if(!isSchemaAccessible(schemaOptions)) {
				BeanUtils.redirect("/HomePage.jsf");
				return;
			}
		}
	}

	/**
	 * BalusC: "This method is called whenever the view scope has been destroyed.
	 *   		That can happen when the user navigates away by a POST which is
	 *   		invoked on this bean, or when the associated session has expired."
	 */
	@PreDestroy
	public void destroy() {
		if(answerThread != null) 
			answerThread.interrupt();
	}

	public boolean isSchemaAccessible(TutorialOptionsTuple options) {
		if (options == null) {
			BeanUtils.addErrorMessage(null, "That tutorial is closed.", true);
			return false;
		}

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
		return accessible;
	}

	public void processSQL() {
		// check if we have a question
		if (!StringUtils.isEmpty(query) && questionTuples != null && !questionTuples.isEmpty()) {	
			final long startTimeMilliseconds = Calendar.getInstance().getTime().getTime();
			// reset all of the feedback fields
			resetResultsAndFeedback();
			
			if (schemaOptions.getMaxQuestionAttempts() != 0 && getNumberOfAttempts() >= schemaOptions.getMaxQuestionAttempts()) {
				resultSetFeedback = "You've exceeded the maximum number of attempts for this question.";
				return;
			}
			
			String nlpResult = null;
			// let the user know if their query is restricted
			if (isRestricted(query)) {
				resultSetFeedback = NO_PERMISSIONS_MESSAGE;
			} else {
				// check answer
				setResultSetFeedback();
				// generate nlp (if the question is incorrect and parsed)
				if (queryResult != null && !getQueryIsCorrect())
					nlpResult = setNLPFeedback();
			} 
			
			if (!getQueryIsCorrect() && queryResult != null)
				numberOfAttempts++;
				
			// log

			final QuestionTuple questionTuple = questionTuples.get(questionIndex);
			try {
				final double totalTimeTakenSeconds = (Calendar.getInstance().getTime().getTime() - startTimeMilliseconds)/1000d; 
				getDatabaseManager().log(BeanUtils.getSessionId(),
						userBean.getHashedEmail(), 
						userBean.getSelectedTutorialName(),
						questionTuple.getQuestion(), 
						questionTuple.getAnswer(),
						query, queryResult != null, getQueryIsCorrect(), nlpResult, totalTimeTakenSeconds, 
						queryResult != null  ? queryResult.getTotalTime()/1000d : 0, 
						answerResult != null ? answerResult.getTotalTime()/1000d : 0, 
						queryResult != null ? queryResult.getExecutionTime()/1000d : 0,
						answerResult != null ? answerResult.getExecutionTime()/1000d : 0, 
						queryResult != null ? queryResult.isTruncated() : false, 
						queryResult != null ? queryResult.isReadLimitExceeded() : false, 
						queryResult != null ? queryResult.getOriginalSize() : 0,
						userBean.getSelectedTutorialAdminCode());
			} catch (SQLException e) {
				for (Throwable t : e) {
					t.printStackTrace();
					logException(t, userBean.getHashedEmail());
				}
			}
		} else 
			BeanUtils.addErrorMessage(null, "Cannot run queries when there are no questions.");
	}

	private boolean isRestricted(String query) {
		return query.toLowerCase().contains("schema_information")
				|| query.toLowerCase().contains("pg_catalog");
	}

	private String setNLPFeedback() {
		String result = null;
		if (queryTranslator != null) {
			try {
				queryTranslator.setQuery(query);
				result = queryTranslator.getTranslation();
				feedbackNLP = "We determined the question that you actually answered was: \" " + format(result) + " \"";
			} catch (Exception e) {
				feedbackNLP += " (Sorry, we were unable to produce sound English translation feedback for your query.)";
				e.printStackTrace();
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

	private StringBuilder formatResultHeader(QueryResult result, StringBuilder header) {
		if (result != null) {
			long execTime = result.getExecutionTime();
			if (execTime >= 0L) {
				NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
				nf.setMinimumFractionDigits(0);
				nf.setMaximumFractionDigits(4);
				header.append(" [").append(nf.format(execTime / 1000d)).append("s]");
			}

			if (result.getData().size() >= RESULT_ROW_LIMIT) {
				NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
				header.append(" (Showing ").append(nf.format(RESULT_ROW_LIMIT)).append(" out of ").append(nf.format(result.getOriginalSize()));
				if (result.isReadLimitExceeded())
					header.append("+ [limit reached]");
				header.append(")");
			}
		}
		return header;
	}

	public String getQueryResultHeader() {
		StringBuilder header = formatResultHeader(queryResult, new StringBuilder("Query Result"));
		return header.toString();
	}

	public String getQueryResultExampleHeader() {
		if( answerResult == null ) {
			return "";
		} else {
			StringBuilder header = formatResultHeader(answerResult, new StringBuilder("Your answer should resemble this example"));
			header.append(':');
			return header.toString();
		}
	}

	private void setResultSetFeedback() {
		// calculate the student's query's result set, and let them know if it
		// was malformed.
		try {
			queryResult = getDatabaseManager().getQueryResult(userBean.getSelectedTutorial(), query, false);
		} catch (SQLException e) {
			if (isTimeoutException(e)) {
				resultSetFeedback = TIMEOUT_MESSAGE;
				log.warn("Statement timeout reached for user query (schema={}, question={}): {}", userBean.getSelectedTutorial(), questionIndex, query);
			} else {
				resultSetFeedback = "Incorrect. Your query was malformed. Please try again.\n"
						+ e.getMessage();
			}
			return;
		}

		// check for "strong" correctness
		// naive check
		// TODO: Move in the clustering classes to do a better job of this
		final QuestionTuple questionTuple = questionTuples.get(questionIndex);
		final String normalizedAnswer = normalize(questionTuple.getAnswer());
		final String normalizedQuery = normalize(query);
		if (normalizedAnswer.equals(normalizedQuery)) {
			resultSetFeedback = "Correct! Your answer matched for all possible instances!";
			isQueryCorrect = true;
		} else {
			// wait for the answerThread to finish before moving forward
			try {
				answerThread.join();
			} catch (InterruptedException e) {
				log.warn("Computing the answer result-set to question at index #{} was interrupted.", questionIndex);
				return;
			}
			// if the answerThread had an SQLException, let the user know and back-out.
			if (answerThread.getException() != null) {
				resultSetFeedback = ANSWER_MALFORMED_MESSAGE;
				log.warn("Error in stored answer for {} question #{}.\nStored query: {}\nException: {}", 
						userBean.getSelectedTutorial(), questionIndex, normalizedAnswer, answerThread.getException());
				return;
			} 

			answerResult = answerThread.getQueryResult();
			
			if (questionTuple.getPerformanceLeniencySeconds() != 0 && queryResult.getExecutionTime() > answerResult.getExecutionTime() + (questionTuple.getPerformanceLeniencySeconds() * 1000)) {
				resultSetFeedback = "Incorrect. Your query's execution time was longer than the answer's plus the alotted leniency time (" + questionTuple.getPerformanceLeniencySeconds() + ").";
				return;
			}

			// check if we truncated the result, we never consider this correct and assume the instructor answer is smaller
			if (queryResult.isTruncated()) {
				log.warn("User query was truncated at {} out of {} rows: {}", 
						queryResult.getData().size(), queryResult.getOriginalSize(), query);
				resultSetFeedback = TRUNCATED_QUERY_MESSAGE;
				return;
			}

			// First checks if the number of columns are equal, then the number of rows, then moves into specialized checks
			// based on if column and/or row order matters
			if (answerResult.getColumns().size() != queryResult.getColumns().size()) {
				resultSetFeedback = "Incorrect. Your query did not have the same number of columns as the stored answer.";
			} else if (answerResult.getData().size() != queryResult.getData().size()) {
				resultSetFeedback = "Incorrect. Your query did not have the same number of rows as the stored answer.";
			} else if (questionTuple.isColumnOrderMatters() && questionTuple.isRowOrderMatters()) {
				if (answerResult.getData().equals(queryResult.getData())) {
					resultSetFeedback = WEAKLY_CORRECT_MESSAGE;
					isQueryCorrect = true;
				} else {
					resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's (note: column and row order matters).";
				}
			} else if (!questionTuple.isColumnOrderMatters() && questionTuple.isRowOrderMatters()) {
				// Puts the column's data into a map, mapped to the column name. Orders by column name then checks equality.
				final Map<String, List<String>> queryTree = new TreeMap<String, List<String>>();
				final Map<String, List<String>> answerTree = new TreeMap<String, List<String>>();

				for (int i = 0; i < queryResult.getColumns().size(); i++) {
					final List<String> columnData = new ArrayList<String>(queryResult.getData().size());
					for (int j = 0; j < queryResult.getData().size(); j++) {
						columnData.add(queryResult.getData().get(j).get(i));
						columnData.add(answerResult.getData().get(j).get(i));
					}
					queryTree.put(queryResult.getColumns().get(i), columnData);
					answerTree.put(answerResult.getColumns().get(i), columnData);
				}

				if (queryTree.equals(answerTree)) {
					resultSetFeedback = WEAKLY_CORRECT_MESSAGE;
					isQueryCorrect = true;
				} else {
					resultSetFeedback = "Incorrect. Your query's data or order differed from the stored answer's (note: row order matters).";
				}
			} else if (questionTuple.isColumnOrderMatters() && !questionTuple.isRowOrderMatters()) {
				final String diff = "SELECT count(*) FROM ((" + normalizedQuery + ") EXCEPT (" + normalizedAnswer 
						+ ") UNION ALL (" + normalizedAnswer + ") EXCEPT (" + normalizedQuery + ")) t";
				try {
					final QueryResult diffResult = getDatabaseManager().getQueryResult(userBean.getSelectedTutorial(), diff, false);
					if ("0".equals(diffResult.getData().get(0).get(0))) {
						resultSetFeedback = WEAKLY_CORRECT_MESSAGE;
						isQueryCorrect = true;
					} else {
						resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's (note: column order matters).";
					}
				} catch (SQLException e) {
					if (isTimeoutException(e)) {
						resultSetFeedback = "We were unable to check your answer in time.";
						log.warn("Timed out checking difference with query: {}", diff);
					} else if (e.getMessage().contains("columns")) {
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
				// Puts all of the data into bags then compares the counts
				final Multiset<String> queryBag = HashMultiset.create();
				final Multiset<String> answerBag = HashMultiset.create();

				for (int i = 0; i < queryResult.getData().size(); i++) {
					queryBag.addAll(queryResult.getData().get(i));
					// we already checked to make sure they're the same size
					answerBag.addAll(answerResult.getData().get(i));
				}

				if (queryBag.equals(answerBag)) {
					resultSetFeedback = WEAKLY_CORRECT_MESSAGE;
					isQueryCorrect = true;
				} else {
					resultSetFeedback = "Incorrect. Your query's data differed from the stored answer's.";
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
			questionTuples = databaseManager.getQuestions(userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode());
		} catch (SQLException e) {
			for (Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
			return;
		}

		if (questionTuples != null && !questionTuples.isEmpty()) {
			TutorialOptionsTuple options = null;
			try {
				options = databaseManager.getOptions(userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode());
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

	/**
	 * This method should reset the bean's result-sets and feedback.
	 */
	private void resetResultsAndFeedback() {
		queryResult = null;
		answerResult = null;
		feedbackNLP = "";
		resultSetFeedback = "";
		isQueryCorrect = false;
	}

	public String getQuestion() {
		if(questionTuples == null || questionTuples.isEmpty())
			return NO_QUESTIONS_MESSAGE;
		// Log that a question was retrieved
		try {
			getDatabaseManager().logQuestionPresentation(
					BeanUtils.getSessionId(), userBean.getHashedEmail(),
					userBean.getSelectedTutorialName(),
					questionTuples.get(questionIndex).getQuestion(), 
					userBean.getSelectedTutorialAdminCode());
		} catch (SQLException e) {
			for (Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
		// Start a thread to pre-compute the answer
		if(answerThread != null) 
			answerThread.interrupt();
		answerThread = new QueryThread(userBean.getSelectedTutorial(), questionTuples.get(questionIndex).getAnswer(), false, getDatabaseManager());
		answerThread.start();
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

	public QueryResult getAnswerResult() {
		return answerResult;
	}

	public String getResultSetFeedback() {
		return resultSetFeedback;
	}

	public boolean getQueryIsCorrect() {
		return isQueryCorrect;
	}

	public boolean getShowExample() {
		return queryResult != null && answerResult != null && !isQueryCorrect;
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
			if (questionIndex < 0) {
				this.questionIndex = 0;
			} else if (questionIndex >= questionTuples.size())  {
				this.questionIndex = 0;
				BeanUtils.addInfoMessage(null, "End of questions. Changed to question number " + getQuestionNumber() + ".");
			} else {
				this.questionIndex = questionIndex;
				BeanUtils.addInfoMessage(null, "Changed to question number " + getQuestionNumber() + ".");
			}
			
			resetResultsAndFeedback();
			
			if (questionTuples != null && !questionTuples.isEmpty() && questionIndex < questionTuples.size() && questionIndex >= 0) {
				try {
					numberOfAttempts = getDatabaseManager().getNumberOfAttempts(userBean.getHashedEmail(), userBean.getSelectedTutorialName(), questionTuples.get(questionIndex).getQuestion(), true, false, userBean.getSelectedTutorialAdminCode());
				} catch (SQLException e) {
					for (Throwable t : e) {
						t.printStackTrace();
						logException(t, userBean.getHashedEmail());
					}
				}
			}
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

	public TutorialOptionsTuple getOptions() {
		return schemaOptions;
	}

	public void setOptions(TutorialOptionsTuple options) {
		this.schemaOptions = options;
	}

	public int getNumberOfAttempts() {
		return numberOfAttempts;
	}

	public void setNumberOfAttempts(int numberOfAttempts) {
		this.numberOfAttempts = numberOfAttempts;
	}
	
	public String getQuestionHeader() {
		StringBuilder header = new StringBuilder();
		header.append("Question");
		if (schemaOptions.getMaxQuestionAttempts() == 0)
			header.append(" [no attempt cap]");
		else
			header.append(" [" + (numberOfAttempts > schemaOptions.getMaxQuestionAttempts() ? schemaOptions.getMaxQuestionAttempts() : numberOfAttempts) + " out of " + schemaOptions.getMaxQuestionAttempts() + " attempts used]");
		return header.toString();
	}
	
	public void submitComment() {
		try {
			getDatabaseManager().addComment(userBean.getSelectedTutorialName(), getQuestionNumber(), getComment(), userBean.getSelectedTutorialAdminCode(), userBean.getEmail());
			BeanUtils.addInfoMessage(null, "Successfully left a comment on question number " + getQuestionNumber() + ".");
		} catch (SQLException e) {
			for (Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
		}
	}
	
	public void markForReview() {
		try {
			getDatabaseManager().markForReview(userBean.getHashedEmail());
			BeanUtils.addInfoMessage(null, "Successfully marked your answer for review.");
		} catch (SQLException e) {
			for (Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
		}
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}