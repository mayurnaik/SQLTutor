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
	private boolean leftParticipating;
	private boolean rightParticipating;

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
			ERRelationship relationship, boolean leftParticipating, boolean rightParticipating) {
		this();
		this.leftEntity = leftEntity;
		this.rightEntity = rightEntity;
		this.relationship = relationship;
		this.leftParticipating = leftParticipating;
		this.rightParticipating = rightParticipating;
	}
	
	public InRelationshipToken(TableEntityToken leftEntity, TableEntityToken rightEntity, 
			ERRelationship relationship) {
		this(leftEntity, rightEntity, relationship, true, true);
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

	public boolean isLeftParticipating() {
		return leftParticipating;
	}

	public void setLeftParticipating(boolean leftParticipating) {
		this.leftParticipating = leftParticipating;
	}

	public boolean isRightParticipating() {
		return rightParticipating;
	}

	public void setRightParticipating(boolean rightParticipating) {
		this.rightParticipating = rightParticipating;
	}
}
