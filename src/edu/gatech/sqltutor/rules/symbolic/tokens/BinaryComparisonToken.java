package edu.gatech.sqltutor.rules.symbolic.tokens;

import com.akiban.sql.parser.NodeTypes;
import com.google.common.base.Joiner;

import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.ValueType;

public class BinaryComparisonToken 
		extends ChildContainerToken implements ISymbolicToken, IHasValueType {
	
	protected String operator;
	protected ValueType valueType = ValueType.UNKNOWN;
	
	public BinaryComparisonToken(BinaryComparisonToken token) {
		super(token);
		this.operator = token.operator;
	}

	public BinaryComparisonToken(String operator, PartOfSpeech pos) {
		super(pos);
		this.operator = operator;
	}
	
	public BinaryComparisonToken(String operator) {
		super(PartOfSpeech.QUANTIFIER_PHRASE);
		this.operator = operator;
	}
	
	public BinaryComparisonToken(SQLToken sqlToken) {
		this(sqlToken.getAstNode().getNodeType());
		if( sqlToken instanceof IHasValueType )
			setValueType(((IHasValueType)sqlToken).getValueType());
	}
	
	public BinaryComparisonToken(int nodeType) {
		super(PartOfSpeech.QUANTIFIER_PHRASE);
		switch( nodeType ) {
			case NodeTypes.BINARY_EQUALS_OPERATOR_NODE: 
				operator = "="; break;
			case NodeTypes.BINARY_GREATER_EQUALS_OPERATOR_NODE:
				operator = ">="; break;
			case NodeTypes.BINARY_GREATER_THAN_OPERATOR_NODE:
				operator = ">"; break;
			case NodeTypes.BINARY_LESS_EQUALS_OPERATOR_NODE:
				operator = "<="; break;
			case NodeTypes.BINARY_LESS_THAN_OPERATOR_NODE:
				operator = "<"; break;
			case NodeTypes.BINARY_NOT_EQUALS_OPERATOR_NODE:
				operator = "!="; break;
			default:
				throw new SymbolicException("Unknown node type: " + nodeType);
		}
	}
	
	public String getOperator() {
		return operator;
	}
	
	public void setOperator(String operator) {
		this.operator = operator;
	}

	@Override
	public SymbolicType getType() {
		return SymbolicType.BINARY_COMPARISON;
	}
	
	@Override
	public ValueType getValueType() {
		return valueType;
	}
	
	@Override
	public void setValueType(ValueType valueType) {
		if( valueType == null )
			valueType = ValueType.UNKNOWN;
		this.valueType = valueType;
	}
	
	@Override
	protected void acceptOrThrow(ISymbolicToken token) {
		super.acceptOrThrow(token);
		if( children.size() >= 2 )
			throw new SymbolicException(this + " already has two children.");
	}
	
	@Override
	public String toString() {
		return "{" + typeAndTag() + " operator='" + operator + 
			"': " + Joiner.on(", ").join(children) + "}";
	}
}
