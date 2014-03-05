package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

/** <code>WHERE</code>-clause action, e.g. "where", "whose", "such that" ... */
public class WhereToken extends AbstractSymbolicToken implements ISymbolicToken {
	public WhereToken() {
		super(PartOfSpeech.WH_ADVERB);
	}
	
	public WhereToken(WhereToken toCopy) {
		super(toCopy);
	}

	public WhereToken(PartOfSpeech pos) {
		super(pos);
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.WHERE;
	}
}
