package edu.gatech.sqltutor.clustering;

import com.akiban.sql.parser.NodeFactory;
import com.akiban.sql.parser.SQLParserContext;
import com.akiban.sql.parser.Visitor;

import edu.gatech.sqltutor.rules.util.ParserVisitorAdapter;

public abstract class ContextVisitorAdapter extends ParserVisitorAdapter
		implements Visitor {
	
	protected SQLParserContext context;
	protected NodeFactory nodeFactory;

	public ContextVisitorAdapter(SQLParserContext context) {
		if( context == null ) throw new NullPointerException("context is null");
		this.context = context;
		this.nodeFactory = context.getNodeFactory();
	}
}
