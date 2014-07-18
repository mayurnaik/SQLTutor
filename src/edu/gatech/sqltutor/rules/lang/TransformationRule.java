package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.AllResultColumn;
import com.akiban.sql.parser.CharConstantNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.NumericConstantNode;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.TableName;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SQLPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
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
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLNounToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLNumberToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SelectToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.WhereToken;

public class TransformationRule extends StandardSymbolicRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(TransformationRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?root", SymbolicType.ROOT),
		literal(SymbolicPredicates.parentOf, "?root", "?select", 0),
		literal(SymbolicPredicates.type, "?select", SymbolicType.SQL_AST),
		literal(SQLPredicates.nodeHasType, "?select", "SelectNode")
	);

	public TransformationRule() {
	}

	public TransformationRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		ext.nextTuple(); // just one result
		
		RootToken root = ext.getToken("?root");
		SQLToken select = ext.getToken("?select");
		root.getChildren().clear(); // delete existing children
		
		List<ISymbolicToken> selectChildren = select.getChildren();
		SQLToken resultColumns = (SQLToken)selectChildren.get(0),
			fromList = (SQLToken)selectChildren.get(1);
		
		root.addChild(new SelectToken());
		
		addResultColumnsAndTables(root, resultColumns, fromList);
		if( selectChildren.size() >= 3 )
			addWhereClause(root, (SQLToken)selectChildren.get(2));
		
		if( _log.isDebugEnabled(Markers.SYMBOLIC) )
			_log.debug(Markers.SYMBOLIC, "State after transformation:\n{}", SymbolicUtil.prettyPrint(root));
		
		return true;
	}
	
	private String getTableName(ResultColumn col) {
		String tableName = col.getTableName();
		// bad implementation keeps table name differently in AllResultColumn subclass
		if( col instanceof AllResultColumn ) {
			TableName tableNameObj = col.getTableNameObject();
			if( tableNameObj != null )
				tableName = tableNameObj.getTableName();
		}
		return tableName;
	}
	
	private List<SQLToken> getResultColumnsForTableAlias(String tableAlias, SQLToken resultColumns) {
		List<SQLToken> columns = new ArrayList<SQLToken>();
		for( ISymbolicToken childToken: resultColumns.getChildren() ) {
			SQLToken child = (SQLToken)childToken;
			ResultColumn colRef = (ResultColumn)child.getAstNode();
			String columnTableName = getTableName(colRef);
			if( tableAlias.equals(columnTableName) )
				columns.add(child);
		}
		return columns;
	}
	
	private void addResultColumnsAndTables(RootToken root, 
			SQLToken resultColumns, SQLToken fromList) {
		ERMapping erMapping = state.getErMapping();
		
		List<ISymbolicToken> attrLists = Lists.newLinkedList();
		for( ISymbolicToken childToken: fromList.getChildren() ) {
			SQLNounToken tableToken = (SQLNounToken)childToken;
			if( !(tableToken.getAstNode() instanceof FromBaseTable) )
				throw new SymbolicException("Expected table node, not: " + tableToken);
			FromBaseTable fromTable = (FromBaseTable)tableToken.getAstNode();
			
			String tableAlias = fromTable.getExposedName();
			List<SQLToken> columnTokens = getResultColumnsForTableAlias(tableAlias, resultColumns);
			if( columnTokens.size() == 0 )
				continue;
			
			SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);

			// list of attributes
			AttributeListToken attrList = new AttributeListToken();
			for( SQLToken columnToken: columnTokens ) {
				ResultColumn resultColumn = (ResultColumn)columnToken.getAstNode();
				
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
			table.setSingularLabel(tableToken.getSingularLabel());
			table.setPluralLabel(tableToken.getPluralLabel());
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
	
	private void addWhereClause(final RootToken root, SQLToken where) {
		root.addChild(new WhereToken());
		
		addChildTokens(root, where);
	}
	
	private void addChildTokens(ISymbolicToken parentToken, ISymbolicToken token) {
		addChildTokens(parentToken, (SQLToken)token);
	}
	
	private void addChildTokens(ISymbolicToken parentToken, SQLToken sqlToken) {
		QueryTreeNode node = sqlToken.getAstNode();
		List<ISymbolicToken> childTokens = sqlToken.getChildren();
		
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
				childTokens = flattenBetweenNodeChildren(childTokens);
				break;
			case NodeTypes.COLUMN_REFERENCE:
				token = createColumnReferenceToken(sqlToken);
				break;
			default:
				if( node instanceof NumericConstantNode ) {
					NumberToken numToken;
					token = numToken = new NumberToken((Number)((NumericConstantNode)node).getValue());
					numToken.setNumericType(((SQLNumberToken)sqlToken).getNumericType());
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
		for( ISymbolicToken childToken: childTokens )
			addChildTokens(token, childToken);
	}
	
	private ISymbolicToken createColumnReferenceToken(SQLToken colRef) {
		Integer nodeId = state.getSymbolicFacts().getTokenMap().getObjectId(colRef);
		IQuery query = Factory.BASIC.createQuery(
			literal(SQLPredicates.columnName, nodeId, "?columnName"),
			literal(SQLPredicates.tableAlias, nodeId, "?tableAlias"),
			literal(SQLPredicates.tableAlias, "?tableId", "?tableAlias"),
			literal(SQLPredicates.tableName, "?tableId", "?tableName")
		);
		
		RelationExtractor ext = IrisUtil.executeQuery(query, state);
		IRelation relation = ext.getRelation();
		if( relation.size() != 1 )
			throw new SymbolicException("Too many or too few results: " + relation);
		ext.nextTuple();
		
		String tableCol = ext.getString("?tableName") + "." + ((ColumnReference)colRef.getAstNode()).getColumnName();
		ERMapping erMapping = state.getErMapping();
		
		ERAttribute attr = erMapping.getAttribute(tableCol);
		if( attr == null )
			throw new SymbolicException("Could not get attribute for " + tableCol);
		
		AttributeToken token = new AttributeToken(attr);
		
		SQLNounToken fromToken = ext.getToken("?tableId");
		FromTable fromTable = (FromTable)fromToken.getAstNode();//this.sqlMaps.getTableAliases().get(ext.getString("?tableAlias"));
		SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);
		TableEntityToken tableEntity = new TableEntityToken(fromTable);
		tableEntity.setSingularLabel(fromToken.getSingularLabel());
		tableEntity.setPluralLabel(fromToken.getPluralLabel());
		seq.addChild(tableEntity);
		seq.addChild(new LiteralToken("'s", PartOfSpeech.POSSESSIVE_ENDING));
		seq.addChild(token);
		
		
		return seq;
	}
	
	private List<ISymbolicToken> flattenBetweenNodeChildren(List<ISymbolicToken> children) {
		try {
			ISymbolicToken leftSide = children.get(0),
				nodeList = children.get(1);
			return ImmutableList.<ISymbolicToken>builder().add(leftSide).addAll(nodeList.getChildren()).build();
		} catch( IndexOutOfBoundsException e ) {
			throw new SymbolicException("Unexpected number of children.", e);
		}
		
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.TRANSFORMATION);
	}

}
