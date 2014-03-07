package edu.gatech.sqltutor.rules.lang;

import java.util.Collections;
import java.util.List;

import org.deri.iris.api.basics.IRule;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.SymbolicState;

public abstract class AbstractSymbolicRule implements ISymbolicTranslationRule {
	/** The symbolic state set during application. */
	protected SymbolicState state;
	
	/** The rule's precedence. */
	protected int precedence = DefaultPrecedence.FRAGMENT_REWRITE;

	public AbstractSymbolicRule() {
	}
	
	public AbstractSymbolicRule(int precedence) {
		this.precedence = precedence;
	}
	
	@Override
	public int getPrecedence() {
		return precedence;
	}
	
	@Override
	public int getType() {
		return ITranslationRule.TYPE_SYMBOLIC;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return Collections.emptyList();
	}

	public SymbolicState getState() {
		return state;
	}
}
