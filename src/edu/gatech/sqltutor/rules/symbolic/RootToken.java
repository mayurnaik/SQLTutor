package edu.gatech.sqltutor.rules.symbolic;

/** The distinguished root token. */
public class RootToken extends ChildContainerToken implements ISymbolicToken {
	public RootToken() {
		super(PartOfSpeech.SENTENCE);
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.ROOT;
	}
}
