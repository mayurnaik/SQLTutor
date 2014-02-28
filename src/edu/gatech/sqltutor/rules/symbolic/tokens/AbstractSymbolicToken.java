package edu.gatech.sqltutor.rules.symbolic.tokens;

import java.util.Collections;
import java.util.List;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;

public abstract class AbstractSymbolicToken implements ISymbolicToken {
	protected PartOfSpeech partOfSpeech;
	
	protected AbstractSymbolicToken(ISymbolicToken toCopy) {
		if( toCopy == null ) throw new NullPointerException("toCopy is null");
		Class<?> thisClass = this.getClass(), thatClass = toCopy.getClass();
		if( thisClass != thatClass ) {
			throw new SymbolicException("Expected type " + thisClass.getName() 
				+ ", found type " + thatClass.getName());
		}
		this.partOfSpeech = toCopy.getPartOfSpeech();
	}
	
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
