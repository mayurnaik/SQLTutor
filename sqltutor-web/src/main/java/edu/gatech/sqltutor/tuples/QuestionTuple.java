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
import java.util.Arrays;

public class QuestionTuple implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int order;
	private String question;
	private String answer;
	private String[] concepts;
	private boolean columnOrderMatters;
	private boolean rowOrderMatters;
	private double performanceLeniencySeconds;
	
	public QuestionTuple() {}
	
	public QuestionTuple(int order, String question, String answer, String[] concepts, double performanceLeniencySeconds, boolean columnOrderMatters, boolean rowOrderMatters) {
		this.order = order;
		this.question = question;
		this.answer = answer;
		this.concepts = concepts;
		this.performanceLeniencySeconds = performanceLeniencySeconds;
		this.columnOrderMatters = columnOrderMatters;
		this.rowOrderMatters = rowOrderMatters;
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
	
	public String getAnswer() {
		return answer;
	}
	
	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String[] getConcepts() {
		return concepts;
	}

	public void setConcepts(String[] concepts) {
		this.concepts = concepts;
	}
	
	public String getConceptsString() {
		if (concepts == null || concepts.length == 0) 
			return null;
		else {
			String conceptsString = Arrays.toString(concepts);
			return conceptsString.substring(1, conceptsString.length() - 1);
		}
	}
	
	public void setConceptsString(String concepts) {
		this.concepts = (concepts == null || concepts.length() == 0 ? null  : concepts.toLowerCase().split(","));
	}

	public boolean isColumnOrderMatters() {
		return columnOrderMatters;
	}

	public void setColumnOrderMatters(boolean columnOrderMatters) {
		this.columnOrderMatters = columnOrderMatters;
	}

	public boolean isRowOrderMatters() {
		return rowOrderMatters;
	}

	public void setRowOrderMatters(boolean rowOrderMatters) {
		this.rowOrderMatters = rowOrderMatters;
	}

	public double getPerformanceLeniencySeconds() {
		return performanceLeniencySeconds;
	}

	public void setPerformanceLeniencySeconds(double performanceLeniencySeconds) {
		this.performanceLeniencySeconds = performanceLeniencySeconds;
	}
}
