package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.FromTable;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class TableEntityToken extends AbstractSymbolicToken implements ISymbolicToken {
	/** The referenced table. */
	protected FromTable table;
	
	public TableEntityToken(TableEntityToken token) {
		super(token);
		this.table = token.table;
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
	protected void setPartOfSpeech(PartOfSpeech partOfSpeech) {
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
	public String toString() {
		if( table == null )
			return super.toString();
		return "{" + typeAndTag() + ", table=" + QueryUtils.nodeToString(table) + "}"; 
	}
}
