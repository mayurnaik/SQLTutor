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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.ERCompositeAttribute;

public class AttributeConverter extends ReflectionConverter {
	public AttributeConverter(Mapper mapper,
			ReflectionProvider reflectionProvider) {
		super(mapper, reflectionProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type) {
		return ERAttribute.class.isAssignableFrom(type);
	}
	
	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		ERAttribute attr = (ERAttribute)obj;
		writer.addAttribute("isComposite", String.valueOf(attr.isComposite()));
		super.marshal(obj, writer, context);
	}
	
	@Override
	protected Object instantiateNewInstance(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		boolean isComposite = Boolean.parseBoolean(reader.getAttribute("isComposite"));
		Class<?> clazz = ERAttribute.class;
		if( isComposite )
			clazz = ERCompositeAttribute.class;
		ERAttribute attr = (ERAttribute)reflectionProvider.newInstance(clazz);
		return attr;
	}
}
