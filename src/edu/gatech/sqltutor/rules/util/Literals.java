package edu.gatech.sqltutor.rules.util;

import static edu.gatech.sqltutor.rules.symbolic.PartOfSpeech.*;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;

/**
 * Short-hand methods for commonly used literal expressions.
 */
public class Literals {
	public static LiteralToken of() {
		return new LiteralToken("of", PREPOSITION_OR_SUBORDINATING_CONJUNCTION);
	}

	public static LiteralToken does() {
		return new LiteralToken("does", VERB_RD_PERSON_SINGULAR_PRESENT);
	}

	public static LiteralToken not() {
		return new LiteralToken("not", ADVERB);
	}

	public static LiteralToken the() {
		return new LiteralToken("the", DETERMINER);
	}

	public static LiteralToken a() {
		return new LiteralToken("a", DETERMINER);
	}

	public static LiteralToken an() {
		return new LiteralToken("an", DETERMINER);
	}
	
	public static LiteralToken is() {
		return new LiteralToken("is", VERB_BASE_FORM);
	}

	private Literals() {
	}
}
