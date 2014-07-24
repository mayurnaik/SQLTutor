package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.er.ERRelationship;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

/**
 * Token indicating entities (<i>e1</i>,<i>e2</i>) participate in relationship <i>r</i>.
 */
public class InRelationshipToken extends AbstractSymbolicToken implements
		ISymbolicToken {
	private TableEntityToken leftEntity, rightEntity;
	private ERRelationship relationship;

	public InRelationshipToken(InRelationshipToken toCopy) {
		super(toCopy);
	}

	public InRelationshipToken(PartOfSpeech pos) {
		super(pos);
	}
	
	public InRelationshipToken() {
		super(PartOfSpeech.VERB_PHRASE);
	}
	
	public InRelationshipToken(TableEntityToken leftEntity, TableEntityToken rightEntity, 
			ERRelationship relationship) {
		this();
		this.leftEntity = leftEntity;
		this.rightEntity = rightEntity;
		this.relationship = relationship;
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.IN_RELATIONSHIP;
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		b.append(", e1=").append(leftEntity).append(", e2=").append(rightEntity)
			.append(", rel=").append(relationship);
		return b;
	}

	public TableEntityToken getLeftEntity() {
		return leftEntity;
	}

	public void setLeftEntity(TableEntityToken leftEntity) {
		this.leftEntity = leftEntity;
	}

	public TableEntityToken getRightEntity() {
		return rightEntity;
	}

	public void setRightEntity(TableEntityToken rightEntity) {
		this.rightEntity = rightEntity;
	}

	public ERRelationship getRelationship() {
		return relationship;
	}

	public void setRelationship(ERRelationship relationship) {
		this.relationship = relationship;
	}
}
