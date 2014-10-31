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

@XStreamAlias("relationship")
public class ERRelationship extends AbstractERAttributeContainer {
	
	@XStreamAlias("edge")
	public static class ERRelationshipEdge {
		private EREntity entity;
		private EREdgeConstraint constraint;
		
		public ERRelationshipEdge(EREntity entity, EREdgeConstraint constraint) {
			this.entity = entity;
			this.constraint = constraint;
		}

		public EREntity getEntity() {
			return entity;
		}

		public void setEntity(EREntity entity) {
			this.entity = entity;
		}

		public EREdgeConstraint getConstraint() {
			return constraint;
		}

		public void setConstraint(EREdgeConstraint constraint) {
			this.constraint = constraint;
		}
	}

	private ERRelationshipEdge leftEdge;
	private ERRelationshipEdge rightEdge;
	
	@XStreamAsAttribute
	private boolean isIdentifying;
	
	private String verbForm;
	
	private ERRelationshipMetadata metadata = new ERRelationshipMetadata();
	
	public ERRelationship(String name) {
		super(name);
	}
	
	public ERRelationship(String name, EREntity leftEntity, 
			EREdgeConstraint leftConstraint, EREntity rightEntity,
			EREdgeConstraint rightConstraint) {
		this(name, new ERRelationshipEdge(leftEntity, leftConstraint), 
			new ERRelationshipEdge(rightEntity, rightConstraint));
	}
	
	public ERRelationship(String name, ERRelationshipEdge leftEdge, 
			ERRelationshipEdge rightEdge) {
		this(name);
		this.leftEdge = leftEdge;
		this.rightEdge = rightEdge;
	}
	
	public boolean isIdentifying() {
		return isIdentifying;
	}
	
	public void setIdentifying(boolean isIdentifying) {
		this.isIdentifying = isIdentifying;
	}
	
	public ERRelationshipEdge getLeftEdge() {
		return leftEdge;
	}
	
	public ERRelationshipEdge getRightEdge() {
		return rightEdge;
	}

	@Override
	public int getNodeType() {
		return ERNode.TYPE_RELATIONSHIP;
	}
	
	@Override
	public String getFullName() {
		StringBuilder b = new StringBuilder();
		// TODO are relationships unique by local name or by combination with entities?
//		if( leftEdge != null )
//			b.append(leftEdge.getEntity().getName()).append('.');
		b.append(getName());
//		if( rightEdge != null )
//			b.append('.').append(rightEdge.getEntity().getName());
		return b.toString();
	}

	public String getVerbForm() {
		return verbForm;
	}

	public void setVerbForm(String verbForm) {
		this.verbForm = verbForm;
	}

	public ERRelationshipMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(ERRelationshipMetadata metadata) {
		this.metadata = metadata;
	}
}
