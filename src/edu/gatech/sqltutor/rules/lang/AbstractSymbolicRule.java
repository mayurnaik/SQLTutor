package edu.gatech.sqltutor.rules.lang;

import java.util.Collections;
import java.util.List;

import org.deri.iris.api.basics.IRule;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.MetaruleUtils;
import edu.gatech.sqltutor.rules.SymbolicState;

public abstract class AbstractSymbolicRule implements ISymbolicTranslationRule {
	/** The symbolic state set during application. */
	protected SymbolicState state;
	
	/** The rule's precedence. */
	protected int precedence = DefaultPrecedence.FRAGMENT_REWRITE;
	
	protected int phases = ITranslationRule.ALL_PHASES;

	public AbstractSymbolicRule() {
	}
	
	public AbstractSymbolicRule(int precedence) {
		this.precedence = precedence;
	}
	
	@Override
	public String getRuleId() {
		return MetaruleUtils.getDefaultRuleId(this.getClass());
	}
	
	@Override
	public int getPrecedence() {
		return precedence;
	}
	
	@Override
	public int getPhases() {
		return phases;
	}
	
	@Override
	public void setPhases(int phases) {
		if( phases == ITranslationRule.PHASE_USE_DEFAULT )
			this.phases = getDefaultPhases();
		else
			this.phases = phases;
	}
	
	protected int getDefaultPhases() { return ITranslationRule.ALL_PHASES; }
	
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
