package edu.gatech.sqltutor.rules.er;

import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;

/**
 * For serializing {@link ERDiagram}s to/from XML.
 */
public class ERSerializer {
	private static XStream xstream = createXStream();
	
	static XStream createXStream() {
		XStream xstream = new XStream(new Xpp3Driver());
		xstream.setMode(XStream.ID_REFERENCES);
		xstream.processAnnotations(new Class<?>[] {
			ERDiagram.class,
			EREntity.class,
			ERAttribute.class,
			ERRelationship.class
		});
		return xstream;
	}
	
	public ERSerializer() {
	}
	
	public String serialize(ERDiagram diagram) {
		if( diagram == null ) throw new NullPointerException("diagram is null");
		return xstream.toXML(diagram);
	}
	
	public void serialize(ERDiagram diagram, OutputStream stream) {
		if( diagram == null ) throw new NullPointerException("diagram is null");
		if( stream == null ) throw new NullPointerException("stream is null");
		xstream.toXML(diagram, stream);
	}
	
	public ERDiagram deserialize(String xml) {
		if( xml == null ) throw new NullPointerException("xml is null");
		return (ERDiagram)xstream.fromXML(xml);
	}
	
	public ERDiagram deserialize(InputStream stream) {
		if( stream == null ) throw new NullPointerException("stream is null");
		return (ERDiagram)xstream.fromXML(stream);
	}
}
