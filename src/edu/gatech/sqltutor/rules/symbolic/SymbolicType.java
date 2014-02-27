package edu.gatech.sqltutor.rules.symbolic;

/**
 * The types of symbolic language fragments.
 */
public enum SymbolicType {
	ROOT,
	SELECT,
	LITERAL,
	LITERALS,
	ATTRIBUTE,
	ATTRIBUTE_LIST,
	TABLE_ENTITY,
	WHERE,
	AND,
	OR,
	SEQUENCE;
}
