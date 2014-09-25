package edu.gatech.sqltutor.rules.symbolic.analysis;

import java.util.EnumSet;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.storage.IRelation;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;

public abstract class StandardAnalysisRule extends StandardSymbolicRule
		implements ITranslationRule {

	public StandardAnalysisRule() {
	}

	public StandardAnalysisRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.FRAGMENT_ENHANCEMENT;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.SQL_ANALYSIS);
	}
}
