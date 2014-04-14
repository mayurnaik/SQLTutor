package edu.gatech.sqltutor.rules.datalog.iris;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.predicate;

import org.deri.iris.api.basics.IPredicate;

public class LearnedPredicates {
	/** 
	 * <code>(?tref:int,?label:string,?plural:string,?source:string)</code>
	 * <p>Table <code>?tref</code> has labels <code>?label</code> (singular) 
	 * and <code>?plural</code> (plural), from source(s) <code>?source</code>.</p>
	 */
	public static final IPredicate tableLabel = predicate("lrnTableLabel", 4);
	
	/**
	 * <code>(?tref:int,?relationship:string,?pos:int,?source:string)</code>
	 * <p>Table <code>?tref</code> participates in relationship <code>?rel</code> at 
	 * the <code>?pos</code>'th index (i.e. 0=left, 1=right), 
	 * learned from source <code>?source</code>.</p>
	 */
	public static final IPredicate tableInRelationship = predicate("lrnTableInRelationship", 4); 
	
	/**
	 * <code>(?entity:string,?attribute:string,?label:string,?plural:string?source:string)</code>
	 * <p>Attribute <code>?entity.?attribute</code>  has labels <code>?label</code> (singular) 
	 * and <code>?plural</code> (plural), from source(s) <code>?source</code>.</p>
	 */
	public static final IPredicate attributeLabel = predicate("lrnAttributeLabel", 5);
}
