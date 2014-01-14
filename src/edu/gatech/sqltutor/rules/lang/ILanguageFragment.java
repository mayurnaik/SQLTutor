package edu.gatech.sqltutor.rules.lang;

/**
 * A symbolic language fragment.
 */
public interface ILanguageFragment {
	/**
	 * Returns the part of speech associated with this fragment, 
	 * or <code>null</code> if there is not a single part of speech.
	 */
	public PartOfSpeech getPartOfSpeech();
}
