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
	
	public static class MaxConverter implements Converter {
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
	
	private int min;
	
	@XStreamConverter(value=MaxConverter.class)
	private int max;
	
	private String label;

	
	public EREdgeConstraint(int min, int max) {
		this(min, max, null);
	}
	
	public EREdgeConstraint(int min, int max, String label) {
		if( min < 0 )
			throw new IllegalArgumentException("Min must be non-negative: " + min);
		if( max >= 0 && max < min )
			throw new IllegalArgumentException("Max must not be less than min: (min=" + min + ", max=" + max + ")");
		
		this.min = min;
		this.max = max;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
	@Override
	public String toString() {
		return "(" + min + "," + (max < 0 ? 'N' : max) + ")" + 
			(label == null ? "" : " " + label);
	}
}
