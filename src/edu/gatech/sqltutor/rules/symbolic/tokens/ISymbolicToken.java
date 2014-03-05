package edu.gatech.sqltutor.rules.symbolic.tokens;

import java.util.List;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;


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
	 * Adds a child token.
	 * @param child the child token
	 * @throws SymbolicException if this token does not accept children or 
	 *                           does not permit the type of token <code>child</code> is 
	 */
	public void addChild(ISymbolicToken child);
	
	/**
	 * Returns the part of speech associated with this fragment, 
	 * or <code>null</code> if there is not a single part of speech.
	 */
	public PartOfSpeech getPartOfSpeech();
	
	/** Returns the type of this token. */
	public SymbolicType getType();
}
