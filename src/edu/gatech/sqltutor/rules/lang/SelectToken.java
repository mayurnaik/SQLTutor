package edu.gatech.sqltutor.rules.lang;

import java.util.Collections;
import java.util.List;

/** The act of selecting. */
public class SelectToken implements ISymbolicToken {
	@Override
	public List<ISymbolicToken> getChildren() {
		return Collections.emptyList();
	}
	
	@Override
	public PartOfSpeech getPartOfSpeech() {
		return PartOfSpeech.VERB_BASE_FORM;
	}
	
	@Override
	public SymbolicType getType() {
		return SymbolicType.SELECT;
	}
	
	@Override
	public String toString() {
		return "{select}";
	}
}
