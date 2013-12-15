package edu.gatech.sqltutor.rules.er.converters;

import java.util.HashSet;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.ERNamedNode;
import edu.gatech.sqltutor.rules.er.util.ERAttributeSet;

public class AttributeSetConverter extends ReflectionConverter implements
		Converter {
	public AttributeSetConverter(Mapper mapper,
			ReflectionProvider reflectionProvider) {
		super(mapper, reflectionProvider);
	}

	@Override
	public boolean canConvert(Class type) {
		return ERAttributeSet.class.equals(type);
	}
	
	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		ERAttributeSet attrs = (ERAttributeSet)obj;
		context.convertAnother(new HashSet<ERAttribute>(attrs.getTopLevelAttributes()));
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		ERNamedNode parent = (ERNamedNode)context.currentObject();
		ERAttributeSet attrs = new ERAttributeSet(parent);
		while( reader.hasMoreChildren() ) {
			reader.moveDown();
			ERAttribute attr = (ERAttribute)context.convertAnother(attrs, ERAttribute.class);
			attrs.addAttribute(attr);
			reader.moveUp();
		}
		return attrs;
		
	}
}
