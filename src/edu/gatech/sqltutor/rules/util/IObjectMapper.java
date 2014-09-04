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
