package edu.gatech.sqltutor;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/** Static utility functions. */
public class Utils {
	/** 
	 * Try to close a resource, suppressing any exception.
	 * 
	 * @param closeable the closeable or <code>null</code>
	 * @return the exception thrown or <code>null</code>
	 */
	public static IOException tryClose(Closeable closeable) {
		try {
			if( closeable != null )
				closeable.close();
			return null;
		} catch( IOException e ) {
			return e;
		}
	}
	
	/**
	 * Try to close a resource, suppressing any exception.
	 * <p>This is done by reflection. The object must have a 
	 * <code>close()</code> method.</p>
	 * 
	 * @param obj the resource to close
	 * @return any exception
	 */
	public static Throwable tryClose(Object obj) {
		if( obj instanceof Closeable )
			return tryClose((Closeable)obj);
		if( obj == null )
			return null;
		
		try {
			obj.getClass().getMethod("close").invoke(obj);
			return null;
		} catch( InvocationTargetException e ) {
			return e.getCause();
		} catch( Exception e ) {
			return e;
		}
	}
}
