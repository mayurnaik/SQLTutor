package edu.gatech.sqltutor.rules.lang;

import java.util.Collections;
import java.util.List;

import org.deri.iris.api.basics.IRule;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISQLTranslationRule;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.MetaruleUtils;
import edu.gatech.sqltutor.rules.SQLState;

public abstract class AbstractSQLRule implements ISQLTranslationRule {
	protected SQLState state;

	public AbstractSQLRule() {
	}
	
	@Override
	public String getRuleId() {
		return MetaruleUtils.getDefaultRuleId(this.getClass());
	}
	
	@Override
	public int getPrecedence() {
		return DefaultPrecedence.DESTRUCTIVE_UPDATE;
	}
	
	@Override
	public int getType() {
		return ITranslationRule.TYPE_SQL;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return Collections.emptyList();
	}
	
	public SQLState getState() {
		return state;
	}
}
