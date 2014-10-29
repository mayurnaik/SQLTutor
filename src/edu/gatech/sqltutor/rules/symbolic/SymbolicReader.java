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

import java.util.List;

import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.RootToken;

public class SymbolicReader {
	
	private static boolean isQuote(char c) { return c == '\'' || c == '"'; }

	public SymbolicReader() {
	}
	
	public String readSymbolicState(RootToken root) {
		StringBuilder out = new StringBuilder();
		
		readSequence(out, root);
		
		// insert period at the end, but inside any trailing quotes
		int dotIdx = out.length();
		while( isQuote(out.charAt(dotIdx - 1)) )
			--dotIdx;
		out.insert(dotIdx, '.');
		
		// start with uppercase
		char first = out.charAt(0);
		if( Character.isLowerCase(first) )
			out.setCharAt(0, Character.toUpperCase(first));
		
		return out.toString();
	}
	
	public String readToken(ISymbolicToken token) {
		return readToken(new StringBuilder(), token).toString();
	}
	
	public StringBuilder readToken(StringBuilder out, ISymbolicToken token) {				
		SymbolicType type = token.getType();
		switch( type ) {
			case LITERAL:
				out.append(((LiteralToken)token).getExpression());
				break;
			case ROOT:
			case SEQUENCE: 
				readSequence(out, token); 
				break;
			case ATTRIBUTE_LIST:
				// fall-through
			case AND:
				readConjunctedList(out, token, "and");
				break;
			case OR:
				readConjunctedList(out, token, "or");
				break;
			default:
				throw new UnhandledSymbolicTypeException(type, "Unhandled type for token " + token);
		}
		return out;
	}
	
	/**
	 * Reads the children with spaces separating any non-punctuation symbols.
	 */
	private void readSequence(StringBuilder out, ISymbolicToken token) {
		boolean first = true, followsOpenQuote = false;
		for( ISymbolicToken child: token.getChildren() ) {
			PartOfSpeech partOfSpeech = child.getPartOfSpeech();
			if( !(first || followsOpenQuote || 
					PartOfSpeech.isPunctuation(partOfSpeech) || 
					partOfSpeech == PartOfSpeech.POSSESSIVE_ENDING) ) {
				out.append(' ');
			}
			first = false;
			followsOpenQuote = (partOfSpeech == PartOfSpeech.QUOTE_LEFT);
			
			readToken(out, child);
		}
	}

	
	/**
	 * Append the English conjunction of a list of terms to <code>out</code>.
	 * 
	 * <p>Produces:<br />
	 * 1: &lt;child&gt; <br />
	 *	2: &lt;child1&gt; &lt;conjunct&gt; &lt;child2&gt;<br />
	 *	3+: &lt;child1&gt;, &lt;child2&gt;, ..., &lt;conjunct&gt; &lt;childN&gt;<br />
	 * </p>
	 */
	private void readConjunctedList(StringBuilder out, ISymbolicToken token, String conjunct) {
		// 1: <child>
		// 2: <child1> <conjunct> <child2>
		// 3+: <child1>, <child2>, ..., <conjunct> <childN> 
		List<ISymbolicToken> children = token.getChildren();
		for( int i = 0, ilen = children.size(); i < ilen; ++i ) {
			ISymbolicToken child = children.get(i);
			if( i != 0 ) {
				if( i != ilen - 1 ) {
					out.append(',');
				} else {
					if( i != 1 )
						out.append(',');
					out.append(' ').append(conjunct);
				}
				out.append(' ');
			}
			readToken(out, child);
		}
	}
}
