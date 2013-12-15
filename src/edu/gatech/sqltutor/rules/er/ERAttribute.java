package edu.gatech.sqltutor.rules.er;

import com.google.common.base.Objects;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.gatech.sqltutor.rules.er.converters.AttributeConverter;

@XStreamAlias("attribute")
@XStreamConverter(AttributeConverter.class)
public class ERAttribute extends AbstractERNamedNode implements ERNode {
	@XStreamAsAttribute
	private boolean isKey;
	
	@XStreamAsAttribute
	private boolean isDerived;
	
	@XStreamAsAttribute
	private boolean isMultivalued;
	
	@XStreamOmitField
	private ERNamedNode parent;
	
	public ERAttribute(String name) {
		super(name);
	}
	
	public ERAttribute(ERNamedNode entity, String name) {
		super(name);
		this.parent = entity;
	}
	
	@Override
	public int getNodeType() {
		return ERNode.TYPE_ATTRIBUTE;
	}
	
	public boolean isMultivalued() {
		return isMultivalued;
	}
	
	public void setMultivalued(boolean isMultivalued) {
		this.isMultivalued = isMultivalued;
	}
	
	public boolean isDerived() {
		return isDerived;
	}
	
	public void setDerived(boolean isDerived) {
		this.isDerived = isDerived;
	}

	public boolean isKey() {
		return isKey;
	}
	
	public void setKey(boolean isKey) {
		this.isKey = isKey;
	}
	
	public boolean isComposite() {
		return false;
	}
	
	public ERNamedNode getParent() {
		return parent;
	}
	
	public void setParent(ERNamedNode parent) {
		this.parent = parent;
	}
	
	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		if( parent != null )
			hashCode ^= parent.hashCode();
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( !super.equals(obj) )
			return false;
		ERAttribute that = (ERAttribute)obj;
		return Objects.equal(this.parent, that.parent);
	}
	
	public String getFullName() {
		StringBuilder b = new StringBuilder();
		if( parent != null )
			b.append(parent).append('.');
		b.append(getName());
		return b.toString();
	}
	
	@Override
	public String toString() {
		return getFullName();
	}
}
