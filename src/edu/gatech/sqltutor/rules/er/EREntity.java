package edu.gatech.sqltutor.rules.er;

import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import edu.gatech.sqltutor.rules.er.util.ERAttributeSet;

@XStreamAlias("entity")
public class EREntity extends AbstractERNamedNode implements ERNode {
	
	private EntityType type = EntityType.THING;
	
	@XStreamAsAttribute
	private boolean isWeak = false;
	
	private ERAttributeSet attributes = new ERAttributeSet(this);
	
//	private Set<ERAttribute> attributes = 
//		new HashSet<ERAttribute>();
	
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
	
	public ERAttribute addAttribute(String name) {
		return addAttribute(name, false);
	}
	
	public ERCompositeAttribute addCompositeAttribute(String name) {
		return (ERCompositeAttribute)addAttribute(name, true);
	}
	
	protected ERAttribute addAttribute(String name, boolean isComposite) {
		if( isComposite )
			return attributes.addCompositeAttribute(name);
		return attributes.addAttribute(name);
//		ERAttribute attr = findAttribute(name, this.attributes, true);
//		if( attr != null )
//			return attr;
//		attr = new ERAttribute(this, name);
//		attributes.add(attr);
//		return attr;
	}
	
	public ERAttribute addChildAttribute(String parent, String name) {
		return attributes.addChildAttribute(parent, name);
	}
	
	public ERAttribute getAttribute(String name) {
		return attributes.getAttribute(name);
//		return findAttribute(name, this.attributes, true);
	}
	
//	protected ERAttribute findAttribute(String name, Set<ERAttribute> attributes, boolean recurse) {
//		for( ERAttribute attr: attributes ) {
//			if( name.equals(attr.getName()) ) {
//				return attr;
//			} else if( recurse && attr.isComposite() ) {
//				attr = findAttribute(name, attr.getAttributes(), recurse);
//				if( attr != null )
//					return attr;
//			}
//		}
//		return null;
//	}
	
//	/**
//	 * Removes a top-level attribute.
//	 * @param name the attribute name
//	 * @return if the attribute was removed
//	 */
//	public boolean removeAttribute(String name) {
//		ERAttribute attr = findAttribute(name, this.attributes, false);
//		if( attr == null )
//			return false;
//		attributes.remove(attr);
//		return true;
//	}
	
	/**
	 * Returns the top-level attributes.
	 */
	public Set<ERAttribute> getAttributes() {
		return attributes.getTopLevelAttributes();
//		return Collections.unmodifiableSet(attributes);
	}
	
	/**
	 * Returns only the simple attributes, including those nested under a 
	 * composite attribute.
	 */
	public Set<ERAttribute> getSimpleAttributes() {
		return attributes.getSimpleAttributes();
//		Set<ERAttribute> simple = 
//			new HashSet<ERAttribute>((int)Math.round(attributes.size() * 0.5));
//		for( ERAttribute attr: attributes ) {
//			if( !attr.isComposite() ) {
//				simple.add(attr);
//			} else {
//				simple.addAll(attr.getAttributes());
//			}
//		}
//		return simple;
	}
}
