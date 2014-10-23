package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.util.NLUtil;

public class AttributeToken extends AbstractSymbolicToken implements ISymbolicToken, INounToken {
	
	protected ERAttribute attribute;

	public AttributeToken(AttributeToken token) {
		super(token);
		this.attribute = token.attribute;
	}
	
	public AttributeToken(ERAttribute attr) {
		this(attr, PartOfSpeech.NOUN_SINGULAR_OR_MASS);
	}
	
	public AttributeToken(ERAttribute attr, PartOfSpeech pos) {
		super(pos);
		this.attribute = attr;
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.ATTRIBUTE;
	}
	
	public ERAttribute getAttribute() {
		return attribute;
	}
	
	public void setAttribute(ERAttribute attribute) {
		this.attribute = attribute;
	}
	
	@Override
	public String getSingularLabel() {
		String label = null;
		if( attribute != null ) {
			if(attribute.getMetadata() != null)
				label = attribute.getMetadata().getSingularLabel();
			if( label == null ) {
				label = NLUtil.nameFormat(attribute.getName()).toLowerCase();
			}
		}
		return label;
	}
	
	@Override
	public String getPluralLabel() {
		String label = null;
		if( attribute != null ) {
			if(attribute.getMetadata() != null)
				label = attribute.getMetadata().getPluralLabel();
			if( label == null ) {
				label = NLUtil.pluralize(getSingularLabel());
			}
		}
		return label;
	}
	
	@Override
	public void setSingularLabel(String label) {
		rejectSetLabel();
	}
	
	@Override
	public void setPluralLabel(String label) {
		rejectSetLabel();
	}
	
	private void rejectSetLabel() { 
		throw new UnsupportedOperationException("AttributeToken does not support overridding labels");
	}

	@Override
	public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
		switch( partOfSpeech ) {
			case NOUN_SINGULAR_OR_MASS:
			case NOUN_PLURAL:
			case NOUN_PHRASE:
				break;
			default:
				throw new SymbolicException("Attributes must be nouns or noun phrases: " + partOfSpeech);
		}
		super.setPartOfSpeech(partOfSpeech);
	}
	
	@Override
	public String toString() {
		return "{" + typeAndTag() + " attribute=" + (attribute == null ? "null" : attribute.getFullName()) + "}";
	}
}
