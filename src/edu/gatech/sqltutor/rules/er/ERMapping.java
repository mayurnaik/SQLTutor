package edu.gatech.sqltutor.rules.er;

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
}
