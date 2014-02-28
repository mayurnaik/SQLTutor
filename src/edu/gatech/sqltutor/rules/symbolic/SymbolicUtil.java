package edu.gatech.sqltutor.rules.symbolic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
}
