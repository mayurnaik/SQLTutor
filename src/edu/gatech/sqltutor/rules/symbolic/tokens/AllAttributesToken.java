package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class AllAttributesToken extends AttributeToken {

	public AllAttributesToken(AttributeToken token) {
		super(token);
	}
	
	public AllAttributesToken(ERAttribute attr) {
		super(attr, PartOfSpeech.NOUN_PLURAL);
	}
	
	public AllAttributesToken(String tableName) {
		super(new ERAttribute(tableName + "." + "*"), PartOfSpeech.NOUN_PLURAL);
	}
	
	@Override
	public SymbolicType getType() {
		return SymbolicType.ALL_ATTRIBUTES;
	}

}
