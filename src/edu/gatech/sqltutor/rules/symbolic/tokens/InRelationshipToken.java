package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.er.ERRelationship;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

/**
 * Token indicating entities (<i>e1</i>,<i>e2</i>) participate in relationship <i>r</i>.
 */
public class InRelationshipToken extends AbstractSymbolicToken implements
		ISymbolicToken {
	private TableEntityToken entity1, entity2;
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

	@Override
	public SymbolicType getType() {
		return SymbolicType.IN_RELATIONSHIP;
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		b.append(", e1=").append(entity1).append(", e2=").append(entity2)
			.append(", rel=").append(relationship);
		return b;
	}
	
}
