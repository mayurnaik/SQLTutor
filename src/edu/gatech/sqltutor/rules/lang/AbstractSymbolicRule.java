package edu.gatech.sqltutor.rules.lang;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.deri.iris.api.basics.IRule;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.MetaruleUtils;
import edu.gatech.sqltutor.rules.SymbolicState;
import edu.gatech.sqltutor.rules.TranslationPhase;

public abstract class AbstractSymbolicRule implements ISymbolicTranslationRule {
	/** The symbolic state set during application. */
	protected SymbolicState state;
	
	/** The rule's precedence. */
	protected int precedence = DefaultPrecedence.FRAGMENT_REWRITE;
	
	protected EnumSet<TranslationPhase> phases = getDefaultPhases();

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
	public EnumSet<TranslationPhase> getPhases() {
		return phases;
	}
	
	@Override
	public void setPhases(EnumSet<TranslationPhase> phases) {
		this.phases = phases != null ? phases : getDefaultPhases();
	}
	
	protected EnumSet<TranslationPhase> getDefaultPhases() { return EnumSet.allOf(TranslationPhase.class); }
	
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
