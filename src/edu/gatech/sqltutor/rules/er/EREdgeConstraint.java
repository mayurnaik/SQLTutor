package edu.gatech.sqltutor.rules.er;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A constraint on a relationship edge, e.g.:
 * <p>
 * <tt>(0,N) Supervisor</tt>
 * </p>
 * 
 * Min must be a non-negative integer.  If max is negative, 
 * this is equivalent to N.
 */
public class EREdgeConstraint {
	public static final int ANY_CARDINALITY = -1;
	
	public static class CardinalityConverter implements Converter {
		@Override
		public boolean canConvert(
				@SuppressWarnings("rawtypes") Class clazz) {
			return Integer.class.equals(clazz);
		}
		
		@Override
		public void marshal(Object max, HierarchicalStreamWriter writer, 
				MarshallingContext context) {
			Integer maxInt = (Integer)max;
			if( maxInt < 0 )
				writer.setValue("N");
			else
				writer.setValue(maxInt.toString());
		}
		
		@Override
		public Object unmarshal(HierarchicalStreamReader reader, 
				UnmarshallingContext context) {
			String value = reader.getValue();
			if( "N".equalsIgnoreCase(value) )
				return Integer.valueOf(-1);
			return Integer.valueOf(value);
		}
	}
	
	private String label;
	
	@XStreamConverter(value=CardinalityConverter.class)
	private int cardinality;

	public EREdgeConstraint(int cardinality) {
		this.cardinality = Math.max(-1, cardinality);
	}
	
	public EREdgeConstraint(int cardinality, String label) {
		this.label = label;
		this.cardinality = Math.max(-1, cardinality);
	}
	public String getLabel() {
		return label;
	}
	
	public int getCardinality() {
		return cardinality;
	}
	
	@Override
	public String toString() {
		return "[" + (cardinality < 0 ? 'N' : cardinality) + "]" + 
			(label == null ? "" : " " + label);
	}
}
