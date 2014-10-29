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

/**
 * General unchecked exception base 
 * for the SQL Tutor application. 
 */
public class SQLTutorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SQLTutorException() {
	}

	public SQLTutorException(String message) {
		super(message);
	}

	public SQLTutorException(Throwable cause) {
		super(cause);
	}

	public SQLTutorException(String message, Throwable cause) {
		super(message, cause);
	}

}
