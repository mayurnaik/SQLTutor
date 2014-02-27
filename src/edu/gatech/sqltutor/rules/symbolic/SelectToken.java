package edu.gatech.sqltutor.rules.symbolic;

/** The act of selecting. */
public class SelectToken extends AbstractSymbolicToken implements ISymbolicToken {
	public SelectToken(SelectToken token) { super(token); }
	
	public SelectToken() { super(PartOfSpeech.VERB_BASE_FORM); }
	
	@Override
	public SymbolicType getType() {
		return SymbolicType.SELECT;
	}
}
