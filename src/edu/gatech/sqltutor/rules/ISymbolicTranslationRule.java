package edu.gatech.sqltutor.rules;

/**
 * A translation rule that acts on the symbolic fragment state.
 */
public interface ISymbolicTranslationRule extends ITranslationRule {
	/**
	 * Applies the rule if possible.  Symbolic translation rules 
	 * may modify the symbolic sentence structure but should not 
	 * modify the accompanying SQL state.
	 * 
	 * @param state the current symbolic state
	 */
	@Override
	public boolean apply(SymbolicState state); 
}
