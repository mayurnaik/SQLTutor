package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class AndToken extends ChildContainerToken implements ISymbolicToken {
	public AndToken(AndToken token) {
		super(token);
	}
	
	public AndToken() {
		super(PartOfSpeech.NOUN_PHRASE);
	}
	
	public AndToken(PartOfSpeech pos) {
		super(pos);
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.AND;
	}
}
