package edu.gatech.sqltutor.rules.er;

import com.google.common.base.Objects;

public abstract class AbstractERNamedNode implements ERNamedNode {
	private final String name;
	public AbstractERNamedNode(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getFullName() {
		return getName();
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ (31 * getNodeType());
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == this ) return true;
		if( obj == null || !(obj instanceof AbstractERNamedNode) )
			return false;
		AbstractERNamedNode that = (AbstractERNamedNode)obj;
		return this.getNodeType() == that.getNodeType() && 
			Objects.equal(this.name, that.name);
	}
}
