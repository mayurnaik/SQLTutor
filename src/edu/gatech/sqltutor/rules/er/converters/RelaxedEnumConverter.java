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
		super.marshal(value, writer, context);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Object instantiateNewInstance(HierarchicalStreamReader reader, UnmarshallingContext context) {
		String value = reader.getValue().toUpperCase(Locale.ENGLISH);
		return Enum.valueOf(context.getRequiredType(), value);
	}
}
