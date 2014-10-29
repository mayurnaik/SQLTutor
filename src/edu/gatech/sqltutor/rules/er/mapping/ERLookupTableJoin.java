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
	
	public String getCorrespondingPrimaryKey(String foreignKey) {
		String primaryKey = null;
		if(getLeftKeyPair().getForeignKey().equals(foreignKey)) {
			return getLeftKeyPair().getPrimaryKey();
		} else if(getRightKeyPair().getForeignKey().equals(foreignKey)) {
			return getRightKeyPair().getPrimaryKey();
		}
		return primaryKey;
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