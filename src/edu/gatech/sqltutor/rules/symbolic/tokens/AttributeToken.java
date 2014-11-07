/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.util.NLUtil;

public class AttributeToken extends AbstractSymbolicToken implements ISymbolicToken, INounToken {
	
	protected ERAttribute attribute;
	protected boolean definite = true;

	public AttributeToken(AttributeToken token) {
		super(token);
		this.attribute = token.attribute;
		this.definite = token.definite;
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

	@Override
	public boolean isDefinite() {
		return definite;
	}

	@Override
	public void setDefinite(boolean definite) {
		this.definite = definite;
	}
	
	protected boolean individual;

	@Override
	public boolean isIndividual() {
		return individual;
	}

	@Override
	public void setIndividual(boolean individual) {
		this.individual = individual;
	}
}
