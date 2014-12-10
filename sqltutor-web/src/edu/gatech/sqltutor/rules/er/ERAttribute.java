/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
		APPEND, PREPEND, REPLACE, NONE;
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
	
	private ERObjectMetadata metadata = new ERObjectMetadata();
	
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
	
	@Override
	public String getFullName() {
		StringBuilder b = new StringBuilder();
		if( parent != null )
			b.append(parent).append('.');
		b.append(getName());
		return b.toString();
	}
	
	private Object readResolve() {
		// don't want other code to have to always check if this is null
		if( metadata == null )
			metadata = new ERObjectMetadata();
		return this;
	}
	
	@Override
	public String toString() {
		return getFullName();
	}
}
