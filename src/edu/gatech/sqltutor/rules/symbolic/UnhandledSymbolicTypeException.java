package edu.gatech.sqltutor.rules.symbolic;

/** Exception indicating a symbolic token was encountered that could not be handled. */
public class UnhandledSymbolicTypeException extends SymbolicException {
	private static final long serialVersionUID = 1L;
	
	protected final SymbolicType type;

	public UnhandledSymbolicTypeException(SymbolicType type) {
		this.type = type;
	}

	public UnhandledSymbolicTypeException(SymbolicType type, String message) {
		super(message);
		this.type = type;
	}

	public UnhandledSymbolicTypeException(SymbolicType type, Throwable cause) {
		super(cause);
		this.type = type;
	}

	public UnhandledSymbolicTypeException(SymbolicType type, String message, Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	public UnhandledSymbolicTypeException(String message) {
		this(null, message);
	}

	public UnhandledSymbolicTypeException(Throwable cause) {
		this((SymbolicType)null, cause);
	}

	public UnhandledSymbolicTypeException(String message, Throwable cause) {
		this(null, message, cause);
	}
	
	public SymbolicType getSymbolicType() {
		return type;
	}
}
