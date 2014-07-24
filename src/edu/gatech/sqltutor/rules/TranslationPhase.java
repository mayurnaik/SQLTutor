package edu.gatech.sqltutor.rules;

/**
 * The sequential translation phases that rules may 
 * participate in.
 */
public enum TranslationPhase {
	/** Any initial computation needed before analysis begins. */
	PREPROCESSING,
	/** Query analysis and fact discovery phase. */
	SQL_ANALYSIS, 
	/** Initial transformation to NL-like structure. */
	TRANSFORMATION,
	/** Lowering to NL and grammatical simplification. */
	LOWERING;
}
