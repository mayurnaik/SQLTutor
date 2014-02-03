package edu.gatech.sqltutor.rules.datalog.iris;

import java.util.Arrays;
import java.util.List;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;

import edu.gatech.sqltutor.SQLTutorException;

/**
 * Extractor for terms of tuples in a relation that imposes 
 * an ordering on variables.
 */
public class RelationExtractor {
	private int[] positions;
	
	public RelationExtractor(List<IVariable> bindings, String... desired) {
		this(bindings, Arrays.asList(desired));
	}
	
	/**
	 * Create an extractor using the order of names in <code>desired</code>.
	 * Variable names need not include the leading <code>"?"</code>.
	 * 
	 * @param bindings the actual binding order
	 * @param desired  the desired binding order
	 */
	public RelationExtractor(List<IVariable> bindings, List<String> desired) {
		int size = desired.size();
		if( size != bindings.size() )
			throw new SQLTutorException("Binding vs size mismatch.");
		positions = new int[size];
		for( int i = 0; i < size; i++ ) {
			IVariable binding = bindings.get(i);
			int pos = desired.indexOf(binding.getValue());
			if( pos == -1 )
				pos = desired.indexOf(binding.toString());
			positions[pos] = i;
		}
	}
	
	/**
	 * Get the term corresponding to position <code>pos</code> in 
	 * the constructor's variable ordering.
	 */
	public ITerm getTerm(int pos, ITuple tuple) {
		return tuple.get(positions[pos]);
	}
}