package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;


/**
 * A language fragment that is a literal expression 
 * rather than symbolic.
 */
public class LiteralToken extends AbstractSymbolicToken {
	protected String expression;
	
	public LiteralToken(LiteralToken token) {
		super(token);
		this.expression = token.expression;
	}
	
	public LiteralToken(String expression, PartOfSpeech pos) {
		super(pos);
		if( pos == null ) 
			throw new IllegalArgumentException("Literal fragments must have a part of speech.");
		if( expression == null ) throw new NullPointerException("expression is null");
		this.expression = expression;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	public String getExpression() {
		return expression;
	}
	
	@Override
	public SymbolicType getType() {
		return SymbolicType.LITERAL;
	}
	
	@Override
	public String toString() {
		return "{" + getType() + "/" + getPartOfSpeech() + ": \"" + expression + "\"}";
	}
}
