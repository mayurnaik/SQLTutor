package edu.gatech.sqltutor.rules.er;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAliasType;

@XStreamAliasType("attribute")
@XStreamAlias("attribute")
public class ERCompositeAttribute extends ERAttribute {
	private Set<ERAttribute> attributes = 
		new HashSet<ERAttribute>(3);

	public ERCompositeAttribute(ERNamedNode entity, String name) {
		super(entity, name);
	}

	public ERCompositeAttribute(String name) {
		super(name);
	}
	
	@Override
	public void setMultivalued(boolean isMultivalued) {
		if( isMultivalued )
			throw new UnsupportedOperationException("Composite attributes are not multi-valued.");
	}
	
	public ERAttribute addAttribute(String name) {
		ERAttribute attr = new ERAttribute(this, name);
		if( !attributes.add(attr) )
			throw new IllegalArgumentException("Name is in use: " + name);
		return attr;
	}
	
	public void addAttribute(ERAttribute attr) {
		attr.setParent(this);
		if( !attributes.add(attr) )
			throw new IllegalArgumentException("Name is in use: " + attr.getName());
	}
	
	public Set<ERAttribute> getAttributes() {
		return attributes;
	}

	@Override
	public boolean isComposite() {
		return true;
	}
	
	public Object readResolve() {
		for( ERAttribute child: attributes )
			child.setParent(this);
		return this;
	}
}
