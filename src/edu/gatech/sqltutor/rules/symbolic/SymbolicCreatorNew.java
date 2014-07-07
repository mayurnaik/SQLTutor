package edu.gatech.sqltutor.rules.symbolic;

import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SelectNode;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.symbolic.tokens.RootToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLNounToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;
import edu.gatech.sqltutor.rules.util.GetChildrenVisitor;

/**
 * Creates the initial symbolic structure.
 */
public class SymbolicCreatorNew {
	private static final Logger _log = LoggerFactory.getLogger(SymbolicCreatorNew.class);
	
	private SelectNode select;
	
	private GetChildrenVisitor childVisitor = new GetChildrenVisitor();

	public SymbolicCreatorNew(SelectNode select) {
		if( select == null ) throw new NullPointerException("select is null");
		this.select = select;
	}

	public RootToken makeSymbolic() {
		RootToken root = new RootToken();
		
		SQLToken selectToken = new SQLToken(select);
		Stack<SQLToken> tokens = new Stack<SQLToken>();
		tokens.push(selectToken);
		while( !tokens.isEmpty() ) {
			List<QueryTreeNode> childNodes;
			SQLToken token = tokens.pop();
			
			try {
				childVisitor.reset();
				token.getAstNode().accept(childVisitor);
				childNodes = childVisitor.getChildren();
			} catch( StandardException e ) { throw new SQLTutorException(e); }
			
			for( QueryTreeNode childNode: childNodes ) {
				
				SQLToken childToken;
				if( childNode instanceof ColumnReference || childNode instanceof FromTable ) {
					childToken = new SQLNounToken(childNode);
				} else {
					childToken = new SQLToken(childNode);
				}
				token.addChild(childToken);
				tokens.push(childToken);
			}
		}
		_log.info(Markers.SYMBOLIC, "Symbolic state directly from AST: {}", SymbolicUtil.prettyPrint(selectToken));
		
		root.addChild(selectToken);
		
		return root;
	}
	
}
