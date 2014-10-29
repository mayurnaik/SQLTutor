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
package edu.gatech.sqltutor;

import java.util.List;

/**
 * Query translators produce natural language 
 * descriptions from SQL queries.
 * <p>
 * Query translators must have the query set before 
 * calling <code>getTranslation()</code>.  Schema 
 * meta data is optional, but may cause some translations 
 * to fail when missing that would succeed otherwise.
 * </p>
 */
public interface IQueryTranslator {
	/** Sets the query to be translated. */
	public void setQuery(String sql);
	/** Returns the query to be translated. */
	public String getQuery();
	
	// FIXME list is not a good type, harder to refactor
	public void setSchemaMetaData(List<DatabaseTable> tables);
	public List<DatabaseTable> getSchemaMetaData();
	
	// FIXME return type?
	/**
	 * Returns an object identifying the type of this translator
	 * for provenance tracking.
	 * @return the translator type
	 */
	public Object getTranslatorType();
	
	/**
	 * Returns the resulting translation.
	 * @return the translation
	 * 
	 * @throws IllegalStateException if required information (e.g. the query) has not been provided 
	 * @throws SQLTutorException if any other problem occurs generating the translation
	 */
	public String getTranslation() throws SQLTutorException;
}
