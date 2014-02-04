package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.api.terms.concrete.IIntegerTerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.BinaryRelationalOperatorNode;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.ValueNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.ISQLTranslationRule;
import edu.gatech.sqltutor.rules.SQLState;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SQLFacts;
import edu.gatech.sqltutor.rules.datalog.iris.SQLPredicates;
import edu.gatech.sqltutor.rules.er.EREdgeConstraint;
import edu.gatech.sqltutor.rules.er.ERRelationship;
import edu.gatech.sqltutor.rules.er.mapping.ERForeignKeyJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap;
import edu.gatech.sqltutor.rules.er.mapping.ERLookupTableJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.util.Pair;

public class JoinLabelRule2 extends AbstractSQLRule implements ISQLTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(JoinLabelRule2.class);
	
	// rules defined statically
	private static final IPredicate joinRuleFK = Factory.BASIC.createPredicate("joinRuleFK", 5);
	private static final IPredicate joinRuleLookup = Factory.BASIC.createPredicate("joinRuleLookup", 10);

	private boolean isInitialized = false;
	private Stack<ERForeignKeyJoin> fkJoins;
	private Stack<ERLookupTableJoin> lookupJoins;

	public JoinLabelRule2() {
	}
	
	@Override
	public boolean apply(SQLState state) {
		this.state = state;
		
		try {
			if( !isInitialized ) {
				findRelationships(state.getErMapping());
				isInitialized = true;
			}
			
			if( detectFKJoin() )
				return true;
			
			if( detectLookupJoins() )
				return true;
			
			return false;
		} finally {
			this.state = null;
		}
	}
	
	private static ILiteral tableName(String var, String name) {
		return literal(SQLPredicates.tableName, var, name);
	}
	
	private boolean detectFKJoin() {
		SQLFacts facts = state.getSqlFacts();
		
		while( !fkJoins.isEmpty() ) {
			ERForeignKeyJoin join = fkJoins.pop();
			Pair<String,String> pk = QueryUtils.splitKeyParts(join.getKeyPair().getPrimaryKey());
			Pair<String,String> fk = QueryUtils.splitKeyParts(join.getKeyPair().getForeignKey());
			ERRelationship rel = state.getErMapping().getRelationship(join);
			if( rel == null ) {
				throw new SQLTutorException("No relationship for join: " + join);
			}

			
			IQuery query = Factory.BASIC.createQuery(
				literal(joinRuleFK, 
					"?t1", pk.getSecond(), "?t2", fk.getSecond(), "?eq"),
				tableName("?t1", pk.getFirst()),
				tableName("?t2", fk.getFirst())
			);
			List<IVariable> bindings = new ArrayList<IVariable>(3);
			IRelation result = null;
			try {
				result = state.getKnowledgeBase().execute(query, bindings);
			} catch( EvaluationException e ) {
				throw new SQLTutorException(e);
			}
			
			_log.info("Result: {}", result);
			_log.info("Bindings: {}", bindings);
			if( result.size() == 0 )
				continue;
			if( result.size() > 1 ) _log.warn("More than one result, only using first: {}", result);
			
			RelationExtractor ext = new RelationExtractor(bindings);
			ext.setSqlFacts(facts);
			ITuple first = result.get(0);
			FromBaseTable t1Table = (FromBaseTable)ext.getNode("?t1", first);
			FromBaseTable t2Table = (FromBaseTable)ext.getNode("?t2", first);
			BinaryRelationalOperatorNode binop = (BinaryRelationalOperatorNode)ext.getNode("?eq", first);
			
			_log.info("t1Table: {}\nt2Table: {}\nbinop: {}", t1Table, t2Table, binop);

			_log.info("Matched on relationship: {}", rel.getFullName());
			EREdgeConstraint leftConstraint = rel.getLeftEdge().getConstraint();
			EREdgeConstraint rightConstraint = rel.getRightEdge().getConstraint();
			
			// TODO actually apply the rule
			_log.info("\nApply {} to table {}\nApply {} to table {}", 
				leftConstraint.getLabel(), t1Table, rightConstraint.getLabel(), t2Table);
			
			SelectNode select = state.getAst();
			if( _log.isDebugEnabled() ) _log.debug("Original query state: {}", QueryUtils.nodeToString(select));
			deleteCondition(binop);
			if( _log.isDebugEnabled() ) _log.debug("New query state: {}", QueryUtils.nodeToString(select));
			
		}
		return false;
	}
	
	private boolean detectLookupJoins() {
		final boolean DEBUG = _log.isDebugEnabled();
		SQLFacts facts = state.getSqlFacts();
		while( !lookupJoins.isEmpty() ) {
			ERLookupTableJoin join = lookupJoins.pop();
			Pair<String,String> pk1 = QueryUtils.splitKeyParts(join.getLeftKeyPair().getPrimaryKey());
			Pair<String,String> fk1 = QueryUtils.splitKeyParts(join.getLeftKeyPair().getForeignKey());
			Pair<String,String> pk2 = QueryUtils.splitKeyParts(join.getRightKeyPair().getPrimaryKey());
			Pair<String,String> fk2 = QueryUtils.splitKeyParts(join.getRightKeyPair().getForeignKey());
			ERRelationship rel = state.getErMapping().getRelationship(join);
			if( rel == null ) {
				throw new SQLTutorException("No relationship for join: " + join);
			}
			
			IQuery query = Factory.BASIC.createQuery(
				literal(joinRuleLookup, 
					"?t1", pk1.getSecond(), "?t2", fk1.getSecond(),
					"?t3", pk2.getSecond(), "?t4", fk2.getSecond(), "?eq1", "?eq2"),
				tableName("?t1", pk1.getFirst()),
				tableName("?t2", fk1.getFirst()),
				tableName("?t3", pk2.getFirst()),
				tableName("?t4", fk2.getFirst())
			);
			_log.info("Evaluating query: {}", query);
			List<IVariable> bindings = new ArrayList<IVariable>(6);
			IRelation result = null;
			try {
				result = state.getKnowledgeBase().execute(query, bindings);
			} catch( EvaluationException e ) {
				throw new SQLTutorException(e);
			}
			
			if( _log.isTraceEnabled() ) {
				_log.trace("Result: {}", result);
				_log.trace("Bindings: {}", bindings);
			}
			if( result.size() == 0 )
				continue;
			if( result.size() > 1 ) 
				_log.warn("More than one result, only using first: {}", result);

			_log.debug("Matched on relationship: {}", rel.getFullName());			
			
			RelationExtractor ext = new RelationExtractor(bindings);
			ext.setSqlFacts(facts);
			ITuple first = result.get(0);
			BinaryRelationalOperatorNode binop1 = (BinaryRelationalOperatorNode)ext.getNode("?eq1", first),
					binop2 = (BinaryRelationalOperatorNode)ext.getNode("?eq2", first);
			
			// TODO actually apply the rule
			_log.warn("FIXME: Need to apply rule.");
			
			// remove the join conditions
			SelectNode select = state.getAst();
			if( DEBUG ) _log.debug("Original query state: {}", QueryUtils.nodeToString(select));
			deleteCondition(binop1);
			if( DEBUG ) _log.debug("Intermediate query state: {}", QueryUtils.nodeToString(select));
			deleteCondition(binop2);
			if( DEBUG ) _log.debug("New query state: {}", QueryUtils.nodeToString(select));
		}
		return false;
	}

	private void findRelationships(ERMapping erMapping) {		
		fkJoins = new Stack<ERForeignKeyJoin>();
		lookupJoins = new Stack<ERLookupTableJoin>();
		Set<ERJoinMap> joins = erMapping.getJoins();
		for( ERJoinMap join: joins ) {
			switch( join.getMapType() ) {
				case FOREIGN_KEY:
					fkJoins.push((ERForeignKeyJoin)join);
					break;
				case LOOKUP_TABLE:
					lookupJoins.push((ERLookupTableJoin)join);
					break;
				case MERGED:
					_log.warn("FIXME: Merged join not implemented.");
					break;
			}
		}
	}
	
	private void deleteCondition(BinaryRelationalOperatorNode binop) {
		QueryTreeNode parent = QueryUtils.findParent(state.getAst(), binop);
		_log.debug("Found parent: {}", parent);
		
		if( parent instanceof BinaryOperatorNode ) {
			BinaryOperatorNode parentOp = (BinaryOperatorNode)parent;
			if( binop == parentOp.getLeftOperand() ) {
				replaceParent(parentOp, parentOp.getRightOperand());
			} else {
				replaceParent(parentOp, parentOp.getLeftOperand());
			}
		} else if( parent instanceof SelectNode ) {
			_log.debug("Deleting WHERE clause.");
			((SelectNode)parent).setWhereClause(null);
		} else {
			String type = parent.getClass().getName();
			_log.warn("Unhandled parent type ({}) for node: {}", type, QueryUtils.nodeToString(parent));
			throw new SQLTutorException("FIXME: Unhandled parent type: " + type);
		}
	}

	private void replaceParent(BinaryOperatorNode parentOp, ValueNode withOperand) {
		parentOp.setLeftOperand(null);
		parentOp.setRightOperand(null);
		
		QueryTreeNode grandparent = QueryUtils.findParent(state.getAst(), parentOp);
		if( grandparent instanceof BinaryOperatorNode ) {
			BinaryOperatorNode binop = (BinaryOperatorNode)grandparent;
			if( binop.getLeftOperand() == parentOp )
				binop.setLeftOperand(withOperand);
			else
				binop.setRightOperand(withOperand);
		} else if( grandparent instanceof SelectNode ) {
			if( _log.isDebugEnabled() ) _log.debug("Replacing WHERE clause with: {}", QueryUtils.nodeToString(withOperand));
			((SelectNode)grandparent).setWhereClause(withOperand);
		} else {
			throw new SQLTutorException("FIXME: Unhandled parent type: " + grandparent.getClass().getName());
		}
	}
}
