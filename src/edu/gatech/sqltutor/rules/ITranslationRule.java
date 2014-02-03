package edu.gatech.sqltutor.rules;


/**
 * Rule that matches on portions of an 
 * SQL query and produces some annotation 
 * or output for a natural language description.
 */
public interface ITranslationRule {
	
	public static final int TYPE_SQL = 1;
	public static final int TYPE_SYMBOLIC = 2;
	
	/** 
	 * Returns the precedence of this rule.  
	 * Rules should be applied in order of precedence.
	 * @return the precedence.
	 */
	public int getPrecedence();
	
	/**
	 * Returns the type of this translation rule, for determining 
	 * sub-interfaces.
	 * 
	 * @return the translation rule type
	 */
	public int getType();
}
