package edu.gatech.sqltutor.rules.er.mapping;

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
	
	public void mapRelationship(ERRelationship relationship, ERJoinMap joinType) {
		mapRelationship(relationship.getFullName(), joinType);
	}
	
	public void mapRelationship(String name, ERJoinMap joinType) {
		relationshipToJoin.put(name, joinType);
	}
	
	protected void checkDiagram() {
		if( diagram == null ) throw new IllegalStateException("No ER diagram is associated.");
	}
}
