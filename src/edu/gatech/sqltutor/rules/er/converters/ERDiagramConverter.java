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

import java.util.HashSet;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.EREntity;
import edu.gatech.sqltutor.rules.er.ERNamedNode;
import edu.gatech.sqltutor.rules.er.ERRelationship;

@SuppressWarnings({"rawtypes"})
public class ERDiagramConverter extends ReflectionConverter implements
		Converter {
	public ERDiagramConverter(Mapper mapper,
			ReflectionProvider reflectionProvider) {
		super(mapper, reflectionProvider);
	}

	@Override
	public boolean canConvert(Class type) {
		return ERDiagram.class.equals(type);
	}
	
	@Override
	public void marshal(Object original, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		ERDiagram diagram = (ERDiagram)original;
		writer.startNode("entities");
		context.convertAnother(new HashSet<EREntity>(diagram.getEntities()));
		writer.endNode();
		
		writer.startNode("relationships");
		context.convertAnother(new HashSet<ERRelationship>(diagram.getRelationships()));
		writer.endNode();
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		ERDiagram diagram = new ERDiagram();
		while( reader.hasMoreChildren() ) {
			reader.moveDown();
			
			Class<? extends ERNamedNode> type = null;
			String nodeName = reader.getNodeName();
			if( "entities".equals(nodeName) ) {
				type = EREntity.class;
			} else if( "relationships".equals(nodeName) ) {
				type = ERRelationship.class;
			} else {
				throw new ConversionException("Unexpected node: " + nodeName);
			}
			
			while( reader.hasMoreChildren() ) {
				reader.moveDown();
				ERNamedNode node = (ERNamedNode)context.convertAnother(diagram, type);
				diagram.addNode(node);
				reader.moveUp();
			}
			
			reader.moveUp();
		}
		
		return diagram;
	}
}
