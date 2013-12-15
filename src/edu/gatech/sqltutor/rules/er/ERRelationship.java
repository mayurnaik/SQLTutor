package edu.gatech.sqltutor.rules.er;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("relationship")
public class ERRelationship extends AbstractERNamedNode implements ERNode {
	
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
	
	@XStreamImplicit
	private Set<ERAttribute> attributes = 
		new HashSet<ERAttribute>(0);

	private ERRelationshipEdge leftEdge;
	private ERRelationshipEdge rightEdge;
	
	@XStreamAsAttribute
	private boolean isIdentifying;
	
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
	
	public String getFullName() {
		StringBuilder b = new StringBuilder();
		if( leftEdge != null )
			b.append(leftEdge.getEntity().getName()).append('.');
		b.append(getName());
		if( rightEdge != null )
			b.append('.').append(rightEdge.getEntity().getName());
		return b.toString();
	}
}
