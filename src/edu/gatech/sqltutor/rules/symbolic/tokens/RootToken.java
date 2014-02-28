package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

/** The distinguished root token. */
public class RootToken extends ChildContainerToken implements ISymbolicToken {
	public RootToken(RootToken token) {
		super(token);
	}
	
	public RootToken() {
		super(PartOfSpeech.SENTENCE);
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.ROOT;
	}
}
