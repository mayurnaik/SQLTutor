package edu.gatech.sqltutor.rules.er;

public class ERObjectMetadata {
	private String singularLabel;
	private String pluralLabel;

	public ERObjectMetadata() {
	}

	public String getSingularLabel() {
		return singularLabel;
	}

	public void setSingularLabel(String singular) {
		this.singularLabel = singular;
	}

	public String getPluralLabel() {
		return pluralLabel;
	}

	public void setPluralLabel(String plural) {
		this.pluralLabel = plural;
	}
}
