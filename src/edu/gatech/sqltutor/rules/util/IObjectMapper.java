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
package edu.gatech.sqltutor.rules.util;

import org.deri.iris.api.terms.ITerm;

import edu.gatech.sqltutor.SQLTutorException;

/** 
 * Mapper that assigns integer ids to some type
 * @param <T> the type of object being mapped
 */
public interface IObjectMapper<T> {
	/**
	 * Returns the id assigned to an object.
	 * @param obj the mapped object
	 * @return the id assigned to the object
	 * @throws SQLTutorException if the object is not mapped to an id
	 */
	public Integer getObjectId(T obj);
	
	/**
	 * Returns the object mapped to the given id.
	 * @param id the object id
	 * @return the object
	 * @throws SQLTutorException if no object is mapped to <code>id</code>
	 */
	public T getMappedObject(Integer id);
	
	/**
	 * Return the object mapped to the id given by a datalog term.
	 * @param id the id, which should be an integer term
	 * @return the mapped object
	 * @throws SQLTutorException if no object is mapped to <code>id</code>
	 */
	public T getMappedObject(ITerm id);
	
	/**
	 * Assign an id to <code>obj</code>.  If obj is already mapped, 
	 * the original id should be preserved.
	 * @param obj the object to map
	 * @return the object's id
	 */
	public Integer mapObject(T obj);
	
	/**
	 * Maps a set of objects rooted at <code>root</code>.  
	 * The traversal mechanism is dependent on the type <code>T</code>.
	 * @param root the root object in a hierarchy
	 */
	public void mapObjects(T root);
	
	/** Clear out all mapping information. */
	public void clearMap();
	
	/** Returns the size of the map. */
	public int size();
}
