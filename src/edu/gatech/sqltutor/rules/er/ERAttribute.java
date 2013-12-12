package edu.gatech.sqltutor.rules.er;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Objects;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("attribute")
public class ERAttribute extends ERNamedNode implements ERNode {
	@XStreamAsAttribute
	private boolean isKey;
	
	@XStreamAsAttribute
	private boolean isDerived;
	
	@XStreamAsAttribute
	private boolean isMultivalued;
	
	@XStreamImplicit
	private Set<ERAttribute> attributes = 
		new HashSet<ERAttribute>(0);
	
	@XStreamOmitField
	private EREntity entity;
	
	public ERAttribute(EREntity entity, String name) {
		super(name);
		this.entity = entity;
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
		return attributes.size() > 0;
	}
	
	public ERAttribute addAttribute(String name) {
		ERAttribute attr = new ERAttribute(entity, name);
		if( attributes.add(attr) )
			return attr;
		return getAttribute(name);
	}
	
	public ERAttribute getAttribute(String name) {
		for( ERAttribute attr: attributes ) 
			if( name.equals(attr.getName()) )
				return attr;
		return null;
	}
	
	public Set<ERAttribute> getAttributes() {
		return Collections.unmodifiableSet(attributes);
	}
	
	public EREntity getEntity() {
		return entity;
	}
	
	public void setEntity(EREntity entity) {
		this.entity = entity;
	}
	
	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		if( entity != null )
			hashCode ^= entity.hashCode();
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( !super.equals(obj) )
			return false;
		ERAttribute that = (ERAttribute)obj;
		return Objects.equal(this.entity, that.entity);
	}
	
	public String getFullName() {
		StringBuilder b = new StringBuilder();
		if( entity != null )
			b.append(entity).append('.');
		b.append(getName());
		return b.toString();
	}
	
	@Override
	public String toString() {
		return getFullName();
	}
}
