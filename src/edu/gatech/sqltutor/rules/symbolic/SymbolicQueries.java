package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;

import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.rules.SymbolicState;
import edu.gatech.sqltutor.rules.datalog.iris.ERPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SQLPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicFacts.NodeMap;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicFacts.TokenMap;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.er.EREntity;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;
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
	
	/**
	 * Returns all the <code>{TABLE_ENTITY_REF}</code> tokens that reference a 
	 * particular <code>{TABLE_ENTITY}</code>.
	 * 
	 * @param tableEntity the table entity
	 * @return the (possibly empty) list of references
	 */
	public List<TableEntityRefToken> getTableEntityReferences(TableEntityToken tableEntity) {
		if( tableEntity == null ) throw new NullPointerException("tableEntity is null");
		
		TokenMap tokenMap = state.getSymbolicFacts().getTokenMap();
		Integer tableEntityId = tokenMap.getObjectId(tableEntity);
		IQuery query = Factory.BASIC.createQuery(
			literal(SymbolicPredicates.type, "?ref", SymbolicType.TABLE_ENTITY_REF),
			literal(SymbolicPredicates.refsTableEntity, "?ref", tableEntityId)
		);
		
		RelationExtractor ext = IrisUtil.executeQuery(query, state);
		List<TableEntityRefToken> refs = new ArrayList<TableEntityRefToken>(ext.getRelation().size());
		while( ext.nextTuple() ) {
			TableEntityRefToken ref = ext.getToken("?ref");
			refs.add(ref);
		}
		return refs;
	}
	
	/**
	 * <b>FIXME</code> Returns the entity associated with the token, which should be kept with that token instead.
	 * @param tableEntity
	 * @return
	 */
	@Deprecated
	public EREntity getReferencedEntity(TableEntityToken tableEntity) {
		if( tableEntity == null ) throw new NullPointerException("tableEntity is null");
		
		String tableName = tableEntity.getTable().getOrigTableName().getTableName();
		IQuery query = Factory.BASIC.createQuery(
			literal(ERPredicates.erTableRefsEntity, tableName, "?entityName")
		);
		RelationExtractor ext = IrisUtil.executeQuery(query, state);
		ext.nextTuple();
		
		String entityName = ext.getString("?entityName");
		EREntity entity = state.getErDiagram().getEntity(entityName);
		return entity;
	}
}
