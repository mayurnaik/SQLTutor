package edu.gatech.sqltutor.rules.lang;

import java.util.EnumSet;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.TranslationPhase;

/**
 * Convenience base class for rules with default <code>LOWERING</code> precedence and phase. 
 */
public abstract class StandardLoweringRule extends StandardSymbolicRule implements
		ITranslationRule {

	public StandardLoweringRule() {
	}

	public StandardLoweringRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.LOWERING;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}

}
