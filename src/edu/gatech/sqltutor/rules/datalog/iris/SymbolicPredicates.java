package edu.gatech.sqltutor.rules.datalog.iris;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.predicate;

import org.deri.iris.api.basics.IPredicate;

/** Static predicates referring to symbolic sentence structure. */
public class SymbolicPredicates {
	/** 
	 * <code>(?parent:int,?child:int,?pos:int)</code> =>
	 * <code>?child</code> is the <code>?pos</code>'th child of <code>?parent</code> 
	 */
	public static final IPredicate parentOf = predicate("symParentOf", 3);
	
	/** <code>(?token:int,?type:string)</code> => <code>?token</code> is of type <code>?type</code> */
	public static final IPredicate type = predicate("symType", 2);
	
	/** <code>(?token:int,?tag:string)</code> => <code>?token</code> has part of speech tag <code>?tag</code> */
	public static final IPredicate partOfSpeech = predicate("symPartOfSpeech", 2); 
	
	/** <code>(?token:int,?entity:string,?attribute:string)</code> */
	public static final IPredicate refsAttribute = predicate("symRefsAttribute", 3);
	
	/** <code>(?token:int,?table:int)</code> */
	public static final IPredicate refsTable = predicate("symRefsTable", 2); 
	
	// for debugging, id => token.toString()
	public static final IPredicate debugString = predicate("symDebugString", 2); 
}
