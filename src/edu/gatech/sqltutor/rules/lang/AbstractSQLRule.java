package edu.gatech.sqltutor.rules.lang;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISQLTranslationRule;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.SQLState;

public abstract class AbstractSQLRule implements ISQLTranslationRule {
	protected SQLState state;

	public AbstractSQLRule() {
	}
	
	@Override
	public int getPrecedence() {
		return DefaultPrecedence.DESTRUCTIVE_UPDATE;
	}
	
	@Override
	public int getType() {
		return ITranslationRule.TYPE_SQL;
	}
	
	public SQLState getState() {
		return state;
	}
}
