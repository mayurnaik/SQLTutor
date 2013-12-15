package edu.gatech.sqltutor.rules.er.converters;

import java.util.ArrayList;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import edu.gatech.sqltutor.rules.er.ERNamedNode;
import edu.gatech.sqltutor.rules.er.util.ERNodeMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NodeMapConverter extends ReflectionConverter 
		implements Converter {
	public NodeMapConverter(Mapper mapper, 
			ReflectionProvider reflectionProvider) {
		super(mapper, reflectionProvider);
	}

	@Override
	public boolean canConvert(Class type) {
		return ERNodeMap.class.isAssignableFrom(type);
	}
	
	@Override
	public void marshal(Object original, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		ERNodeMap<?> map = (ERNodeMap)original;
		context.convertAnother(new ArrayList(map.getNodes()));
	}
	
	
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		ArrayList<ERNamedNode> nodes = (ArrayList<ERNamedNode>)context.convertAnother(context.currentObject(), ArrayList.class);
		ERNodeMap map = new ERNodeMap();
		for( ERNamedNode node: nodes )
			map.addNode(node);
		return map;
	}
}
