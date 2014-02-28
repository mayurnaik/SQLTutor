package edu.gatech.sqltutor.rules.lang;

import java.util.Collections;
import java.util.List;

import org.deri.iris.api.basics.IRule;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.SymbolicState;

public abstract class AbstractSymbolicRule implements ISymbolicTranslationRule {
	protected SymbolicState state;

	public AbstractSymbolicRule() {
	}
	
	@Override
	public int getPrecedence() {
		return DefaultPrecedence.FRAGMENT_REWRITE;
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
