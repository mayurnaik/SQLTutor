package edu.gatech.sqltutor.rules.lang;

import java.util.List;

/**
 * A symbolic language fragment.
 */
public interface ISymbolicToken {
	/**
	 * Returns any child tokens of this fragment.  
	 * Fragments that do not support children should return an empty list 
	 * rather than <code>null</code>.
	 * 
	 * @return the possibly empty list of child tokens
	 */
	public List<ISymbolicToken> getChildren();
	
	/**
	 * Returns the part of speech associated with this fragment, 
	 * or <code>null</code> if there is not a single part of speech.
	 */
	public PartOfSpeech getPartOfSpeech();
	
	/** Returns the type of this token. */
	public SymbolicType getType();
}
