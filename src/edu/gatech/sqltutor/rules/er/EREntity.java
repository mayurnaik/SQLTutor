package edu.gatech.sqltutor.rules.er;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("entity")
public class EREntity extends AbstractERAttributeContainer {
	private EntityType type = EntityType.THING;
	
	@XStreamAsAttribute
	private boolean isWeak = false;
	
	private ERObjectMetadata metadata;
	
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
	
	public EntityType getEntityType() {
		return type;
	}
	
	public void setEntityType(EntityType type) {
		this.type = type;
	}
	
	public ERObjectMetadata getMetadata() {
		return metadata;
	}
	
	public void setMetadata(ERObjectMetadata metadata) {
		this.metadata = metadata;
	}
}
