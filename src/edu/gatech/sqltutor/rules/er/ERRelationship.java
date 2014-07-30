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
}
