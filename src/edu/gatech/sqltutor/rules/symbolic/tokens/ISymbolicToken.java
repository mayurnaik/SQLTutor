package edu.gatech.sqltutor.rules.symbolic.tokens;

import java.util.List;
import java.util.Set;

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
	 * Removes a child token.
	 * @param child the child to remove
	 * @return whether the child was removed
	 */
	public boolean removeChild(ISymbolicToken child);
	
	/**
	 * Returns the token's parent.
	 * @return the token's parent
	 */
	public ISymbolicToken getParent();
	
	/**
	 * Sets the token's parent.
	 * @param parent the token's parent
	 */
	public void setParent(ISymbolicToken parent);
	
	/**
	 * Returns the (modifiable) provenance set of this token.
	 * The set may be empty but should not be <code>null</code>.
	 * @return the provenance set
	 */
	public Set<String> getProvenance();
	
	/**
	 * Returns the part of speech associated with this fragment, 
	 * or <code>null</code> if there is not a single part of speech.
	 */
	public PartOfSpeech getPartOfSpeech();
	
	/** Returns the type of this token. */
	public SymbolicType getType();
}
