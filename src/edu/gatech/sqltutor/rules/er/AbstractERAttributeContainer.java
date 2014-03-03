package edu.gatech.sqltutor.rules.er;

import java.util.Set;

import edu.gatech.sqltutor.rules.er.util.ERAttributeSet;

public abstract class AbstractERAttributeContainer 
		extends AbstractERNamedNode implements 	ERAttributeContainer {
	protected ERAttributeSet attributes = new ERAttributeSet(this);

	public AbstractERAttributeContainer(String name) {
		super(name);
	}
	
	@Override
	public ERAttribute addAttribute(String name) {
		return addAttribute(name, false);
	}
	
	@Override
	public ERCompositeAttribute addCompositeAttribute(String name) {
		return (ERCompositeAttribute)addAttribute(name, true);
	}
	
	protected ERAttribute addAttribute(String name, boolean isComposite) {
		if( isComposite )
			return attributes.addCompositeAttribute(name);
		return attributes.addAttribute(name);
	}
	
	@Override
	public ERAttribute addChildAttribute(String parent, String name) {
		return attributes.addChildAttribute(parent, name);
	}
	
	@Override
	public ERAttribute getAttribute(String name) {
		return attributes.getAttribute(name);
	}

	@Override
	public void addAttribute(ERAttribute attr) {
		attributes.addAttribute(attr);
	}
	
	@Override
	public ERAttribute removeAttribute(String name) {
		return attributes.removeAttribute(name);
	}
	
	@Override
	public void removeAttribute(ERAttribute attr) {
		attributes.removeAttribute(attr);
	}
	
	@Override
	public Set<ERAttribute> getAttributes() {
		return attributes.getNodes();
	}

	@Override
	public Set<ERAttribute> getSimpleAttributes() {
		return attributes.getSimpleAttributes();
	}

	@Override
	public Set<ERAttribute> getTopLevelAttributes() {
		return attributes.getTopLevelAttributes();
	}
	
	protected Object readResolve() {
		if( attributes == null )
			attributes = new ERAttributeSet(this);
		else
			attributes.setParent(this);
		return this;
	}
}
