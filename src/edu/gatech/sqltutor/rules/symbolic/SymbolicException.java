package edu.gatech.sqltutor.rules.symbolic;

import edu.gatech.sqltutor.SQLTutorException;

public class SymbolicException extends SQLTutorException {
	private static final long serialVersionUID = 1L;

	public SymbolicException() {
	}

	public SymbolicException(String message) {
		super(message);
	}

	public SymbolicException(Throwable cause) {
		super(cause);
	}

	public SymbolicException(String message, Throwable cause) {
		super(message, cause);
	}
}
