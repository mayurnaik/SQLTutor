package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultColumn;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;

public class AttributeListToken 
		extends ChildContainerToken implements ISymbolicToken {
	public AttributeListToken(AttributeListToken token) {
		super(token);
	}
	
	public AttributeListToken() {
		super(PartOfSpeech.NOUN_PHRASE);
	}
	
	@Override
	public SymbolicType getType() {
		return SymbolicType.ATTRIBUTE_LIST;
	}

	@Override
	protected boolean canAcceptChild(ISymbolicToken tok) {
		switch( tok.getType() ) {
			case ATTRIBUTE:
			case LITERAL:
			case ALL_ATTRIBUTES:
				return true;
			case SQL_AST: {
				SQLToken sqlToken = ((SQLToken)tok);
				QueryTreeNode astNode = sqlToken.getAstNode();
				return astNode instanceof ResultColumn || astNode instanceof ColumnReference;
			}
			default:
				return false;
		}
	}
}
