package edu.gatech.sqltutor.rules;

import java.util.List;

import org.deri.iris.api.basics.IRule;


/**
 * Rule that matches on portions of an 
 * SQL query and produces some annotation 
 * or output for a natural language description.
 */
public interface ITranslationRule {
	public static final int TYPE_SQL = 1;
	public static final int TYPE_SYMBOLIC = 2;

	public static final int PHASE_SQL_ANALYSIS = 1 << 0;
	public static final int PHASE_TRANSFORMATION = 1 << 1;
	public static final int PHASE_LOWERING = 1 << 2;
	// convienence
	public static final int PHASE_USE_DEFAULT = -1;
	public static final int ALL_PHASES = 
		PHASE_SQL_ANALYSIS | PHASE_TRANSFORMATION | PHASE_LOWERING;
	
	public boolean apply(SymbolicState state);
	
	/** 
	 * Returns the precedence of this rule.  
	 * Rules should be applied in order of precedence.
	 * @return the precedence.
	 */
	public int getPrecedence();
	
	/**
	 * Returns the phases the rule should operate in.
	 * @return the phases as a bit vector
	 */
	public int getPhases();
	
	/**
	 * Overrides the default phases this rule should operate in.
	 * @param phase
	 */
	public void setPhases(int phase);
	
	/**
	 * Returns the type of this translation rule, for determining 
	 * sub-interfaces.
	 * 
	 * @return the translation rule type
	 */
	public int getType();
	
	/**
	 * Returns a unique id for this rule.
	 * @return the rule id
	 */
	public String getRuleId();
	
	/**
	 * Returns a list of static datalog rules used by this meta-rule.
	 * @return the static rules, which may be empty but should not be <code>null</code>
	 */
	public List<IRule> getDatalogRules();
}
