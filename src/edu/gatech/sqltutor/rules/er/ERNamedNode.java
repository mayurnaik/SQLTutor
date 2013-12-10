package edu.gatech.sqltutor.rules.er;

import com.google.common.base.Objects;

public abstract class ERNamedNode implements ERNode {
	private final String name;
	public ERNamedNode(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
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
		if( obj == null || !(obj instanceof ERNamedNode) )
			return false;
		ERNamedNode that = (ERNamedNode)obj;
		return this.getNodeType() == that.getNodeType() && 
			Objects.equal(this.name, that.name);
	}
}
