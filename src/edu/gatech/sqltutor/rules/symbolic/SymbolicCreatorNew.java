package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.CharConstantNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.NumericConstantNode;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.ValueNode;
import com.akiban.sql.parser.ValueNodeList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.SQLMaps;
import edu.gatech.sqltutor.rules.SQLState;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SQLFacts.NodeMap;
import edu.gatech.sqltutor.rules.datalog.iris.SQLPredicates;
import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.rules.symbolic.tokens.AllAttributesToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.AndToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeListToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.BetweenToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.BinaryComparisonToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.NumberToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.OrToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.RootToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SelectToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.WhereToken;
import edu.gatech.sqltutor.rules.util.GetChildrenVisitor;

/**
 * Creates the initial symbolic structure.
 */
public class SymbolicCreatorNew {
	private static final Logger _log = LoggerFactory.getLogger(SymbolicCreatorNew.class);
	
	private SQLState sqlState;
	private SQLMaps sqlMaps;

	public SymbolicCreatorNew(SQLState sqlState, SQLMaps sqlMaps) {
		if( sqlState == null ) throw new NullPointerException("sqlState is null");
		if( sqlMaps == null ) throw new NullPointerException("sqlMaps is null");
		this.sqlState = sqlState;
		this.sqlMaps = sqlMaps;
	}

