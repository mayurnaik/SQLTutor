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
package edu.gatech.sqltutor.rules.symbolic;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.ClassPath;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;

public class SymbolicUtil {
	private static final Logger _log = LoggerFactory.getLogger(SymbolicUtil.class);
	
	public static List<ISymbolicTranslationRule> loadSymbolicRules() {
		try {
			String pkg = SymbolicUtil.class.getPackage().getName();
			ClassPath classPath = ClassPath.from(SymbolicUtil.class.getClassLoader());
			
			List<ISymbolicTranslationRule> rules = new ArrayList<ISymbolicTranslationRule>();
			for( ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(pkg) ) {
				ISymbolicTranslationRule rule = createSymbolicRule(classInfo);
				if( rule != null )
					rules.add(rule);
			}
			
			return rules;
		} catch( IOException e ) {
			throw new SQLTutorException("Could not read classpath.");
		}
	}
	
	private static ISymbolicTranslationRule createSymbolicRule(ClassPath.ClassInfo classInfo) {
		if( !classInfo.getSimpleName().endsWith("Rule") )
			return null;
		Class<?> clazz = classInfo.load();
		if( 0 != (clazz.getModifiers() & Modifier.ABSTRACT) || !ISymbolicTranslationRule.class.isAssignableFrom(clazz) )
			return null;
		
		@SuppressWarnings("unchecked")
		Class<? extends ISymbolicTranslationRule> ruleClass = (Class<? extends ISymbolicTranslationRule>)clazz;

		try {
			return ruleClass.newInstance();
		} catch( Exception e ) {
			_log.error("Could not instantiate symbolic translation rule: " + ruleClass.getName(), e);
			return null;
		}
	}
	
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
	 * @param child  the child token
	 * @param replacement the token to replace <code>child</code> with
	 * @throws SymbolicException if replacement failed
	 */
	public static void replaceChild(ISymbolicToken child, ISymbolicToken replacement) {
		replaceChild(child.getParent(), child, replacement);
	}
	
	/**
	 * Replace a child token with another token, in the same position.
	 * 
	 * @param parent the parent token
	 * @param child  the child token
	 * @param replacement the token to replace <code>child</code> with
	 * @throws SymbolicException if replacement failed, e.g. because <code>child</code> is not a child of <code>parent</code>
	 */
	public static void replaceChild(ISymbolicToken parent, ISymbolicToken child, ISymbolicToken replacement) {
		List<ISymbolicToken> children = parent.getChildren();
		for( int i = 0, ilen = children.size(); i < ilen; ++i ) {
			ISymbolicToken token = children.get(i);
			if( token.equals(child) ) {
				token.setParent(null);
				children.set(i, replacement);
				replacement.setParent(parent);
				return;
			}
		}
		throw new SymbolicException("Failed to replace " + child + " with " + replacement + " in " + parent);
	}
	
	public static ISymbolicToken getPotentialDeterminer(ISymbolicToken token) {
		ISymbolicToken before = getPrecedingToken(token);
		if( before == null )
			return null;
		switch( before.getPartOfSpeech() ) {
		case POSSESSIVE_ENDING:
			before = getPotentialDeterminer(before);
			if( before != null )
				before = getPotentialDeterminer(before);
			return before;
		default:
			return before;
		}
	}
	
	/**
	 * Gets the token that would precede the <code>token</code> if the 
	 * tree-structure was flattened.  The returned token is always a 
	 * leaf-token in the tree structure.
	 * 
	 * @param token the token to find the predecessor of
	 * @return the predecessor or <code>null</code> if there is no predecessor
	 */
	public static ISymbolicToken getPrecedingToken(ISymbolicToken token) {
		if( token == null ) throw new NullPointerException("token is null");
		ISymbolicToken parent = token.getParent();
		if( parent == null ) return null; // the root token, has no predecessor
		
		// if we have a sibling before us, its right-most descendant
		List<ISymbolicToken> siblings = parent.getChildren();
		int idx = siblings.indexOf(token);
		if( idx > 0 )
			return getRightmostDescendant(siblings.get(idx-1));
		
		// otherwise, up one level and check
		return getPrecedingToken(parent);
	}
	
	/**
	 * Gets the token that would follow the <code>token</code> if the 
	 * tree-structure was flattened.  The returned token is always a 
	 * leaf-token in the tree structure.
	 * 
	 * @param token the token to find the predecessor of
	 * @return the predecessor or <code>null</code> if there is no predecessor
	 */
	public static ISymbolicToken getSucceedingToken(ISymbolicToken token) {
		if( token == null ) throw new NullPointerException("token is null");
		ISymbolicToken parent = token.getParent();
		if( parent == null ) return null; // the root token, has no following token
		
		// if we have a sibling after us, its left-most descendant
		List<ISymbolicToken> siblings = parent.getChildren();
		int idx = siblings.indexOf(token);
		if( idx < siblings.size() - 1 )
			return getLeftmostDescendant(siblings.get(idx+1));
		
		// otherwise, up one level and check
		return getSucceedingToken(parent);
	}
	
	/**
	 * Returns the right-most tree descendant of a token.  This may be the 
	 * token itself if it has no children.
	 * 
	 * @param token the ancestor token
	 * @return the right-most descendent 
	 */
	public static ISymbolicToken getRightmostDescendant(ISymbolicToken token) {
		if( token == null ) throw new NullPointerException("token is null");
		List<ISymbolicToken> children = token.getChildren();
		int size = children.size();
		if( size == 0 )
			return token;
		return getRightmostDescendant(children.get(size-1));
	}
	
	/**
	 * Returns the left-most tree descendant of a token.  This may be the 
	 * token itself if it has no children.
	 * 
	 * @param token the ancestor token
	 * @return the right-most descendent 
	 */
	public static ISymbolicToken getLeftmostDescendant(ISymbolicToken token) {
		if( token == null ) throw new NullPointerException("token is null");
		List<ISymbolicToken> children = token.getChildren();
		int size = children.size();
		if( size == 0 )
			return token;
		return getLeftmostDescendant(children.get(0));
	}
	
	/**
	 * Returns whether every leave node is a literal expression node.
	 * 
	 * @param kb the knowledge base
	 * @return <code>true</code> if all leaves are literals
	 * @throws SymbolicException if the query cannot be evaluated
	 */
	public static boolean areAllLeavesLiterals(IKnowledgeBase kb) {
		if( kb == null ) throw new NullPointerException("kb is null");
		
		// defined in symbolicrules.dlog
		IPredicate nonLiteralLeaves = IrisUtil.predicate("symNonLiteralLeaves", 1);
		IQuery query = Factory.BASIC.createQuery(IrisUtil.literal(nonLiteralLeaves, "?id"));
		try {
			IRelation relation = kb.execute(query);
			return relation.size() == 0;
		} catch( EvaluationException e ) {
			throw new SymbolicException(e);
		}
	}
	
	public static String prettyPrint(ISymbolicToken token) {
		final int INDENT_LEVEL = 2;
		
		String asString = token.toString();
		StringBuilder output = new StringBuilder((int)(asString.length() * 1.25));
		int indent = 0;
		for( int i = 0, ilen = asString.length(); i < ilen; ++i ) {
			char c = asString.charAt(i);
			if( c == '{' ) {
				output.append('\n');
				addIndent(output, indent);
				indent += INDENT_LEVEL;
			} else if( c == '}' ) {
				indent -= INDENT_LEVEL;
			}
			output.append(c);
		}
		return output.toString();
	}
	
	private static StringBuilder addIndent(StringBuilder builder, int num) {
		while( num-- > 0 )
			builder.append(' ');
		return builder;
	}
}
