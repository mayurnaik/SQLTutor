package edu.gatech.sqltutor.rules.er;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

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
	
	public ERAttribute(String name) {
		super(name);
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
	
	public boolean addAttribute(ERAttribute attr) {
		return attributes.add(attr);
	}
	
	public boolean removeAttribute(ERAttribute attr) {
		return attributes.remove(attr);
	}
	
	public Set<ERAttribute> getAttributes() {
		return Collections.unmodifiableSet(attributes);
	}
}
