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
