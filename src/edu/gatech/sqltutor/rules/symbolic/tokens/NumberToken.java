package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class NumberToken extends AbstractSymbolicToken implements ISymbolicToken {
	private Number number;
	private NumericType numericType = NumericType.GENERAL;
	
	public static enum NumericType {
		GENERAL,
		MONEY;
		// TODO others?
	}

	public NumberToken(NumberToken toCopy) {
		super(toCopy);
		this.number = toCopy.number;
	}

	public NumberToken(Number number) {
		this(number, PartOfSpeech.CARDINAL_NUMBER);
	}
	
	public NumberToken(Number number, PartOfSpeech pos) {
		super(pos);
		this.number = number;
	}
	
	public NumericType getNumericType() {
		return numericType;
	}
	
	public void setNumericType(NumericType numericType) {
		if( numericType == null ) throw new NullPointerException("numericType is null");
		this.numericType = numericType;
	}
	
	public void setNumber(Number number) {
		this.number = number;
	}
	
	public Number getNumber() {
		return number;
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.NUMBER;
	}
	
	@Override
	public String toString() {
		return "{" + typeAndTag() + " number=" + number + ", type=" + numericType + "}";
	}
}
