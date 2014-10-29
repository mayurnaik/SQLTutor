/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package edu.gatech.sqltutor.rules.symbolic.tokens;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.google.common.base.Joiner;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.util.SymbolicTokenChildList;

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
	@Override
	public void addChild(ISymbolicToken token) {
		if( token == null ) throw new NullPointerException("token is null");
		acceptOrThrow(token);
		children.add(token);
		token.setParent(this);
	}
	
	@Override
	public boolean removeChild(ISymbolicToken token) {
		if( token == null ) throw new NullPointerException("token is null");
		boolean result = children.remove(token);
		if( result )
			token.setParent(null);
		return result;
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
				token.setParent(null);
				replacement.setParent(this);
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
			this.addChild(token);
		}
	}
	
	/** Returns the child tokens, which are modifiable. */
	@Override
	public List<ISymbolicToken> getChildren() {
		return children;
	}
	
	/** Instantiate the initial child container. */
	protected List<ISymbolicToken> makeChildContainer() {
		return new SymbolicTokenChildList(this, new ArrayList<ISymbolicToken>(1));
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
		StringBuilder b = new StringBuilder("{");
		addPropertiesString(addTypeAndTag(b)).append(": ");
		Joiner.on(", ").appendTo(b, children);
		return b.append('}').toString();
	}
}
