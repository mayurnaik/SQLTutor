package edu.gatech.sqltutor.rules.er.mapping;

import com.google.common.base.Objects;

/**
 * A join using an intermediate table with two key relationships.
 */
public class ERLookupTableJoin extends ERJoinMap {
	private ERKeyPair leftKeyPair;
	private ERKeyPair rightKeyPair;
	
	public ERLookupTableJoin(String leftPrimaryKey, String leftForeignKey, 
			String rightPrimaryKey, String rightForeignKey) {
		this.leftKeyPair = new ERKeyPair(leftPrimaryKey, leftForeignKey);
		this.rightKeyPair = new ERKeyPair(rightPrimaryKey, rightForeignKey);
	}
	
	public ERLookupTableJoin(ERKeyPair leftKeyPair, ERKeyPair rightKeyPair) {
		this.leftKeyPair = leftKeyPair;
		this.rightKeyPair = rightKeyPair;
	}
	
	@Override
	public MapType getMapType() {
		return MapType.LOOKUP_TABLE;
	}
	
	public ERKeyPair getLeftKeyPair() {
		return leftKeyPair;
	}
	
	public ERKeyPair getRightKeyPair() {
		return rightKeyPair;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(leftKeyPair, rightKeyPair);
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == this ) return true;
		if( obj == null || !obj.getClass().equals(getClass()) )
			return false;
		ERLookupTableJoin that = (ERLookupTableJoin)obj;
		return leftKeyPair.equals(that.leftKeyPair) &&
				rightKeyPair.equals(that.rightKeyPair);
	}
}