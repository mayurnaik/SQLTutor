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
