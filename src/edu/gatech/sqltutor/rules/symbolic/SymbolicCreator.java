package edu.gatech.sqltutor.rules.symbolic;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.ValueNode;
import com.google.common.collect.Lists;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.SQLMaps;
import edu.gatech.sqltutor.rules.SQLState;
import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.rules.symbolic.tokens.AndToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeListToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralsToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.RootToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SelectToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.WhereToken;

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
	
	private void addWhereClause(RootToken root) {
		// now the WHERE clause
		ValueNode where = sqlState.getAst().getWhereClause();
		if( where == null )
			return;
		
		root.addChild(new WhereToken());
		_log.info(Markers.SYMBOLIC, "Have WHERE clause to convert: {}", QueryUtils.nodeToString(where));
	}
}
