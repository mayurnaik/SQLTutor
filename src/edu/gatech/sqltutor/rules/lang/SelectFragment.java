package edu.gatech.sqltutor.rules.lang;

public class SelectFragment implements ILanguageFragment {
	@Override
	public PartOfSpeech getPartOfSpeech() {
		return PartOfSpeech.VERB_BASE_FORM;
	}
	
	@Override
	public String toString() {
		return "{select}";
	}
}
