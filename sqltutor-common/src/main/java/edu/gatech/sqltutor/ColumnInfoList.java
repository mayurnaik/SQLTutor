package edu.gatech.sqltutor;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ForwardingList;

/**
 * A list of column metadata with a few extra conveniences.
 */
public class ColumnInfoList extends ForwardingList<ColumnInfo> implements List<ColumnInfo> {
	protected List<ColumnInfo> delegate;

	public ColumnInfoList() {
		delegate = new ArrayList<ColumnInfo>();
	}
	
	protected ColumnInfoList(List<ColumnInfo> delegate) {
		if( delegate == null ) throw new NullPointerException("delegate is null");
		this.delegate = delegate;
	}

	@Override
	protected List<ColumnInfo> delegate() {
		return delegate;
	}
	
	/**
	 * Returns whether there is a column with a given name (case-sensitive).
	 * @param name the name to check
	 * @return <code>true</code> if <code>getByName(name)</code> would return a non-null object
	 */
	public boolean containsName(String name) { return containsName(name, true); }
	
	/**
	 * Returns whether there is a column with a given name.
	 * @param name the name to check
	 * @param isCaseSensitive if the match should be case-sensitive or not
	 * @return <code>true</code> if <code>getByName(name, isCaseSensitive)</code> would return a non-null object
	 */
	public boolean containsName(String name, boolean isCaseSensitive) {
		return getByName(name, isCaseSensitive) != null;
	}
	
	/**
	 * Returns the (first) column with a given name (case-sensitive).
	 * @param name the name to check
	 * @return the matching <code>ColumnInfo</code> or <code>null</code> if there is no match
	 */
	public ColumnInfo getByName(String name) { return getByName(name, true); }
	
	/**
	 * Returns the (first) column with a given name (case-sensitive).
	 * @param name the name to check
	 * @param isCaseSensitive if the match should be case-sensitive or not
	 * @return the matching <code>ColumnInfo</code> or <code>null</code> if there is no match
	 */
	public ColumnInfo getByName(String name, boolean isCaseSensitive) {
		if( isCaseSensitive ) {
			for( ColumnInfo cinfo: delegate )
				if( name.equals(cinfo.getName()) )
					return cinfo;
		} else {
			for( ColumnInfo cinfo: delegate )
				if( name.equalsIgnoreCase(cinfo.getName()) )
					return cinfo;
		}
		return null;
	}
}
