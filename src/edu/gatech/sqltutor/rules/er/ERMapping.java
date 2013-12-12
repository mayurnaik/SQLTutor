package edu.gatech.sqltutor.rules.er;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

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
	
	@XStreamOmitField
	private ERDiagram diagram;
	
	@XStreamAlias(value="attribute-map")
	@XStreamConverter(AttributeColumnConverter.class)
	BiMap<String, String> attributeToColumn = HashBiMap.create();

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
	
	public ERAttribute getAttribute(String name) {
		checkDiagram();
		if( !isFQName(name) )
			throw new IllegalArgumentException("Name must be fully qualified: " + name);
		String attrName = getAttributeName(name);
		if( attrName == null ) {
			System.err.println("No attribute for column: " + name);
			return null;
		}
		System.out.println("Looking for: " + attrName);
		Matcher m = fqNamePattern.matcher(attrName);
		if( !m.matches() )
			throw new IllegalStateException("Attribute name must be fully qualified: " + name);
		
		String entityName = m.group(1);
		attrName = m.group(2);
		
		for( EREntity entity: diagram.getEntities() ) {
			if( !entityName.equals(entity.getName()) )
				continue;
			
			return entity.getAttribute(attrName);
		}
		
		return null;
	}
	
	protected void checkDiagram() {
		if( diagram == null ) throw new IllegalStateException("No ER diagram is associated.");
	}
}
