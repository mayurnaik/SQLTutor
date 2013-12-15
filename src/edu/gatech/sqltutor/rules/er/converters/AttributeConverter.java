package edu.gatech.sqltutor.rules.er.converters;

import com.thoughtworks.xstream.converters.Converter;
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
	private Converter defaultConverter;
	
	public AttributeConverter(Mapper mapper,
			ReflectionProvider reflectionProvider) {
		super(mapper, reflectionProvider);
		defaultConverter = new ReflectionConverter(mapper, reflectionProvider);
	}
	
	@Override
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
