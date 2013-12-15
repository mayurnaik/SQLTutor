package edu.gatech.sqltutor.rules.er;

/**
 * Nodes that have an identifying name.
 */
public interface ERNamedNode extends ERNode {
	/**
	 * Returns the local name of this node.
	 */
	public String getName();
	
	/**
	 * Returns the qualified name of this node.
	 */
	public String getFullName();
}
