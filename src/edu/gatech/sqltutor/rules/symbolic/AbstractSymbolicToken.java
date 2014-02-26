package edu.gatech.sqltutor.rules.symbolic;

import java.util.Collections;
import java.util.List;

public abstract class AbstractSymbolicToken implements ISymbolicToken {
	protected PartOfSpeech partOfSpeech;
	
	protected AbstractSymbolicToken(PartOfSpeech pos) {
		setPartOfSpeech(pos);
	}
	
	@Override
	public PartOfSpeech getPartOfSpeech() {
		return partOfSpeech;
	}
	
	protected void setPartOfSpeech(PartOfSpeech partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}
	
	@Override
	public List<ISymbolicToken> getChildren() {
		return Collections.emptyList();
	}
	
	protected String typeAndTag() { return getType() + "/" + getPartOfSpeech().getTag(); }
	
	@Override
	public String toString() {
		return "{" + typeAndTag() + "}";
	}
}
