package edu.gatech.sqltutor.rules.er;

import java.util.Set;

/**
 * Nodes containing attributes, such as entities and relationships.
 */
public interface ERAttributeContainer extends ERNamedNode {
	public void addAttribute(ERAttribute attr);
	public ERAttribute addAttribute(String name);
	public ERCompositeAttribute addCompositeAttribute(String name);
	
	public ERAttribute addChildAttribute(String parent, String name);
	public ERAttribute getAttribute(String name);
	
	public ERAttribute removeAttribute(String name);
	public void removeAttribute(ERAttribute attr);
	
	public Set<ERAttribute> getSimpleAttributes();
	public Set<ERAttribute> getTopLevelAttributes();
	public Set<ERAttribute> getAttributes();
}
