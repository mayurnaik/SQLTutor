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
package edu.gatech.sqltutor.rules.symbolic.tokens;

/**
 * A token that is represented by a noun.
 */
public interface INounToken extends ISymbolicToken {
	public String getSingularLabel();
	public String getPluralLabel();
	/**
	 * The difference between using "the" and "a/an"
	 */
	public boolean isDefinite();
	/**
	 * The difference between using "each" and "all"
	 */
	public boolean isIndividual();
	public void setSingularLabel(String label);
	public void setPluralLabel(String label);
	public void setDefinite(boolean definite);
	public void setIndividual(boolean individual);
}
