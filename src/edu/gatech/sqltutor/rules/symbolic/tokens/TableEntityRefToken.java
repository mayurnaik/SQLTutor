package edu.gatech.sqltutor.rules.symbolic.tokens;

import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

/**
 * This represents a reference to a table-entity 
 * that will be resolved based on context.
 */
public class TableEntityRefToken extends AbstractSymbolicToken implements ISymbolicToken {
	private TableEntityToken tableEntity;
	private boolean needsId = false;

	public TableEntityRefToken(TableEntityRefToken toCopy) {
		super(toCopy);
		this.tableEntity = toCopy.tableEntity;
		this.needsId = needsId;
	}
	
	public TableEntityRefToken(TableEntityToken tableEntity) {
		super(tableEntity.getPartOfSpeech());
		this.tableEntity = tableEntity;
	}
	
	public TableEntityToken getTableEntity() {
		return tableEntity;
	}
	
	public void setTableEntity(TableEntityToken tableEntity) {
		this.tableEntity = tableEntity;
	}
	
	public void setNeedsId(boolean needsId) {
		this.needsId = needsId;
	}
	
	public boolean getNeedsId() {
		return needsId;
	}
	
	@Override
	public SymbolicType getType() {
		return SymbolicType.TABLE_ENTITY_REF;
	}
	
	@Override
	protected StringBuilder addPropertiesString(StringBuilder b) {
		b.append("tableEntity=").append(tableEntity)
			.append(", needsId=").append(needsId);
		return b;
	}
}
