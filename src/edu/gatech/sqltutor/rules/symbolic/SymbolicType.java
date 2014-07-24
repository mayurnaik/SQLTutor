package edu.gatech.sqltutor.rules.symbolic;

/**
 * The types of symbolic language fragments.
 */
public enum SymbolicType {
	ROOT,
	SELECT,
	LITERAL,
	ATTRIBUTE,
	ALL_ATTRIBUTES,
	ATTRIBUTE_LIST,
	TABLE_ENTITY,
	TABLE_ENTITY_REF,
	WHERE,
	AND,
	OR,
	SEQUENCE,
	BINARY_COMPARISON,
	NUMBER,
	BETWEEN,
	SQL_AST,
	IN_RELATIONSHIP;
}
