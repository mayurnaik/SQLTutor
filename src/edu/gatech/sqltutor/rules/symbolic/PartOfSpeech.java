package edu.gatech.sqltutor.rules.symbolic;

/**
 * <a href="http://www.cis.upenn.edu/~treebank/">Penn Treebank Project</a> 
 * part-of-speech tags.
 * <p>
 * These were scraped from the UPenn  
 * <a href="http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html">
 * course website</a> and supplemented with chunks from 
 * <a href="http://www.clips.ua.ac.be/pages/mbsp-tags">Penn Treebank II</a>.
 * </p>
 */
public enum PartOfSpeech {
	// non-chunks
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
	WH_ADVERB("WRB"),
	
	//// PUNCTUATION
	COMMA(","),
	PERIOD("."),
	QUESTION_MARK("?"),
	EXCLAMATION_POINT("!"),
	
	//// CHUNKS
	
	// words: DT+RB+JJ+NN + PR
	// example: the strange bird
	NOUN_PHRASE (true,"NP"),

	// words: TO+IN
	// example: in between
	PREPOSITIONAL_PHRASE(true,"PP"),

	// words: RB+MD+VB 
	// example: was looking
	VERB_PHRASE (true,"VP "),

	// words: RB
	// example: also
	ADVERB_PHRASE(true,"ADVP"),

	// words: CC+RB+JJ
	// example: warm and cosy
	ADJECTIVE_PHRASE (true,"ADJP"),

	// words: IN
	// example: ->whether<- or not
	SUBORDINATING_CONJUNCTION (true,"SBAR"),

	// words: RP
	// example: ->up<- the stairs
	PARTICLE_CHUNK(true,"PRT"),

	// words: UH
	// example: hello
	INTERJECTION_CHUNK(true,"INTJ"),
	
	SENTENCE(true, "S");

	private final boolean isChunk;
	private final String tag;

	private PartOfSpeech(String tag) {
		this(false, tag);
	}
	
	private PartOfSpeech(boolean isChunk, String tag) {
		this.isChunk = isChunk;
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
	
	public boolean isChunk() { return isChunk; }

	public String getTag() {
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