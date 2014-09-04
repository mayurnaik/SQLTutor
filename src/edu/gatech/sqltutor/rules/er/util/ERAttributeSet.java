package edu.gatech.sqltutor.rules.er.util;

import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.ERCompositeAttribute;
import edu.gatech.sqltutor.rules.er.ERNamedNode;
import edu.gatech.sqltutor.rules.er.converters.AttributeSetConverter;

@XStreamConverter(AttributeSetConverter.class)
public class ERAttributeSet extends ERNodeMap<ERAttribute> {
	private ERNamedNode parent;
	
	public ERAttributeSet(ERNamedNode parent) {
		this.parent = parent;
	}
	
	protected void checkNameAvailable(String name) {
		if( hasNode(name) )
			throw new IllegalArgumentException("Name is already in use.");
	}
	
	public void addAttribute(ERAttribute attr) {
		attr.setParent(parent);
		String name = getNodeKey(attr);
		checkNameAvailable(name);
		addNode(attr);
		
		if( attr instanceof ERCompositeAttribute ) {
			ERCompositeAttribute composite = (ERCompositeAttribute)attr;
			for( ERAttribute child: composite.getAttributes() )
				addNode(child);
		}
	}
	
	public ERAttribute addAttribute(String name) {
		checkNameAvailable(name);
		ERAttribute attr = new ERAttribute(parent, name);
		addNode(attr);
		return attr;
	}
	
	public ERAttribute addCompositeAttribute(String name) {
		checkNameAvailable(name);
		ERCompositeAttribute attr = new ERCompositeAttribute(parent, name);
		addNode(attr);
		return attr;
	}
	
	public ERAttribute addChildAttribute(String parent, String name) {
		checkNameAvailable(name);
		ERAttribute parentAttr = getNode(parent);
		if( parentAttr == null )
			throw new IllegalArgumentException("No composite attribute: " + parent);
		if( !(parentAttr instanceof ERCompositeAttribute) )
			throw new IllegalArgumentException("Parent attribute is not composite: " + parent);
		
		ERAttribute attr = ((ERCompositeAttribute)parentAttr).addAttribute(name);
		addNode(attr);
		return attr;
	}
	
	public ERAttribute getAttribute(String name) {
		return getNode(name);
	}
	
	public void removeAttribute(ERAttribute attr) {
		String name = getNodeKey(attr);
		ERAttribute current = getAttribute(name);
		if( current == attr )
			removeAttribute(name);
	}
	
	public ERAttribute removeAttribute(String name) {
		ERAttribute attr = removeNode(name);
		if( attr instanceof ERCompositeAttribute ) {
			ERCompositeAttribute comp = (ERCompositeAttribute)attr;
			for( ERAttribute child: comp.getAttributes() )
				removeNode(child);
		}
		return attr;
	}
	
	public Set<ERAttribute> getSimpleAttributes() {
		return Sets.filter(getNodes(), new Predicate<ERAttribute>() {
			@Override
			public boolean apply(ERAttribute attr) {
				return !(attr.isComposite() || attr.isMultivalued());
			}
		});
	}
	
	public Set<ERAttribute> getTopLevelAttributes() {
		return Sets.filter(getNodes(), new Predicate<ERAttribute>() {
			@Override
			public boolean apply(ERAttribute attr) {
				return attr.getParent() == parent;
			}
		});
	}

	@Override
	protected String getNodeKey(ERAttribute node) {
		// use local name
		return node.getName();
	}
	
	public ERNamedNode getParent() {
		return parent;
	}
	
	public void setParent(ERNamedNode parent) {
		this.parent = parent;
		if( parent != null )
			readResolve();
	}
	
	protected Object readResolve() {
		for(ERAttribute attr: getNodes())
			attr.setParent(parent);
		return this;
	}
}
