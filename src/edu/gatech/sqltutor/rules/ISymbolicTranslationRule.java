package edu.gatech.sqltutor.rules;

import org.deri.iris.api.IKnowledgeBase;

import com.akiban.sql.parser.SelectNode;

/**
 * A translation rule that acts on the symbolic fragment state.
 */
public interface ISymbolicTranslationRule extends ITranslationRule {
	/**
	 * Applies the rule if possible.  Symbolic translation rules 
	 * may modify <code>fragment</code> but should not modify 
	 * <code>ast</code>.
	 * 
	 * @param knowledgeBase the current datalog state
	 * @param ast           the current query AST
	 * @param fragment      the current symbolic fragment
	 * @return <code>true</code> if the rule was applied, <code>false</code> otherwise
	 */
	public boolean apply(IKnowledgeBase knowledgeBase, SelectNode ast, Object fragment); // TODO type of fragment
}
