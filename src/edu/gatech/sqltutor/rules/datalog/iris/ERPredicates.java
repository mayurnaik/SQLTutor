/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
	
	/** <code>(?entity:string,?label:string)</code> => ?label is the user-specified singular label for ?entity */
	public static final IPredicate erEntityLabelSingular = predicate("erEntityLabelSingular", 2);
	
	/** <code>(?entity:string,?label:string)</code> => ?label is the user-specified plural label for ?entity */
	public static final IPredicate erEntityLabelPlural = predicate("erEntityLabelPlural", 2); 
	
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

	
	/** <code>(?entity:string,?attr:string,?label:string)</code> => 
	 * ?label is the user-specified singular label for ?entity.?attribute */
	public static final IPredicate erAttributeLabelSingular = predicate("erAttributeLabelSingular", 3);
	
	/** <code>(?entity:string,?attr:string,?label:string)</code> => 
	 * ?label is the user-specified plural label for ?entity.?attribute */
	public static final IPredicate erAttributeLabelPlural = predicate("erAttributeLabelPlural", 3); 
	
	/** <code>(?relationship:string,?alternate:string)</code> => 
	 * ?label is the user-specified alternate singular name for ?relationship */
	public static final IPredicate erRelationshipAlternateSingular = predicate("erRelationshipAlternateSingular", 2);
	
	/** <code>(?relationship:string,?alternate:string)</code> => 
	 * ?label is the user-specified alternate plural name for ?relationship */
	public static final IPredicate erRelationshipAlternatePlural = predicate("erRelationshipAlternatePlural", 2);
	
	/** <code>(?relationship:string,?negated:string)</code> => 
	 * ?label is the user-specified negated singular name for ?relationship */
	public static final IPredicate erRelationshipNegatedSingular = predicate("erRelationshipNegatedSingular", 2);
	
	/** <code>(?relationship:string,?negated:string)</code> => 
	 * ?label is the user-specified negated plural name for ?relationship */
	public static final IPredicate erRelationshipNegatedPlural = predicate("erRelationshipNegatedPlural", 2);
	
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
