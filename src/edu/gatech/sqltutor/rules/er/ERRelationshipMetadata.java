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
package edu.gatech.sqltutor.rules.er;

public class ERRelationshipMetadata {
	private String negatedSingularVerbForm;
	private String negatedPluralVerbForm;
	private String alternateSingularVerbForm;
	private String alternatePluralVerbForm;
	
	public ERRelationshipMetadata() {
	}

	public String getNegatedSingularVerbForm() {
		return negatedSingularVerbForm;
	}

	public void setNegatedSingularVerbForm(String negatedSingularVerbForm) {
		this.negatedSingularVerbForm = negatedSingularVerbForm;
	}

	public String getNegatedPluralVerbForm() {
		return negatedPluralVerbForm;
	}

	public void setNegatedPluralVerbForm(String negatedPluralVerbForm) {
		this.negatedPluralVerbForm = negatedPluralVerbForm;
	}

	public String getAlternatePluralVerbForm() {
		return alternatePluralVerbForm;
	}

	public void setAlternatePluralVerbForm(String alternatePluralVerbForm) {
		this.alternatePluralVerbForm = alternatePluralVerbForm;
	}

	public String getAlternateSingularVerbForm() {
		return alternateSingularVerbForm;
	}

	public void setAlternateSingularVerbForm(String alternateSingularVerbForm) {
		this.alternateSingularVerbForm = alternateSingularVerbForm;
	}
}
