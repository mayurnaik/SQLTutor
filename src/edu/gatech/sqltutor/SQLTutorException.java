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
