package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

/**
 * Generic sequence of tokens.
 */
public class SequenceToken extends ChildContainerToken implements ISymbolicToken {
	public SequenceToken(SequenceToken token) {
		super(token);
	}

	public SequenceToken(PartOfSpeech pos) {
		super(pos);
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.SEQUENCE;
	}
}
