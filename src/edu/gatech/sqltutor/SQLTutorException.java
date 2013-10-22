package edu.gatech.sqltutor;

/**
 * General unchecked exception base 
 * for the SQL Tutor application. 
 */
public class SQLTutorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SQLTutorException() {
		// TODO Auto-generated constructor stub
	}

	public SQLTutorException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public SQLTutorException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public SQLTutorException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
