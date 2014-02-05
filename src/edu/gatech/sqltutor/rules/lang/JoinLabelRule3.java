package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
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

public class JoinLabelRule3 extends AbstractSQLRule implements ISQLTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(JoinLabelRule3.class);
	
	// rules defined statically
	private static final IPredicate joinRuleFK = 
			Factory.BASIC.createPredicate("joinRuleFK3", 8);
	private static final IPredicate joinRuleLookup = 
			Factory.BASIC.createPredicate("joinRuleLookup3", 15);

	public JoinLabelRule3() {
	}
	
	@Override
	public boolean apply(SQLState state) {
		this.state = state;
		
		try {
			if( detectFKJoin() )
				return true;
			
			if( detectLookupJoins() )
				return true;
			
			return false;
		} finally {
			this.state = null;
		}
	}
	
	private boolean detectFKJoin() {
		IQuery query = Factory.BASIC.createQuery(
			literal(joinRuleFK, "?rel", 
				"?tref1", "?tname1", "?attr1",
				"?tref2", "?tname2", "?attr2",
				"?eq")
		);
		List<IVariable> bindings = new ArrayList<IVariable>(joinRuleFK.getArity());
		IRelation results = null;
		try {
			_log.debug("Evaluating query: {}", query);
			results = state.getKnowledgeBase().execute(query, bindings);
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
		
		if( results.size() == 0 )
			return false;
		
		_log.debug("Results: {}", results);
		
		RelationExtractor ext = new RelationExtractor(bindings);
		ext.setSqlFacts(state.getSqlFacts());
		for( int i = 0, ilen = results.size(); i < ilen; ++i ) {
			ITuple result = results.get(i);
			String relationship = ext.getTerm("?rel", result).toString();
			_log.debug("Matched on relationship: {}", relationship);
			FromBaseTable t1Table = (FromBaseTable)ext.getNode("?tref1", result);
			FromBaseTable t2Table = (FromBaseTable)ext.getNode("?tref2", result);
			BinaryRelationalOperatorNode binop = (BinaryRelationalOperatorNode)ext.getNode("?eq", result);
			
//			_log.info("t1Table: {}\nt2Table: {}\nbinop: {}", t1Table, t2Table, binop);
//			EREdgeConstraint leftConstraint = rel.getLeftEdge().getConstraint();
//			EREdgeConstraint rightConstraint = rel.getRightEdge().getConstraint();
//			
//			// TODO actually apply the rule
//			_log.info("\nApply {} to table {}\nApply {} to table {}", 
//				leftConstraint.getLabel(), t1Table, rightConstraint.getLabel(), t2Table);
			
			SelectNode select = state.getAst();
			if( _log.isDebugEnabled() ) _log.debug("Original query state: {}", QueryUtils.nodeToString(select));
			deleteCondition(binop);
			if( _log.isDebugEnabled() ) _log.debug("New query state: {}", QueryUtils.nodeToString(select));
		}
		
		return true;
	}
	
	private boolean detectLookupJoins() {
		final boolean DEBUG = _log.isDebugEnabled();
		IQuery query = Factory.BASIC.createQuery(
			literal(joinRuleLookup, "?rel", 
				"?tref1", "?tname1", "?attr1",
				"?tref2", "?tname2", "?attr2",
				"?tref3", "?tname3", "?attr3",
				"?tref4", "?tname4", "?attr4",
				"?eq1", "?eq2")
		);
		List<IVariable> bindings = new ArrayList<IVariable>(joinRuleLookup.getArity());
		IRelation results = null;
		try {
			_log.debug("Evaluating query: {}", query);
			results = state.getKnowledgeBase().execute(query, bindings);
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
		
		if( results.size() == 0 )
			return false;
		
		_log.debug("JOIN RULE LOOKUP Results: {}", results);
		
		RelationExtractor ext = new RelationExtractor(bindings);
		ext.setSqlFacts(state.getSqlFacts());
		for( int i = 0, ilen = results.size(); i < ilen; ++i ) {
			ITuple result = results.get(i);
			String relationship = ext.getTerm("?rel", result).toString();
			_log.debug("Matched on relationship: {}", relationship);
			FromBaseTable t1Table = (FromBaseTable)ext.getNode("?tref1", result);
			FromBaseTable t2Table = (FromBaseTable)ext.getNode("?tref2", result);
			BinaryRelationalOperatorNode binop1 = (BinaryRelationalOperatorNode)ext.getNode("?eq1", result);
			FromBaseTable t3Table = (FromBaseTable)ext.getNode("?tref3", result);
			FromBaseTable t4Table = (FromBaseTable)ext.getNode("?tref4", result);
			BinaryRelationalOperatorNode binop2 = (BinaryRelationalOperatorNode)ext.getNode("?eq2", result);
			
			_log.info("t1Table: {}\nt2Table: {}\nbinop: {}", t1Table, t2Table, binop1);

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
