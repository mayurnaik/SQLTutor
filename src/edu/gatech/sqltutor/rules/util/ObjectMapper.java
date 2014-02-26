package edu.gatech.sqltutor.rules.util;

import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.concrete.IIntegerTerm;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import edu.gatech.sqltutor.SQLTutorException;

/**
 * Base class for object mappers that handles everything except 
 * the {@link IObjectMapper#mapObjects(Object)} method.
 *
 * @param <T> the type of object being mapped
 */
public abstract class ObjectMapper<T> implements IObjectMapper<T> {
	protected int nextId = 0;
	protected BiMap<Integer, T> objectIds = HashBiMap.create();
	
	public ObjectMapper() {}
	
	// this is the only method subclasses need to implement
	@Override
	public abstract void mapObjects(T root);
	
	@Override
	public Integer getObjectId(T obj) {
		if( obj == null ) throw new NullPointerException("obj is null");
		Integer id = objectIds.inverse().get(obj);
		if( id == null )
			throw new SQLTutorException("No id mapped to object: " + objectToString(obj));
		return id;
	}
	
	@Override
	public T getMappedObject(Integer id) {
		if( id == null ) throw new NullPointerException("id is null");
		T node = objectIds.get(id);
		if( node == null )
			throw new SQLTutorException("No object with id: " + id);
		return node;
	}
	
	@Override
	public T getMappedObject(ITerm id) {
		try {
			return getMappedObject(((IIntegerTerm)id).getValue().intValueExact());
		} catch ( ClassCastException e ) {
			throw new SQLTutorException("Term is not an integer.", e);
		} catch( ArithmeticException e ) {
			throw new SQLTutorException("Term is not an integer or is too large.", e);
		}
	}
	
	@Override
	public void mapObject(T obj) {
		if( obj == null ) throw new NullPointerException("obj is null");
		Integer id = objectIds.inverse().get(obj);
		if( id == null )
			objectIds.put(nextId++, obj);
	}
	
	@Override
	public void clearMap() {
		nextId = 0;
		objectIds.clear();
	}
	
	@Override
	public int size() {
		return objectIds.size();
	}
	
	/**
	 * Convert object to string.  Subclasses may override for better string 
	 * formatting.
	 * @param obj the object to convert
	 * @return the string form
	 */
	protected String objectToString(T obj) {
		if( obj == null ) return "null";
		return obj.toString();
	}
}
