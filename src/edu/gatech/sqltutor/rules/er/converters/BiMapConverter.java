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
	
	protected Class<?> keyType = String.class;
	protected Class<?> valueType = String.class;

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
			Object key = null, value = null, nodeName;
			reader.moveDown();
			if( !entryAlias.equals(reader.getNodeName()) )
				throw new ConversionException("Malformed XML, expected '" + entryAlias + "' node, not: " + reader.getNodeName());

			for( int i = 0; i < 2; ++i ) {
				reader.moveDown();
				nodeName = reader.getNodeName();
				if( keyAlias.equals(nodeName) )
					key = context.convertAnother(map, keyType);
				else if( valueAlias.equals(nodeName) )
					value = context.convertAnother(map, valueType);
				reader.moveUp();
			}

			if( key == null )
				throw new ConversionException("Missing <" + keyAlias + ">");
			if( value == null )
				throw new ConversionException("Missing <" + valueAlias + ">");

			try {
				map.put(key, value);
			} catch( IllegalArgumentException e ) {
				System.err.println("Could not insert key=" + key + ", value=" + value);
				System.err.println("value.class=" + value.getClass());
				throw e;
			}
			reader.moveUp();
		}
		return map;
	}
	
	public Class<?> getValueType() {
		return valueType;
	}
	
	public void setValueType(Class<?> valueType) {
		this.valueType = valueType;
	}
	
	public Class<?> getKeyType() {
		return keyType;
	}
	
	public void setKeyType(Class<?> keyType) {
		this.keyType = keyType;
	}
}