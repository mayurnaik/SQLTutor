package edu.gatech.sqltutor.rules.symbolic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.BetweenOperatorNode;
import com.akiban.sql.parser.BinaryRelationalOperatorNode;
import com.akiban.sql.parser.CharConstantNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.ConstantNode;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.NumericConstantNode;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SelectNode;

import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.SymbolicState;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.RootToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLNounToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLNumberToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLStringToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLValueToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.util.GetChildrenVisitor;

/**
 * Creates the initial symbolic structure.
 */
public class SymbolicCreator {
	private static final Logger _log = LoggerFactory.getLogger(SymbolicCreator.class);
	
	private SelectNode select;
	
	private GetChildrenVisitor childVisitor = new GetChildrenVisitor();
	
	private Map<String, Integer> nextVar;

	public SymbolicCreator(SelectNode select) {
		if( select == null ) throw new NullPointerException("select is null");
		this.select = select;
	}

	public SymbolicState makeSymbolic() {
		SymbolicState state = new SymbolicState();
		nextVar = new HashMap<String, Integer>();
		List<ISymbolicToken> unrootedTokens = new ArrayList<ISymbolicToken>();
		RootToken rootToken = new RootToken();
		
		SQLToken selectToken = new SQLToken(select);
		Stack<SQLToken> tokens = new Stack<SQLToken>();
		tokens.push(selectToken);
		while( !tokens.isEmpty() ) {
			SQLToken token = tokens.pop();
			List<QueryTreeNode> childNodes = childVisitor.getChildren(token.getAstNode());
			
			for( QueryTreeNode childNode: childNodes ) {
				
				SQLToken childToken;
				if( childNode instanceof ColumnReference ) {
					childToken = new SQLNounToken(childNode);
				} else if( childNode instanceof FromTable ) {
					FromTable fromTable = (FromTable)childNode;
					childToken = new SQLNounToken(childNode);
					
					// make an unrooted entity token for this table
					// FIXME what about relationship tables?
					TableEntityToken tableEntity = new TableEntityToken(fromTable);
					tableEntity.setId(getNextEntityId(fromTable.getOrigTableName().getTableName()));
					tableEntity.setConjunctScope(select); // FIXME should this be computed?
					unrootedTokens.add(tableEntity);
				} else if( childNode instanceof BinaryRelationalOperatorNode 
						|| childNode instanceof BetweenOperatorNode
						|| childNode instanceof ConstantNode) {
					childToken = new SQLValueToken(childNode);
				} else {
					childToken = new SQLToken(childNode);
				}
				token.addChild(childToken);
				tokens.push(childToken);
			}
		}
		if( _log.isDebugEnabled(Markers.SYMBOLIC) )
			_log.debug(Markers.SYMBOLIC, "Symbolic state directly from AST: {}", SymbolicUtil.prettyPrint(selectToken));
		
		rootToken.addChild(selectToken);
		
		state.setRootToken(rootToken);
		state.setExtraTokens(unrootedTokens);
		return state;
	}
	
	private String getNextEntityId(String tableName) {
		String firstChar = tableName.substring(0,1).toLowerCase();
		Integer nextInt = nextVar.get(firstChar);
		if( nextInt == null ) {
			nextVar.put(firstChar, Integer.valueOf(2));
			return firstChar;
		}
		nextVar.put(firstChar, nextInt + 1);
		return firstChar + nextInt;
	}
}
