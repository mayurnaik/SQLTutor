package edu.gatech.sqltutor.rules;



/**
 * Translation rule that applies some information at 
 * the AST level.
 */
public interface ISQLTranslationRule extends ITranslationRule {
	
	public boolean apply(SQLState state);
}
