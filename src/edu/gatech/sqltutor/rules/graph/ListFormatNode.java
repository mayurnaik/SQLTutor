package edu.gatech.sqltutor.rules.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFormatNode extends LabelNode {

	public ListFormatNode() {
	}

	@Override
	public List<List<String>> getChoices() {
		
		if( outputChoices == null ) {
			// conjunct each list of terms into a single string
			List<List<String>> choices = new ArrayList<List<String>>(childChoices.size());
			for( List<String> choiceList: childChoices ) {
				choices.add(Collections.singletonList(conjunctTerms(choiceList)));
			}
			childChoices = choices;
		}
		
		// merge with local choices as usual
		return super.getChoices();
	}
	
	/**
	 * Append the English conjunction of a list of terms to <code>result</code>.
	 * <p>Produces:<br />
	 * 1: &lt;attr&gt; <br />
	 *	2: &lt;attr1&gt; and &lt;attr2&gt;<br />
	 *	3+: &lt;attr1&gt;, &lt;attr2&gt;, ..., and &lt;attrN&gt;<br />
	 * </p>
	 * 
	 * @param result
	 * @param terms
	 */
	private String conjunctTerms(List<String> terms) {
		StringBuilder result = new StringBuilder();
		// 1: <attr>
		// 2: <attr1> and <attr2>
		// 3+: <attr1>, <attr2>, ..., and <attrN> 
		for( int i = 0, ilen = terms.size(); i < ilen; ++i ) {
			String term = terms.get(i);
			if( i != 0 ) {
				if( i != ilen - 1 ) {
					result.append(',');
				} else {
					if( i != 1 )
						result.append(',');
					result.append(" and");
				}
				result.append(' ');
			}
			result.append(term);
		}
		
		return result.toString();
	}
}
