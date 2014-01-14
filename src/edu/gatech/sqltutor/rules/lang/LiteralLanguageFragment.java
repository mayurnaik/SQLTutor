package edu.gatech.sqltutor.rules.lang;

/**
 * A language fragment that is a literal expression 
 * rather than symbolic.
 */
public class LiteralLanguageFragment extends AbstractLanguageFragment {
	protected final String expression;
	
	public LiteralLanguageFragment(String expression, PartOfSpeech pos) {
		super(pos);
		if( pos == null ) 
			throw new IllegalArgumentException("Literal fragments must have a part of speech.");
		if( expression == null ) throw new NullPointerException("expression is null");
		this.expression = expression;
	}
	
	public String getExpression() {
		return expression;
	}
	
	@Override
	public String toString() {
		return partOfSpeech.getTag() + "(\"" + expression + "\")";
	}
}
