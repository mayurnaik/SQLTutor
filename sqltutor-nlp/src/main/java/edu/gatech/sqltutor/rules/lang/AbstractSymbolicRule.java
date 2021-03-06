/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
	protected int precedence = getDefaultPrecedence();
	
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
	
	/**
	 * Get the phases this rule participates in by default.
	 * This is all phases unless overridden.
	 * @return the default phases
	 */
	protected EnumSet<TranslationPhase> getDefaultPhases() { 
		return EnumSet.allOf(TranslationPhase.class); 
	}
	
	protected int getDefaultPrecedence() {
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
