package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class NumberToken extends AbstractSymbolicToken implements ISymbolicToken {
	private Number number;

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

	@Override
	public SymbolicType getType() {
		return SymbolicType.NUMBER;
	}
	
	@Override
	public String toString() {
		return "{" + typeAndTag() + " number=" + number + "}";
	}
}
