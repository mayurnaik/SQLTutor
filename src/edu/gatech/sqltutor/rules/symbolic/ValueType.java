package edu.gatech.sqltutor.rules.symbolic;

/**
 * Storage and semantic value types.
 */
public enum ValueType {
	/** General strings. */
	STRING,
	/** Dates and/or times. */
	DATETIME,
	/** Generic numbers. */
	NUMBER,
	/** US Dollars */
	DOLLARS,
	
	UNKNOWN;
}
