package edu.gatech.sqltutor.rules.er;

import com.google.common.base.Objects;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.gatech.sqltutor.rules.er.converters.AttributeConverter;
import edu.gatech.sqltutor.rules.er.converters.RelaxedEnumConverter;

@XStreamAlias("attribute")
@XStreamConverter(AttributeConverter.class)
public class ERAttribute extends AbstractERNamedNode implements ERNode {
	public static enum DescriptionType {
		PREPEND, REPLACE, NONE;
	}
	
	@XStreamAsAttribute
	private boolean isKey;
	
	@XStreamAsAttribute
	private boolean isDerived;
	
	@XStreamAsAttribute
	private boolean isMultivalued;
	
	@XStreamConverter(RelaxedEnumConverter.class)
	private DescriptionType describesEntity;
	
	@XStreamConverter(RelaxedEnumConverter.class)
	private ERAttributeDataType dataType;
	
	private ERObjectMetadata metadata;
	
	@XStreamOmitField
	private ERNamedNode parent;
	
	public ERAttribute(String name) {
		super(name);
	}
	
	public ERAttribute(ERNamedNode entity, String name) {
		super(name);
		this.parent = entity;
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
		return false;
	}
	
	public ERNamedNode getParent() {
		return parent;
	}
	
	public void setParent(ERNamedNode parent) {
		this.parent = parent;
	}
	
	public DescriptionType getDescribesEntity() {
		return describesEntity;
	}
	
	public void setDescribesEntity(DescriptionType describesEntity) {
		this.describesEntity = describesEntity;
	}
	
	// treat null as UNKNOWN, but leave field null for serialization purposes (to avoid an empty tag)
	public ERAttributeDataType getDataType() {
		return dataType != null ? dataType : ERAttributeDataType.UNKNOWN;
	}
	public void setDataType(ERAttributeDataType dataType) {
		this.dataType = dataType != ERAttributeDataType.UNKNOWN ? dataType : null;
	}
	
	public ERObjectMetadata getMetadata() {
		return metadata;
	}
	
	public void setMetadata(ERObjectMetadata metadata) {
		this.metadata = metadata;
	}
	
	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		if( parent != null )
			hashCode ^= parent.hashCode();
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( !super.equals(obj) )
			return false;
		ERAttribute that = (ERAttribute)obj;
		return Objects.equal(this.parent, that.parent);
	}
	
	public String getFullName() {
		StringBuilder b = new StringBuilder();
		if( parent != null )
			b.append(parent).append('.');
		b.append(getName());
		return b.toString();
	}
	
	@Override
	public String toString() {
		return getFullName();
	}
}
