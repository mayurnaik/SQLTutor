package edu.gatech.sqltutor.rules.datalog.iris;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.predicate;

import org.deri.iris.api.basics.IPredicate;

import edu.gatech.sqltutor.rules.er.ERAttributeDataType;

public class ERPredicates {
// dynamically generated predicates 
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
	 * <code>erAttributeIsComposite(?parent:string,?attr:string)</code> =>
	 *   <code>erAttribute(?parent,?attr)</code> is a key attribute.
	 */	
	public static final IPredicate erAttributeIsComposite = predicate("erAttributeIsComposite", 2); 
	
	/**
	 * <code>erAttributeParent(?entity:string,?parent:string,?child:string)</code> =>
	 *   <code>erAttribute(?entity,?parent)</code> is the parent of <code>erAttribute(?entity,?child)</code>.
	 *   Implies <code>erAttributeIsComposite(?entity,?parent)</code>.
	 */
	public static final IPredicate erAttributeParent = predicate("erAttributeParent", 3); 
	
	/** <code>erAttributeDescribes(?ent,?attr,?type)</code> =>  
	 * the attribute can be used to describe the entity, 
	 * type indicates how, one of ('prepend', 'replace', ...?others?)
	 */
	public static final IPredicate erAttributeDescribes = predicate("erAttributeDescribes", 3);
	/** 
	 * <code>erAttributeDataType(?ent,?attr,?dataType)</code> =>  
	 * the attribute has a certain data type.
	 * @see ERAttributeDataType
	 */	
	public static final IPredicate erAttributeDataType = predicate("erAttributeDataType", 3); 
	
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
	
//
// statically defined predicates
//
	/** <code>(table:id,entity:string)</code> */
	public static final IPredicate erTableRefsEntity = predicate("erTableRefsEntity", 2); 
	public static final IPredicate erEntityOrRelationship = predicate("erEntityOrRelationship", 1); 
	public static final IPredicate erFKJoin = predicate("erFKJoin", 5);
	public static final IPredicate erFKJoinSides = predicate("erFKJoinSides", 3);
	public static final IPredicate erLookupJoinKeyPair = predicate("erLookupJoinKeyPair", 6); 
}
