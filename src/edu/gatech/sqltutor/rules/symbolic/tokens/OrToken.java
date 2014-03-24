package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class OrToken extends ChildContainerToken implements ISymbolicToken {
	public OrToken(OrToken token) {
		super(token);
	}
	
	public OrToken() {
		super(PartOfSpeech.NOUN_PHRASE);
	}
	
	public OrToken(PartOfSpeech pos) {
		super(pos);
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.OR;
	}
}
