package edu.gatech.sqltutor.rules.lang;

public class AbstractLanguageFragment implements ILanguageFragment {
	protected PartOfSpeech partOfSpeech;
	
	protected AbstractLanguageFragment(PartOfSpeech pos) {
		this.partOfSpeech = pos;
	}
	
	@Override
	public PartOfSpeech getPartOfSpeech() {
		return partOfSpeech;
	}
	
	protected void setPartOfSpeech(PartOfSpeech partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}
}
