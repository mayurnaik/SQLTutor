package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class BetweenToken extends ChildContainerToken {
	public BetweenToken(BetweenToken token) {
		super(token);
	}

	public BetweenToken(PartOfSpeech pos) {
		super(pos);
	}
	
	public BetweenToken() {
		super(PartOfSpeech.QUANTIFIER_PHRASE);
	}
	
	public ISymbolicToken getObjectToken() {
		if( children.size() < 1 )
			return null;
		return children.get(0); 
	}
	
	public ISymbolicToken getLowerBoundToken() {
		if( children.size() < 2 )
			return null;
		return children.get(1);
	}
	
	public ISymbolicToken getUpperBoundToken() {
		if( children.size() < 3 )
			return null;
		return children.get(2);
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.BETWEEN;
	}
	
	@Override
	protected void acceptOrThrow(ISymbolicToken token) {
		int size = getChildren().size();
		if( size >= 3 )
			throw new SymbolicException("Only three children are allowed.");
		SymbolicType childType = token.getType();
		if( size == 0 )
			return; // FIXME could restrict to attributes, but this is more flexible
		if( childType != SymbolicType.NUMBER )
			throw new SymbolicException("Second and third children must be numeric, not: " + token);
	}
}
