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
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.gatech.sqltutor.rules.er.converters.JoinMapConverter;
import edu.gatech.sqltutor.rules.er.converters.KeyPairConverter;
import edu.gatech.sqltutor.util.Pair;

/**
 * The way a relationship is modeled through joins.
 */
@XStreamAlias("join")
@XStreamConverter(JoinMapConverter.class)
public abstract class ERJoinMap {
	public static enum MapType {
		MERGED,
		FOREIGN_KEY,
		LOOKUP_TABLE
	}
	
	/**
	 * A pair of keys used in a join, one primary and one foreign.
	 */
	@XStreamConverter(KeyPairConverter.class)
	public static class ERKeyPair {
		private Pair<String, String> keys;
		public ERKeyPair(String primaryKey, String foreignKey) {
			if( primaryKey == null ) throw new NullPointerException("primaryKey is null");
			if( foreignKey == null ) throw new NullPointerException("foreignKey is null");
			keys = Pair.make(primaryKey, foreignKey);
		}
		
		public String getPrimaryKey() { return keys.getFirst(); }
		public String getForeignKey() { return keys.getSecond(); }
		
		public Pair<String, String> asPair() { return keys; }
		
		@Override
		public int hashCode() {
			return Objects.hashCode(keys);
		}
		
		@Override
		public boolean equals(Object obj) {
			if( obj == this ) return true;
			if( obj == null || !obj.getClass().equals(getClass()) )
				return false;
			ERKeyPair that = (ERKeyPair)obj;
			return this.keys.equals(that.keys);
		}
		
		@Override
		public String toString() {
			return Objects.toStringHelper(this)
				.add("pk", getPrimaryKey())
				.add("fk", getForeignKey())
				.toString();
		}
	}
	
	@XStreamOmitField
	private transient String relationship;

	public ERJoinMap() {
	}
	
	/** Returns the type of join mapping this is. */
	public abstract MapType getMapType();
	
	public String getRelationship() {
		return relationship;
	}
	
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}
}
