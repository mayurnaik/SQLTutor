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
package edu.gatech.sqltutor.rules.util;

import static edu.gatech.sqltutor.rules.symbolic.PartOfSpeech.ADVERB;
import static edu.gatech.sqltutor.rules.symbolic.PartOfSpeech.DETERMINER;
import static edu.gatech.sqltutor.rules.symbolic.PartOfSpeech.PERSONAL_PRONOUN;
import static edu.gatech.sqltutor.rules.symbolic.PartOfSpeech.POSSESSIVE_PRONOUN;
import static edu.gatech.sqltutor.rules.symbolic.PartOfSpeech.POSSESSIVE_WH_PRONOUN;
import static edu.gatech.sqltutor.rules.symbolic.PartOfSpeech.PREPOSITION_OR_SUBORDINATING_CONJUNCTION;
import static edu.gatech.sqltutor.rules.symbolic.PartOfSpeech.VERB_BASE_FORM;
import static edu.gatech.sqltutor.rules.symbolic.PartOfSpeech.VERB_RD_PERSON_SINGULAR_PRESENT;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
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
	
	public static LiteralToken all() {
		return new LiteralToken("all", DETERMINER);
	}

	public static LiteralToken each() {
		return new LiteralToken("each", DETERMINER);
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
	
	public static LiteralToken their() {
		return new LiteralToken("their", POSSESSIVE_PRONOUN);
	}
	
	public static LiteralToken its() {
		return new LiteralToken("its", POSSESSIVE_PRONOUN);
	}
	
	public static LiteralToken it() {
		return new LiteralToken("it", PERSONAL_PRONOUN);
	}
	
	public static LiteralToken they() {
		return new LiteralToken("they", PERSONAL_PRONOUN);
	}

	public static LiteralToken whose() {
		return new LiteralToken("whose", POSSESSIVE_WH_PRONOUN);
	}
	
	public static LiteralToken have() {
		return new LiteralToken("have", PartOfSpeech.VERB_BASE_FORM);
	}

	public static LiteralToken any() {
		return new LiteralToken("any", PartOfSpeech.DETERMINER);
	}
	
	private Literals() {
	}
}
