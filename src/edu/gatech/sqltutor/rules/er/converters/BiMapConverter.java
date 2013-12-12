package edu.gatech.sqltutor.rules.er.converters;

import java.util.Map.Entry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * XStream converter for Guava {@link BiMap} objects.
 */
public class BiMapConverter 
		implements Converter {
	protected String entryAlias = "entry";
	protected String keyAlias = "key";
	protected String valueAlias = "value";

	public BiMapConverter() {}
	
	public BiMapConverter(String keyAlias, String valueAlias) {
		this.keyAlias = keyAlias;
		this.valueAlias = valueAlias;
	}

	public BiMapConverter(String entryAlias, String keyAlias, String valueAlias) {
		this.entryAlias = entryAlias;
		this.keyAlias = keyAlias;
		this.valueAlias = valueAlias;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type) {
		return BiMap.class.isAssignableFrom(type);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		@SuppressWarnings("rawtypes")
		BiMap<?,?> map = (BiMap)source;
		for( Entry<?,?> entry : map.entrySet() ) {
			writer.startNode(entryAlias);
			writer.startNode(keyAlias);
			context.convertAnother(entry.getKey());
			writer.endNode();
			writer.startNode(valueAlias);
			context.convertAnother(entry.getValue());
			writer.endNode();
			writer.endNode();
		}
	}

	@Override
	@SuppressWarnings({"unchecked","rawtypes"})
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		BiMap map = HashBiMap.create();
		while( reader.hasMoreChildren() ) {
			String key = null, value = null, nodeName;
			reader.moveDown();
			if( !entryAlias.equals(reader.getNodeName()) )
				throw new ConversionException("Malformed XML, expected '" + entryAlias + "' node, not: " + reader.getNodeName());

			for( int i = 0; i < 2; ++i ) {
				reader.moveDown();
				nodeName = reader.getNodeName();
				if( keyAlias.equals(nodeName) )
					key = reader.getValue(); // FIXME non-simple values?
				else if( valueAlias.equals(nodeName) )
					value = reader.getValue();
				reader.moveUp();
			}

			if( key == null )
				throw new ConversionException("Missing <" + keyAlias + ">");
			if( value == null )
				throw new ConversionException("Missing <" + valueAlias + ">");

			map.put(key, value);
			reader.moveUp();
		}
		return map;
	}
}