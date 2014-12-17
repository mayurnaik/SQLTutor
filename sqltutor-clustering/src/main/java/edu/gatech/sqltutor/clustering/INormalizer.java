package edu.gatech.sqltutor.clustering;

import com.akiban.sql.parser.Visitable;
import com.akiban.sql.parser.Visitor;

public interface INormalizer extends Visitor {
	public boolean normalize(Visitable v);
}
