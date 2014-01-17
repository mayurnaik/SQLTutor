package edu.gatech.sqltutor.rules;

public class DefaultPrecedence {
	/** For rules that will destructively update the AST. */
	public static final int DESTRUCTIVE_UPDATE = 1000;
	
	/** For rules that will just reorganize the symbolic sentence structure. */
	public static final int FRAGMENT_REWRITE = 100;
	
	/** For rules that will lower to literal natural language. */
	public static final int LOWERING = 10;
}
