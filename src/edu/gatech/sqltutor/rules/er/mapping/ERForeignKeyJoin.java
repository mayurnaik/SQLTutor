package edu.gatech.sqltutor.rules.er.mapping;

import com.google.common.base.Objects;

/**
 * A join using a single key relationship.
 */
public class ERForeignKeyJoin extends ERJoinMap {
	private ERKeyPair keyPair;
	
	public ERForeignKeyJoin(String primaryKey, String foreignKey) {
		this.keyPair = new ERKeyPair(primaryKey, foreignKey);
	}
	
	public ERForeignKeyJoin(ERKeyPair keyPair) {
		if( keyPair == null ) throw new NullPointerException("keyPair is null");
		this.keyPair = keyPair;
	}
	
	@Override
	public MapType getMapType() {
		return MapType.FOREIGN_KEY;
	}
	
	public ERKeyPair getKeyPair() {
		return keyPair;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(keyPair);
	}

	@Override
	public boolean equals(Object obj) {
		if( obj == this ) return true;
		if( obj == null || !obj.getClass().equals(getClass()) )
			return false;
		ERForeignKeyJoin that = (ERForeignKeyJoin)obj;
		return this.keyPair.equals(that.keyPair);
	}
}