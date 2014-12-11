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

import java.util.Locale;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

@SuppressWarnings("rawtypes")
public class RelaxedEnumConverter extends ReflectionConverter implements Converter {
	public RelaxedEnumConverter(Mapper mapper, 
			ReflectionProvider reflectionProvider) {
		super(mapper, reflectionProvider);
	}
	
	@Override
	public boolean canConvert(Class type) {
		return Enum.class.isAssignableFrom(type);
	}
	
	@Override
	public void marshal(Object original, HierarchicalStreamWriter writer, 
			MarshallingContext context) {
		String value = original.toString().toLowerCase(Locale.ENGLISH);
		writer.setValue(value);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Object instantiateNewInstance(HierarchicalStreamReader reader, UnmarshallingContext context) {
		String value = reader.getValue().toUpperCase(Locale.ENGLISH);
		return Enum.valueOf(context.getRequiredType(), value);
	}
}
