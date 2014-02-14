package edu.gatech.sqltutor.rules.datalog.iris;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.predicate;

import org.deri.iris.api.basics.IPredicate;

public class LearnedPredicates {
	/** 
	 * <code>(?tref:int,?label:string,?source:string)</code>
	 * <p>Table <code>?tref</code> has label <code>?label</code>, learned 
	 * from source <code>?source</code>.</p>
	 */
	public static final IPredicate tableLabel = predicate("lrnTableLabel", 3);
}
