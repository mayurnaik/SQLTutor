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