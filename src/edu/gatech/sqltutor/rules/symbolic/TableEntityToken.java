package edu.gatech.sqltutor.rules.symbolic;

public class TableEntityToken extends AbstractSymbolicToken implements ISymbolicToken {
	public TableEntityToken() {
		super(PartOfSpeech.NOUN_SINGULAR_OR_MASS);
	}
	
	public TableEntityToken(PartOfSpeech pos) {
		super(pos);
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.TABLE_ENTITY;
	}
	
	@Override
	protected void setPartOfSpeech(PartOfSpeech partOfSpeech) {
		switch( partOfSpeech ) {
			case NOUN_SINGULAR_OR_MASS:
			case NOUN_PLURAL:
			case NOUN_PHRASE:
				break;
			default:
				throw new SymbolicException("Table entities must be nouns or noun phrases: " + partOfSpeech);
		}
		super.setPartOfSpeech(partOfSpeech);
	}
}
