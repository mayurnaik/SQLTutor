package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;

import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.rules.SymbolicState;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SQLPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicFacts.NodeMap;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;

/**
 * Common queries that are needed across various rules.
 */
public class SymbolicQueries {

	SymbolicState state;
	public SymbolicQueries(SymbolicState state) {
		this.state = state;
	}

	
	/**
	 * Returns the <code>{TABLE_ENTITY}</code> associated with a conjunct-scope.
	 * @param cscope the conjunct scope or <code>null</code> for the global scope
	 * @return the entity or <code>null</code> if none is associated
	 */
	public List<TableEntityToken> getTableEntitiesForScope(QueryTreeNode cscope) {
		NodeMap scopeMap = state.getSymbolicFacts().getScopeMap();
		Integer scopeId = cscope != null ? scopeMap.getObjectId(cscope) : 0;
		IQuery query = Factory.BASIC.createQuery(
			literal(SymbolicPredicates.type, "?tableEntity", SymbolicType.TABLE_ENTITY),
			literal(SymbolicPredicates.conjunctScope, "?tableEntity", scopeId)
		);
		RelationExtractor ext = IrisUtil.executeQuery(query, state);
		List<TableEntityToken> tokens = new ArrayList<TableEntityToken>(ext.getRelation().size());
		while( ext.nextTuple() )
			tokens.add(ext.<TableEntityToken>getToken("?tableEntity"));
		return tokens;
	}
	
	/**
	 * Returns the <code>{TABLE_ENTITY}</code> associated with a conjunct-scope.
	 * @param cscope the conjunct scope or <code>null</code> for the global scope
	 * @return the entity or <code>null</code> if none is associated
	 */
	public TableEntityToken getTableEntityForScope(String tableAlias, QueryTreeNode cscope) {
		if( tableAlias == null ) throw new NullPointerException("tableAlias is null");
		NodeMap scopeMap = state.getSymbolicFacts().getScopeMap();
		Integer scopeId = cscope != null ? scopeMap.getObjectId(cscope) : 0;
		IQuery query = Factory.BASIC.createQuery(
			literal(SymbolicPredicates.type, "?tableEntity", SymbolicType.TABLE_ENTITY),
			literal(SymbolicPredicates.conjunctScope, "?tableEntity", scopeId),
			literal(SymbolicPredicates.refsTable, "?tableEntity", "?table"),
			literal(SQLPredicates.tableAlias, "?table", tableAlias)
		);
		RelationExtractor ext = IrisUtil.executeQuery(query, state);
		if( !ext.nextTuple() )
			return null;
		return (TableEntityToken)ext.getToken("?tableEntity");
	}
	
	/**
	 * Returns the <code>{TABLE_ENTITY}</code> associated with a conjunct-scope.
	 * @param cscope the conjunct scope or <code>null</code> for the global scope
	 * @return the entity or <code>null</code> if none is associated
	 */
	public TableEntityToken getTableEntityForScope(FromBaseTable fromTable, QueryTreeNode cscope) {
		if( fromTable == null ) throw new NullPointerException("fromTable is null");
		return getTableEntityForScope(fromTable.getExposedName(), cscope);
	}
}
