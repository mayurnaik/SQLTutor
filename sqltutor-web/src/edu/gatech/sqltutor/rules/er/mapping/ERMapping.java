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

import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.ERRelationship;
import edu.gatech.sqltutor.rules.er.converters.BiMapConverter;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap.ERKeyPair;

@XStreamAlias("ermapping")
public class ERMapping {
	private static final Pattern fqNamePattern = 
		Pattern.compile("([^\\.\\s]+)\\.([^\\.\\s]+)");
	private static boolean isFQName(String name) {
		return fqNamePattern.matcher(name).matches();
	}
	
	public static class AttributeColumnConverter extends BiMapConverter {
		public AttributeColumnConverter() { super("attribute", "column"); }
	}
	
	public static class RelationshipMapConverter extends BiMapConverter {
		public RelationshipMapConverter() { 
			super("relationship", "join");
			setValueType(ERJoinMap.class);
		}
	}
	
	@XStreamOmitField
	private ERDiagram diagram;
	
	@XStreamAlias("attribute-map")
	@XStreamConverter(AttributeColumnConverter.class)
	BiMap<String, String> attributeToColumn = HashBiMap.create();
	
	@XStreamAlias("join-map")
	@XStreamConverter(RelationshipMapConverter.class)
	BiMap<String, ERJoinMap> relationshipToJoin = HashBiMap.create();

	public ERMapping(ERDiagram diagram) {
		this.diagram = diagram;
	}

	public ERDiagram getDiagram() {
		return diagram;
	}
	
	public void setDiagram(ERDiagram diagram) {
		this.diagram = diagram;
	}
	
	public void mapAttribute(ERAttribute attribute, String column) {
		mapAttribute(attribute.getFullName(), column);
	}
	
	public void mapAttribute(String attribute, String column) {
		if( !(isFQName(attribute) && isFQName(column)) ) {
			throw new IllegalArgumentException("Both names must be fully qualified: attribute=" 
				+ attribute + ", column=" + column);
		}
		
		attributeToColumn.put(attribute, column);
	}
	
	public String getColumnName(String attribute) {
		return attributeToColumn.get(attribute);
	}
	
	public String getAttributeName(String column) {
		return attributeToColumn.inverse().get(column);
	}
	
	/**
	 * Returns the attribute a column is mapped to.
	 * @param column the fully qualified column name
	 * @return the attribute or <code>null</code> if there is no such attribute
	 */
	public ERAttribute getAttribute(String column) {
		checkDiagram();
		String attrName = getAttributeName(column);
		if( attrName == null )
			return null;
		return diagram.getAttribute(attrName);
	}
	
	public Set<String> getAttributes() {
		return attributeToColumn.keySet();
	}
	
	public Set<String> getColumns() {
		return attributeToColumn.values();
	}
	
	public Set<ERJoinMap> getJoins() {
		return relationshipToJoin.values();
	}
	
	public void mapRelationship(ERRelationship relationship, ERJoinMap joinType) {
		mapRelationship(relationship.getFullName(), joinType);
	}
	
	public void mapRelationship(String name, ERJoinMap joinType) {
		relationshipToJoin.put(name, joinType);
		joinType.setRelationship(name);
	}
	
	public String getRelationshipName(ERJoinMap join) {
		return relationshipToJoin.inverse().get(join);
	}
	
	public ERRelationship getRelationship(ERJoinMap join) {
		checkDiagram();
		return diagram.getRelationship(getRelationshipName(join));
	}
	
	/**
	 * Given a foreign key column, this method will iterate over the list of joins and return
	 * the one that uses the foreign key.
	 * 
	 * @param column	the foreign key of the join you're wishing to find
	 * @return
	 */
	public ERJoinMap getJoin(String column) {
		for(ERJoinMap join : getJoins()) {
			if(join instanceof ERForeignKeyJoin) {
				ERKeyPair p = ((ERForeignKeyJoin) join).getKeyPair();
				if(p.getForeignKey().equals(column)) {
					return join;
				}
			} else if(join instanceof ERLookupTableJoin) {
				ERKeyPair p = ((ERLookupTableJoin) join).getLeftKeyPair();
				ERKeyPair p2 = ((ERLookupTableJoin) join).getRightKeyPair();
				if(p.getForeignKey().equals(column) || p2.getForeignKey().equals(column)) {
					return join;
				}
			}
		}
		return null;
	}
	
	protected void checkDiagram() {
		if( diagram == null ) throw new IllegalStateException("No ER diagram is associated.");
	}
	
	private Object readResolve() {
		for( Entry<String, ERJoinMap> entry: relationshipToJoin.entrySet() ) {
			entry.getValue().setRelationship(entry.getKey());
		}
		return this;
	}
}
