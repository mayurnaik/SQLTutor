package edu.gatech.sqltutor.rules;

import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.rules.graph.TranslationGraph;

/**
 * Rule that matches on portions of an 
 * SQL query and produces some annotation 
 * or output for a natural language description.
 */
public interface ITranslationRule {
	public int getPrecedence();
	
	/**
	 * Apply any transformation to the given statement AST.
	 * 
	 * @graph the translation graph being built
	 * @param statement the query AST
	 * @return <code>true</code> if the rule applies, <code>false</code> otherwise
	 * @deprecated Not used in symbolic language fragment strategy
	 */
	@Deprecated
	public boolean apply(TranslationGraph graph, StatementNode statement);
	
	/**
	 * Apply any transformation to the given statement AST.
	 * 
	 * @param statement the query AST
	 * @return <code>true</code> if the rule applies, <code>false</code> otherwise
	 */
	public boolean apply(StatementNode statement);
}
