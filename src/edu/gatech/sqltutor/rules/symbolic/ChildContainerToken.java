package edu.gatech.sqltutor.rules.symbolic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import com.google.common.base.Joiner;

import edu.gatech.sqltutor.SQLTutorException;

/**
 * A symbolic token that accepts children.
 */
public abstract class ChildContainerToken 
		extends AbstractSymbolicToken implements ISymbolicToken {
	
	/** The child tokens, in order. */
	protected List<ISymbolicToken> children = makeChildContainer();
	
	public ChildContainerToken(ISymbolicToken token) {
		super(token);
		for( ISymbolicToken child: token.getChildren() )
			addChild(SymbolicUtil.copyToken(child));
	}
	
	public ChildContainerToken(PartOfSpeech pos) {
		super(pos);
	}
	
	/**
	 * Adds a new child.
	 * @param token the child to add
	 * @throws SQLTutorException if <code>token</code> is not an acceptable child type
	 */
	public void addChild(ISymbolicToken token) {
		if( token == null ) throw new NullPointerException("token is null");
		acceptOrThrow(token);
		children.add(token);
	}
	
	public boolean removeChild(ISymbolicToken token) {
		if( token == null ) throw new NullPointerException("token is null");
		return children.remove(token);
	}
	
	/**
	 * Replace one child token with another.
	 * @param original    the token to replace
	 * @param replacement the replacement token
	 * @return <code>true</code> if <code>original</code> was replaced
	 * @throws SQLTutorException if <code>replacement</code> is not an acceptable child type
	 */
	public boolean replaceChild(ISymbolicToken original, ISymbolicToken replacement) {
		if( original == null ) throw new NullPointerException("original is null");
		if( replacement == null ) throw new NullPointerException("replacement is null");
		acceptOrThrow(replacement);
		
		for( ListIterator<ISymbolicToken> iter = children.listIterator(); iter.hasNext(); ) {
			ISymbolicToken token = iter.next();
			if( token.equals(original) ) {
				int index = iter.previousIndex();
				children.set(index, replacement);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Set the child tokens.
	 * 
	 * @param tokens the tokens to set
	 * @throws SQLTutorException if any child is not an acceptable type,
	 *                           the child list is undefined if this is thrown
	 */
	public void setChildren(Iterable<ISymbolicToken> tokens) {
		if( tokens == null ) throw new NullPointerException("tokens is null");
		children.clear();
		for( ISymbolicToken token: tokens ) {
			acceptOrThrow(token);
			children.add(token);
		}
	}
	
	/** Returns an unmodifiable view of the child tokens. */
	@Override
	public List<ISymbolicToken> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	/** Instantiate the initial child container. */
	protected List<ISymbolicToken> makeChildContainer() {
		return new ArrayList<ISymbolicToken>(1);
	}

	/**
	 * Tests whether the child token is an acceptable type.  By default, 
	 * all types are accepted, subclasses should override to restrict this.
	 * 
	 * @param tok the child token
	 * @return <code>true</code> if the child can be accepted
	 */
	protected boolean canAcceptChild(ISymbolicToken tok) {
		return true;
	}
	
	protected void acceptOrThrow(ISymbolicToken token) {
		if( !canAcceptChild(token) )
			throw new SymbolicException(this + " does not accept token " + token);
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder('{').append(getType()).append('/')
			.append(getPartOfSpeech().getTag()).append(": ");
		Joiner.on(", ").appendTo(b, children);
		return b.append('}').toString();
	}
}
