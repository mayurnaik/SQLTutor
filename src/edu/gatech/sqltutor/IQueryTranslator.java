package edu.gatech.sqltutor;

import java.util.List;

import objects.DatabaseTable;

/**
 * Query translators produce natural language 
 * descriptions from SQL queries.
 * <p>
 * Query translators must have the query set before 
 * calling <code>getTranslation()</code>.  Schema 
 * meta data is optional, but may cause some translations 
 * to fail when missing that would succeed otherwise.
 * </p>
 */
public interface IQueryTranslator {
	/** Sets the query to be translated. */
	public void setQuery(String sql);
	/** Returns the query to be translated. */
	public String getQuery();
	
	// FIXME list is not a good type, harder to refactor
	public void setSchemaMetaData(List<DatabaseTable> tables);
	public List<DatabaseTable> getSchemaMetaData();
	
	// FIXME return type?
	/**
	 * Returns an object identifying the type of this translator
	 * for provenance tracking.
	 * @return the translator type
	 */
	public Object getTranslatorType();
	
	/**
	 * Returns the resulting translation.
	 * @return the translation
	 */
	public String getTranslation();
}
