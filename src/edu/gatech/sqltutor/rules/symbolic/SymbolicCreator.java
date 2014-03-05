package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.NumericConstantNode;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.ValueNode;
import com.google.common.collect.Lists;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.SQLMaps;
import edu.gatech.sqltutor.rules.SQLState;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SQLPredicates;
import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.rules.symbolic.tokens.AndToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeListToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.BinaryComparisonToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralsToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.NumberToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.RootToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SelectToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.WhereToken;
import edu.gatech.sqltutor.rules.util.GetChildrenVisitor;

/**
 * Creates the initial symbolic structure.
 */
public class SymbolicCreator {
	private static final Logger _log = LoggerFactory.getLogger(SymbolicCreator.class);
	
	private SQLState sqlState;
	private SQLMaps sqlMaps;

	public SymbolicCreator(SQLState sqlState, SQLMaps sqlMaps) {
		if( sqlState == null ) throw new NullPointerException("sqlState is null");
		if( sqlMaps == null ) throw new NullPointerException("sqlMaps is null");
		this.sqlState = sqlState;
		this.sqlMaps = sqlMaps;
	}

	public RootToken makeSymbolic() {
		RootToken root = new RootToken();
		root.addChild(new SelectToken());
		
		addResultColumnsAndTables(root);
		addWhereClause(root);
		
		return root;
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
				String attrName =
					fromTable.getOrigTableName().getTableName() + "." + resultColumn.getExpression().getColumnName();
				ERAttribute erAttr = erMapping.getAttribute(attrName);
				if( erAttr == null )
					_log.warn("No attribute for name {}", attrName);
				AttributeToken attr = new AttributeToken(erAttr);
				attrList.addChild(attr);
			}

			seq.addChild(attrList);

			// "of each" {entity}
			LiteralsToken literals = new LiteralsToken(PartOfSpeech.PREPOSITIONAL_PHRASE);
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
			case NodeTypes.BINARY_EQUALS_OPERATOR_NODE:
			case NodeTypes.BINARY_GREATER_EQUALS_OPERATOR_NODE:
			case NodeTypes.BINARY_GREATER_THAN_OPERATOR_NODE:
			case NodeTypes.BINARY_LESS_EQUALS_OPERATOR_NODE:
			case NodeTypes.BINARY_LESS_THAN_OPERATOR_NODE:
			case NodeTypes.BINARY_NOT_EQUALS_OPERATOR_NODE:
				token = new BinaryComparisonToken(nodeType);
				break;
			case NodeTypes.COLUMN_REFERENCE:
				token = createColumnReferenceToken((ColumnReference)node);
				break;
			default:
				if( node instanceof NumericConstantNode ) {
					token = new NumberToken((Number)((NumericConstantNode)node).getValue());
				} else {
					throw new SymbolicException("FIXME: Unhandled node type: " + node.getClass().getSimpleName());
				}
		}
		
		parentToken.addChild(token);
		for( QueryTreeNode childNode: childNodes )
			addChildTokens(token, childNode);
	}
	
	private ISymbolicToken createColumnReferenceToken(ColumnReference colRef) {
		Integer nodeId = sqlState.getSqlFacts().getNodeMap().getObjectId(colRef);
		IQuery query = Factory.BASIC.createQuery(
			literal(SQLPredicates.columnName, nodeId, "?columnName"),
			literal(SQLPredicates.tableAlias, nodeId, "?tableAlias"),
			literal(SQLPredicates.tableAlias, "?tableId", "?tableAlias"),
			literal(SQLPredicates.tableName, "?tableId", "?tableName")
		);
		List<IVariable> bindings = new ArrayList<IVariable>(4);
		IKnowledgeBase kb = sqlState.getKnowledgeBase();
		
		IRelation relation = null;
		try {
			relation = kb.execute(query, bindings);
		} catch( EvaluationException e ) {
			throw new SymbolicException(e);
		}
		
		if( relation.size() != 1 )
			throw new SymbolicException("Too many or too few results: " + relation);
		
		RelationExtractor ext = new RelationExtractor(bindings);
		ITuple result = relation.get(0);
		
		String tableCol = ((IStringTerm)ext.getTerm("?tableName", result)).getValue() + "." + colRef.getColumnName();
		ERMapping erMapping = sqlState.getErMapping();
		
		ERAttribute attr = erMapping.getAttribute(tableCol);
		if( attr == null )
			throw new SymbolicException("Could not get attribute for " + tableCol);
		
		AttributeToken token = new AttributeToken(attr);
		return token;
	}
}
