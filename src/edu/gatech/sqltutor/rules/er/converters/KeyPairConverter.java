package edu.gatech.sqltutor.rules.er.converters;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap.ERKeyPair;

public class KeyPairConverter implements Converter {

	private static final String NODE_FOREIGN_KEY = "foreign";
	private static final String NODE_PRIMARY_KEY = "primary";

	public KeyPairConverter() {
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type) {
		return ERKeyPair.class.isAssignableFrom(type);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer, 
			MarshallingContext context) {
		ERKeyPair pair = (ERKeyPair)source;
		writer.startNode(NODE_PRIMARY_KEY);
		writer.setValue(pair.getPrimaryKey());
		writer.endNode();
		writer.startNode(NODE_FOREIGN_KEY);
		writer.setValue(pair.getForeignKey());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, 
			UnmarshallingContext context) {
		String pk = null, fk = null;
		while( reader.hasMoreChildren() ) {
			reader.moveDown();
			String nodeName = reader.getNodeName();
			if( NODE_PRIMARY_KEY.equals(nodeName) )
				pk = reader.getValue();
			else if( NODE_FOREIGN_KEY.equals(nodeName) )
				fk = reader.getValue();
			reader.moveUp();
			
			if( pk != null && fk != null )
				break;
		}
		
		if( pk == null || fk == null )
			throw new ConversionException("Missing primary and/or foreign key.");
		
		return new ERKeyPair(pk, fk);
	}
}
