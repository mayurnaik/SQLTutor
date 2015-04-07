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
package edu.gatech.sqltutor.tuples;

import java.io.Serializable;
import java.util.Date;

public class UserQuery implements Serializable {
	private static final long serialVersionUID = 1L;

	private String email;
	private String query;
	private String schema;
	private String question;
	private String correctAnswer;
	private int order;
	private Date time;
	private boolean parsed;
	private boolean correct;
	private boolean readLimitExceeded;
	
	public UserQuery(String email, String query, String schema,
			String question, String correctAnswer, int order, Date time,
			boolean parsed, boolean correct, boolean readLimitExceeded) {
		super();
		this.email = email;
		this.query = query;
		this.schema = schema;
		this.question = question;
		this.correctAnswer = correctAnswer;
		this.order = order;
		this.time = time;
		this.parsed = parsed;
		this.correct = correct;
		this.readLimitExceeded = readLimitExceeded;
	}

	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getSchema() {
		return schema;
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public Date getTime() {
		return time;
	}
	
	public void setTime(Date time) {
		this.time = time;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getCorrectAnswer() {
		return correctAnswer;
	}

	public void setCorrectAnswer(String correctAnswer) {
		this.correctAnswer = correctAnswer;
	}

	public boolean isParsed() {
		return parsed;
	}

	public void setParsed(boolean parsed) {
		this.parsed = parsed;
	}

	public boolean isCorrect() {
		return correct;
	}

	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	public boolean isReadLimitExceeded() {
		return readLimitExceeded;
	}

	public void setReadLimitExceeded(boolean readLimitExceeded) {
		this.readLimitExceeded = readLimitExceeded;
	}
	
}
