package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.symbolic.ValueType;

/**
 * Symbolic tokens that have a value type associated.
 * @see ValueType
 */
public interface IHasValueType {
	public ValueType getValueType();
	public void setValueType(ValueType valueType);
}
