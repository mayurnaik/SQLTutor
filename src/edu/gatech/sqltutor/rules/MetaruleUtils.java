package edu.gatech.sqltutor.rules;

public class MetaruleUtils {
	/**
	 * Returns the default rule id format for the given class.
	 * @param ruleClass the rule class
	 * @return the default rule id
	 */
	public static String getDefaultRuleId(Class<?> ruleClass) {
		if( ruleClass == null ) throw new NullPointerException("ruleClass is null");
		return ruleClass.getSimpleName();
	}
}
