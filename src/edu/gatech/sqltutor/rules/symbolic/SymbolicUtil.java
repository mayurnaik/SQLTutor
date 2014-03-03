package edu.gatech.sqltutor.rules.symbolic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;

public class SymbolicUtil {
	/**
	 * Attempt to copy a token using its copy constructor.
	 * 
	 * <p>The copy constructor must either take the type 
	 * of <code>token</code> or take a generic <code>ISymbolicToken</code>.</p>
	 * 
	 * @param token the token to copy
	 * @return the copy
	 * @throws SymbolicException if the token type has no copy constructor
	 */
	public static ISymbolicToken copyToken(ISymbolicToken token) {
		if( token == null ) throw new NullPointerException("token is null");
		Class<? extends ISymbolicToken> tokenType = token.getClass();
		Constructor<? extends ISymbolicToken> constructor = null;
		try {
			constructor = tokenType.getConstructor(tokenType);
		} catch( NoSuchMethodException e ) {
			try {
				constructor = tokenType.getConstructor(ISymbolicToken.class);
			} catch( NoSuchMethodException e2 ) {
				throw new SymbolicException("Token type has no copy constructor: " + tokenType.getName());
			}
		}
		
		ISymbolicToken copy = null;
		try {
			copy = constructor.newInstance(token);
		} catch( InstantiationException e ) {
			throw new SymbolicException(e);
		} catch( IllegalAccessException e ) {
			throw new SymbolicException(e);
		} catch( InvocationTargetException e ) {
			throw new SymbolicException(e);
		}
		
		return copy;
	}
	
	/**
	 * Replace a child token with another token, in the same position.
	 * 
	 * @param parent the parent token
	 * @param child  the child token
	 * @param replacement the token to replace <code>child</code> with
	 * @return <code>true</code> if the child was found and replaced, <code>false</code> otherwise
	 */
	public static boolean replaceChild(ISymbolicToken parent, ISymbolicToken child, ISymbolicToken replacement) {
		List<ISymbolicToken> children = parent.getChildren();
		for( int i = 0, ilen = children.size(); i < ilen; ++i ) {
			if( children.get(i).equals(child) ) {
				children.set(i, replacement);
				return true;
			}
		}
		return false;
	}
}
