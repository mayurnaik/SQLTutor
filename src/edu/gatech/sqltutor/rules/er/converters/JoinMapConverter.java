package edu.gatech.sqltutor.rules.er.converters;

import java.util.Locale;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import edu.gatech.sqltutor.rules.er.mapping.ERForeignKeyJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap.MapType;
import edu.gatech.sqltutor.rules.er.mapping.ERLookupTableJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERMergedJoin;

@SuppressWarnings("rawtypes")
public class JoinMapConverter extends ReflectionConverter implements Converter {
	private static final String TYPE_ATTR = "type";
	public JoinMapConverter(Mapper mapper, 
			ReflectionProvider reflectionProvider) {
		super(mapper, reflectionProvider);
	}
	
	@Override
	public boolean canConvert(Class type) {
		return ERJoinMap.class.isAssignableFrom(type);
	}
	
	@Override
	public void marshal(Object original, HierarchicalStreamWriter writer, 
			MarshallingContext context) {
		ERJoinMap joinMap = (ERJoinMap)original;
		
		writer.addAttribute(TYPE_ATTR, 
			joinMap.getMapType().name().toLowerCase(Locale.ENGLISH));
		super.marshal(original, writer, context);
	}
	
	@Override
	protected Object instantiateNewInstance(HierarchicalStreamReader reader, 
			UnmarshallingContext context) {
		String typeString = reader.getAttribute(TYPE_ATTR);
		if( typeString == null )
			throw new ConversionException("Missing '" + TYPE_ATTR + "' attribute.");
		typeString = typeString.toUpperCase(Locale.ENGLISH);
		MapType mapType = MapType.valueOf(typeString);
		Class<? extends ERJoinMap> mapClass = null;
		switch( mapType ) {
			case MERGED: 
				mapClass = ERMergedJoin.class; break;
			case FOREIGN_KEY:
				mapClass = ERForeignKeyJoin.class; break;
			case LOOKUP_TABLE:
				mapClass = ERLookupTableJoin.class; break;
		}
		
		ERJoinMap map = (ERJoinMap)reflectionProvider.newInstance(mapClass);
		return map;
	}
}
