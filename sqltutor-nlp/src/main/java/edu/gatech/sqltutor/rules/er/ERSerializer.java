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
package edu.gatech.sqltutor.rules.er;

import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;

import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap.ERKeyPair;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;

/**
 * For serializing {@link ERDiagram}s to/from XML.
 */
public class ERSerializer {
	private static XStream xstream = createXStream();
	
	static XStream createXStream() {
		XStream xstream = new XStream(new Sun14ReflectionProvider(),  new Xpp3Driver());
		xstream.setMode(XStream.ID_REFERENCES);
		xstream.processAnnotations(new Class<?>[] {
			ERDiagram.class,
			EREntity.class,
			ERAttribute.class,
			ERRelationship.class,
			ERMapping.class,
			ERJoinMap.class,
			ERKeyPair.class
		});
		xstream.addDefaultImplementation(ERCompositeAttribute.class, ERAttribute.class);
		return xstream;
	}
	
	public ERSerializer() {
	}
	
	public String serialize(Object diagram) {
		if( diagram == null ) throw new NullPointerException("diagram is null");
		return xstream.toXML(diagram);
	}
	
	public void serialize(Object diagram, OutputStream stream) {
		if( diagram == null ) throw new NullPointerException("diagram is null");
		if( stream == null ) throw new NullPointerException("stream is null");
		xstream.toXML(diagram, stream);
	}
	
	public Object deserialize(String xml) {
		if( xml == null ) throw new NullPointerException("xml is null");
		return xstream.fromXML(xml);
	}
	
	public Object deserialize(InputStream stream) {
		if( stream == null ) throw new NullPointerException("stream is null");
		return xstream.fromXML(stream);
	}
}
