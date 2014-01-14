package edu.gatech.sqltutor.rules.lang;

/**
 * <a href="http://www.cis.upenn.edu/~treebank/">Penn Treebank Project</a> 
 * part-of-speech tags.
 * <p>
 * These were scraped from the UPenn  
 * <a href="http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html">
 * course website</a>.
 * </p>
 */
public enum PartOfSpeech {
	COORDINATING_CONJUNCTION("CC"),
	CARDINAL_NUMBER("CD"),
	DETERMINER("DT"),
	EXISTENTIAL_THERE("EX"),
	FOREIGN_WORD("FW"),
	PREPOSITION_OR_SUBORDINATING_CONJUNCTION("IN"),
	ADJECTIVE("JJ"),
	ADJECTIVE_COMPARATIVE("JJR"),
	ADJECTIVE_SUPERLATIVE("JJS"),
	LIST_ITEM_MARKER("LS"),
	MODAL("MD"),
	NOUN_SINGULAR_OR_MASS("NN"),
	NOUN_PLURAL("NNS"),
	PROPER_NOUN_SINGULAR("NNP"),
	PROPER_NOUN_PLURAL("NNPS"),
	PREDETERMINER("PDT"),
	POSSESSIVE_ENDING("POS"),
	PERSONAL_PRONOUN("PRP"),
	POSSESSIVE_PRONOUN("PRP$"),
	ADVERB("RB"),
	ADVERB_COMPARATIVE("RBR"),
	ADVERB_SUPERLATIVE("RBS"),
	PARTICLE("RP"),
	SYMBOL("SYM"),
	TO("TO"),
	INTERJECTION("UH"),
	VERB_BASE_FORM("VB"),
	VERB_PAST_TENSE("VBD"),
	VERB_GERUND_OR_PRESENT_PARTICIPLE("VBG"),
	VERB_PAST_PARTICIPLE("VBN"),
	VERB_NON_RD_PERSON_SINGULAR_PRESENT("VBP"),
	VERB_RD_PERSON_SINGULAR_PRESENT("VBZ"),
	WH_DETERMINER("WDT"),
	WH_PRONOUN("WP"),
	POSSESSIVE_WH_PRONOUN("WP$"),
	WH_ADVERB("WRB");

	private final String tag;

	private PartOfSpeech(String tag) {
		this.tag = tag;
	}

	/**
	 * Returns the encoding for this part-of-speech.
	 * 
	 * @return A string representing a Penn Treebank encoding for an English
	 * part-of-speech.
	 */
	public String toString() {
		return getTag();
	}

	protected String getTag() {
		return this.tag;
	}

	public static PartOfSpeech get(String value) {
		value = value.toUpperCase();
		for( PartOfSpeech v : values() ) {
			if( value.equals(v.getTag()) ) {
				return v;
			}
		}

		throw new IllegalArgumentException("Unknown part of speech: '" + value + "'.");
	}
}