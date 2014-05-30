package edu.gatech.sqltutor.rules.symbolic.tokens;

/**
 * A token that is represented by a noun.
 */
public interface INounToken extends ISymbolicToken {
	public String getSingularLabel();
	public String getPluralLabel();
	public void setSingularLabel(String label);
	public void setPluralLabel(String label);
}
