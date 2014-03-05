package edu.gatech.sqltutor.rules.symbolic;

import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.RootToken;

public class SymbolicReader {

	public SymbolicReader() {
	}
	
	public String readSymbolicState(RootToken root) {
		StringBuilder out = new StringBuilder();
		
		readSequence(out, root);
		out.append('.');
		
		return out.toString();
	}
	
	private void readToken(StringBuilder out, ISymbolicToken token) {				
		SymbolicType type = token.getType();
		switch( type ) {
			case LITERAL:
				out.append(((LiteralToken)token).getExpression());
				break;
			case ROOT:
			case LITERALS:
			case SEQUENCE: 
				readSequence(out, token); 
				break;
			case ATTRIBUTE_LIST:
			case AND:
				readConjunctedList(out, token, "and");
				break;
			case OR:
				readConjunctedList(out, token, "or");
				break;
			default:
				throw new SymbolicException("Unhandled type " + type + " for token " + token);
		}
	}
	
	/**
	 * Reads the children with spaces separating any non-punctuation symbols.
	 */
	private void readSequence(StringBuilder out, ISymbolicToken token) {
		boolean first = true;
		for( ISymbolicToken child: token.getChildren() ) {
			if( !first && !PartOfSpeech.isPunctuation(child.getPartOfSpeech()) ) {
				out.append(' ');
			}
			first = false;
			
			readToken(out, child);
		}
	}
	
	private void readConjunctedList(StringBuilder out, ISymbolicToken token, String conjunct) {
		// FIXME do the conjugation
		out.append("the ");
		readSequence(out, token);
	}

}
