package edu.gatech.sqltutor.rules.er;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("entity")
public class EREntity extends ERNamedNode implements ERNode {
	@XStreamAsAttribute
	private boolean isWeak = false;
	
	private Set<ERAttribute> attributes = 
		new HashSet<ERAttribute>();
	
	public EREntity(String name) {
		super(name);
	}
	
	@Override
	public int getNodeType() {
		return ERNode.TYPE_ENTITY;
	}
	
	public boolean isWeak() {
		return isWeak;
	}
	
	public void setWeak(boolean isWeak) {
		this.isWeak = isWeak;
	}
	
	public boolean addAttribute(ERAttribute attr) {
		return attributes.add(attr);
	}
	
	public boolean removeAttribute(ERAttribute attr) {
		return attributes.remove(attr);
	}
	
	public Set<ERAttribute> getAttributes() {
		return attributes;
	}
}