	public RootToken makeSymbolic() {
		RootToken root = new RootToken();
		
		SelectNode select = sqlState.getAst();
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
				SQLToken childToken = new SQLToken(childNode);
				token.addChild(childToken);
				tokens.push(childToken);
			}
		}
		_log.info(Markers.SYMBOLIC, "Symbolic state directly from AST: {}", SymbolicUtil.prettyPrint(selectToken));
		
		root.addChild(new SelectToken());
		
		addResultColumnsAndTables(root);
		addWhereClause(root);
		
		return root;
	}
	
	private void addRecursiveTokens(SQLToken parent) {
		
	}
	
	private void addResultColumnsAndTables(RootToken root) {
		ERMapping erMapping = sqlState.getErMapping();
		
		// create an attribute list for each group of columns that go with a table reference
		List<ISymbolicToken> attrLists = Lists.newLinkedList();
		for( Map.Entry<FromTable, Collection<ResultColumn>> entry: 
				sqlMaps.getFromToResult().asMap().entrySet() ) {
			FromTable fromTable = entry.getKey();
			Collection<ResultColumn> resultColumns = entry.getValue();

			SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);

			// list of attributes
			AttributeListToken attrList = new AttributeListToken();
			for( ResultColumn resultColumn : resultColumns ) {
				String attrName;
				AttributeToken attr;
				if(resultColumn.getNodeType() == NodeTypes.ALL_RESULT_COLUMN) {
					// @see com.akiban.sql.parser.AllResultColumn.java
					attr = new AllAttributesToken(fromTable.getOrigTableName().getTableName());
				} else {
					attrName =
						fromTable.getOrigTableName().getTableName() + 
						"." + resultColumn.getExpression().getColumnName();
					ERAttribute erAttr = erMapping.getAttribute(attrName);
					if( erAttr == null )
						throw new SQLTutorException("No attribute for name " + attrName);
					attr = new AttributeToken(erAttr);
				}

				attrList.addChild(attr);
			}

			seq.addChild(attrList);

			// "of each" {entity}
			SequenceToken literals = new SequenceToken(PartOfSpeech.PREPOSITIONAL_PHRASE);
			LiteralToken of = new LiteralToken("of", PartOfSpeech.PREPOSITION_OR_SUBORDINATING_CONJUNCTION);
			LiteralToken each = new LiteralToken("each", PartOfSpeech.DETERMINER);
			literals.addChild(of);
			literals.addChild(each);
			seq.addChild(literals);

			TableEntityToken table = new TableEntityToken(fromTable);
			seq.addChild(table);

			attrLists.add(seq);
		}

		if( attrLists.size() == 1 ) {
			root.addChild(attrLists.get(0));
		} else {
			AndToken and = new AndToken();
			for( ISymbolicToken attrList : attrLists )
				and.addChild(attrList);
			root.addChild(and);
		}
	}
	
	private void addWhereClause(final RootToken root) {
		// now the WHERE clause
		ValueNode where = sqlState.getAst().getWhereClause();
		if( where == null )
			return;
		
		_log.info(Markers.SYMBOLIC, "Have WHERE clause to convert: {}", QueryUtils.nodeToString(where));
		
		root.addChild(new WhereToken());
		
		addChildTokens(root, where);
	}
	
	private GetChildrenVisitor childVisitor = new GetChildrenVisitor();
	private void addChildTokens(ISymbolicToken parentToken, QueryTreeNode node) {
		
		List<QueryTreeNode> childNodes = childVisitor.getChildren(node);
		
		ISymbolicToken token = null;
		int nodeType = node.getNodeType();
		switch(nodeType) {
			case NodeTypes.AND_NODE:
				token = new AndToken();
				break;
			case NodeTypes.OR_NODE:
				token = new OrToken();
				break;
			case NodeTypes.BINARY_EQUALS_OPERATOR_NODE:
			case NodeTypes.BINARY_GREATER_EQUALS_OPERATOR_NODE:
			case NodeTypes.BINARY_GREATER_THAN_OPERATOR_NODE:
			case NodeTypes.BINARY_LESS_EQUALS_OPERATOR_NODE:
			case NodeTypes.BINARY_LESS_THAN_OPERATOR_NODE:
			case NodeTypes.BINARY_NOT_EQUALS_OPERATOR_NODE:
				token = new BinaryComparisonToken(nodeType);
				break;
			case NodeTypes.BETWEEN_OPERATOR_NODE:
				token = new BetweenToken();
				childNodes = flattenBetweenNodeChildren(childNodes);
				break;
			case NodeTypes.COLUMN_REFERENCE:
				token = createColumnReferenceToken((ColumnReference)node);
				break;
			default:
				if( node instanceof NumericConstantNode ) {
					token = new NumberToken((Number)((NumericConstantNode)node).getValue());
				} else if ( node instanceof CharConstantNode ) {
					token = new SequenceToken(PartOfSpeech.NOUN_PHRASE);
					token.addChild(new LiteralToken("\"", PartOfSpeech.QUOTE_LEFT));
					token.addChild(new LiteralToken(
						((CharConstantNode)node).getValue().toString(), 
						PartOfSpeech.NOUN_PHRASE // FIXME part of speech not actually known
					));
					token.addChild(new LiteralToken("\"", PartOfSpeech.QUOTE_RIGHT));
				} else {
					throw new SymbolicException("FIXME: Unhandled node type: " + node.getClass().getSimpleName());
				}
		}
		
		parentToken.addChild(token);
		for( QueryTreeNode childNode: childNodes )
			addChildTokens(token, childNode);
	}
	
	private ISymbolicToken createColumnReferenceToken(ColumnReference colRef) {
		NodeMap nodeMap = sqlState.getSqlFacts().getNodeMap();
		Integer nodeId = nodeMap.getObjectId(colRef);
		IQuery query = Factory.BASIC.createQuery(
			literal(SQLPredicates.columnName, nodeId, "?columnName"),
			literal(SQLPredicates.tableAlias, nodeId, "?tableAlias"),
			literal(SQLPredicates.tableAlias, "?tableId", "?tableAlias"),
			literal(SQLPredicates.tableName, "?tableId", "?tableName")
		);
		
		RelationExtractor ext = IrisUtil.executeQuery(query, sqlState);
		IRelation relation = ext.getRelation();
		if( relation.size() != 1 )
			throw new SymbolicException("Too many or too few results: " + relation);
		ext.nextTuple();
		
		String tableCol = ext.getString("?tableName") + "." + colRef.getColumnName();
		ERMapping erMapping = sqlState.getErMapping();
		
		ERAttribute attr = erMapping.getAttribute(tableCol);
		if( attr == null )
			throw new SymbolicException("Could not get attribute for " + tableCol);
		
		AttributeToken token = new AttributeToken(attr);
		
		FromTable fromTable = this.sqlMaps.getTableAliases().get(ext.getString("?tableAlias"));
		SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);
		seq.addChild(new TableEntityToken(fromTable));
		seq.addChild(new LiteralToken("'s", PartOfSpeech.POSSESSIVE_ENDING));
		seq.addChild(token);
		
		
		return seq;
	}
	
	private List<QueryTreeNode> flattenBetweenNodeChildren(List<QueryTreeNode> children) {
		try {
			QueryTreeNode leftSide = children.get(0);
			ValueNodeList nodeList = (ValueNodeList)children.get(1);
			return ImmutableList.<QueryTreeNode>builder().add(leftSide).addAll(nodeList).build();
		} catch( IndexOutOfBoundsException e ) {
			throw new SymbolicException("Unexpected number of children.", e);
		} catch( ClassCastException e ) {
			throw new SymbolicException("Expected ValueNodeList in second position.", e);
		}
		
	}
}
