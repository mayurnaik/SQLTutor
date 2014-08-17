package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.Utils;
import edu.gatech.sqltutor.rules.er.EREdgeConstraint;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class TableEntityToken extends AbstractSymbolicToken
		implements ISymbolicToken, INounToken, IScopedToken {
	/** The referenced table. */
	protected FromTable table;
	
	/** The conjunct scope of this token. */
	protected QueryTreeNode cscope;
	
	protected String id;
	protected String singular;
	protected String plural;
	protected int cardinality = EREdgeConstraint.ANY_CARDINALITY;
	
	public TableEntityToken(TableEntityToken token) {
		super(token);
		this.id = token.id;
		this.cscope = token.cscope;
		this.table = token.table;
		this.singular = token.singular;
		this.plural = token.plural;
		this.cardinality = token.cardinality;
	}
	
	public TableEntityToken(FromTable table) {
		this(table, PartOfSpeech.NOUN_SINGULAR_OR_MASS);
	}
	
	public TableEntityToken(FromTable table, PartOfSpeech pos) {
		super(pos);
		this.table = table;
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.TABLE_ENTITY;
	}
	
	@Override
	public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
		switch( partOfSpeech ) {
			case NOUN_SINGULAR_OR_MASS:
			case NOUN_PLURAL:
			case NOUN_PHRASE:
				break;
			default:
				throw new SymbolicException("Table entities must be nouns or noun phrases: " + partOfSpeech);
		}
		super.setPartOfSpeech(partOfSpeech);
	}
	
	public FromTable getTable() {
		return table;
	}
	
	public void setTable(FromTable table) {
		this.table = table;
	}

	@Override
	public String getSingularLabel() {
		return singular;
	}

	@Override
	public String getPluralLabel() {
		return plural;
	}

	@Override
	public void setSingularLabel(String label) {
		this.singular = label;
	}

	@Override
	public void setPluralLabel(String label) {
		this.plural = label;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public int getCardinality() {
		return cardinality;
	}
	
	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}
	
	@Override
	public QueryTreeNode getConjunctScope() {
		return cscope;
	}
	
	@Override
	public void setConjunctScope(QueryTreeNode cscope) {
		this.cscope = cscope;
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		super.addPropertiesString(b);
		b.append(", id=").append(id)
			.append(", table=").append(QueryUtils.nodeToString(table))
			.append(", cscope=").append(Utils.ellided(QueryUtils.nodeToString(cscope)))
			.append(", singular=").append(singular)
			.append(", plural=").append(plural)
			.append(", card=");
		if( cardinality == EREdgeConstraint.ANY_CARDINALITY )
			b.append("N");
		else
			b.append(cardinality);
		return b;
	}
}
