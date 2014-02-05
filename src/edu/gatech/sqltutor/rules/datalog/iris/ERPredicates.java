package edu.gatech.sqltutor.rules.datalog.iris;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.predicate;

import org.deri.iris.api.basics.IPredicate;

public class ERPredicates {
	/** <code>erEntity(?ent:string)</code> => <code>ent</code> names an entity. */
	public static final IPredicate erEntity = predicate("erEntity", 1);

	/** <code>erRelationship(?ent:string)</code> => <code>ent</code> names a relationship. */
	public static final IPredicate erRelationship = predicate("erRelationship", 1);
	
	/** 
	 * <code>erAttribute(?parent:string,?attr:string)</code> =>
	 *   <code>attr</code> is an attribute with parent entity or relationship
	 *   <code>parent</code>
	 */
	public static final IPredicate erAttribute = predicate("erAttribute", 2);
	
	public static final IPredicate erEntityType = predicate("erEntityType", 2);
	
	/**
	 * <code>erAttributeIsKey(?parent:string,?attr:string)</code> =>
	 *   <code>erAttribute(?parent,?attr)</code> is a key attribute.
	 */
	public static final IPredicate erAttributeIsKey = predicate("erAttributeIsKey", 2);
	
	/**
	 * <code>erRelationshipEdgeEntity(?rel:string,?n:int,?ent:string)</code> =><br />
	 *   The <code>n</code>th (0-based) edge of relationship <code>rel</code> refers 
	 *   to entity <code>ent</code>.
	 */
	public static final IPredicate erRelationshipEdgeEntity = predicate("erRelationshipEdgeEntity", 3);
	/** @see #erRelationshipEdgeEntity */
	public static final IPredicate erRelationshipEdgeLabel = predicate("erRelationshipEdgeLabel", 3);
	/** @see #erRelationshipEdgeEntity */
	public static final IPredicate erRelationshipEdgeCardinality = predicate("erRelationshipEdgeCardinality", 3);
	
	/** <code>(?parent:string,?attr:string,?table:string,?column:string)</code>. */
	public static final IPredicate erAttributeMapsTo = predicate("erAttributeMapsTo", 4);
	
	public static final IPredicate erRelationshipJoinType = predicate("erRelationshipJoinType", 2);
	
	/** <code>(?rel:string,?pos:int,?table:string,?col:string)</code> */
	public static final IPredicate erJoinPK = predicate("erJoinPK", 4);
	/** <code>(?rel:string,?pos:int, ?table:string,?col:string)</code> */
	public static final IPredicate erJoinFK = predicate("erJoinFK", 4);
	
	
}
