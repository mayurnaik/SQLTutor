package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.asTuple;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.BinaryRelationalOperatorNode;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.SelectNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.ISQLTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.SQLState;
import edu.gatech.sqltutor.rules.datalog.iris.ERPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.LearnedPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.util.QueryManip;

/**
 * Meta-rule for labeling join entities.
 * 
 * <p>
 * Given an inner join of the form:
 * </p><p>
 * <i>t<sub>1</sub></i> <tt>INNER JOIN</tt> <i>t<sub>2</sub></i> 
 * <tt>ON</tt> <i>t<sub>1</sub>.a</i> <tt>=</tt> <i>t<sub>2</sub>.b</i>
 * </p><p>
 * Where <i>t<sub>1</sub>.a</i> and <i>t<sub>2</sub>.b</i> form a 
 * one-to-one or one-to-many foreign-key relationship, there is a specified 
 * name or label for the <i>t<sub>1</sub></i> and <i>t<sub>2</sub></i> entities in the context 
 * of this join.
 * </p><p>
 * For example, in a company database, the join:
 * </p><p>
 * <code>employee AS e1 INNER JOIN employee e2 ON e1.manager_ssn=e2.ssn</code> 
 * implies that <code>e2</code> is the "manager" of <code>e1</code>.
 * </p><p>
 * Similarly, for lookup table joins there is a verb relationship between two 
 * entities based on two lookups.
 * </p>
 */
public class JoinLabelRule extends AbstractSQLRule implements ISQLTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(JoinLabelRule.class);
	
	private static final String RULE_SOURCE = JoinLabelRule.class.getSimpleName();
	private static final ITerm TERM_RULE_SOURCE = IrisUtil.asTerm(RULE_SOURCE);
	
	private static final StaticRules staticRules = new StaticRules(JoinLabelRule.class);
	
	// rules defined statically
	private static final IPredicate joinRuleFK = 
			Factory.BASIC.createPredicate("joinRuleFK", 8);	
	private static final IPredicate joinRuleLookup = 
			Factory.BASIC.createPredicate("joinRuleLookup", 15);
	public JoinLabelRule() {
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
				"?eq"),
			literal(ERPredicates.erFKJoinSides, "?rel", "?pkPos", "?fkPos"),
			literal(ERPredicates.erRelationshipEdgeLabel, "?rel", "?pkPos", "?pkLabel"),
			literal(ERPredicates.erRelationshipEdgeLabel, "?rel", "?fkPos", "?fkLabel")
		);
		List<IVariable> bindings = new ArrayList<IVariable>(joinRuleFK.getArity());
		IRelation results = null;
		try {
			_log.debug(Markers.METARULE, "Evaluating query: {}", query);
			results = state.getKnowledgeBase().execute(query, bindings);
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
		
		if( results.size() == 0 )
			return false;
		
		_log.debug(Markers.METARULE, "Join rule foreign-key results: {}", results);
		
		RelationExtractor ext = new RelationExtractor(bindings);
		ext.setSqlFacts(state.getSqlFacts());
		for( int i = 0, ilen = results.size(); i < ilen; ++i ) {
			ITuple result = results.get(i);
			ITerm relationship = ext.getTerm("?rel", result);
			_log.debug(Markers.METARULE, "Matched on relationship: {}", relationship);
			BinaryRelationalOperatorNode binop = (BinaryRelationalOperatorNode)ext.getNode("?eq", result);
			
			String pkLabel = ((IStringTerm)ext.getTerm("?pkLabel", result)).getValue();
			String fkLabel = ((IStringTerm)ext.getTerm("?fkLabel", result)).getValue();
			
			if( _log.isDebugEnabled() ) {
				FromBaseTable t1Table = (FromBaseTable)ext.getNode("?tref1", result);
				FromBaseTable t2Table = (FromBaseTable)ext.getNode("?tref2", result);
				_log.debug(Markers.METARULE, "\nApply {} to table {}\nApply {} to table {}", 
					pkLabel, t1Table, fkLabel, t2Table);
			}
			
			// generate facts for the labels
			ITerm tref1 = ext.getTerm("?tref1", result),
					tref2 = ext.getTerm("?tref2", result);
			state.addFact(LearnedPredicates.tableLabel, asTuple(
				tref1, pkLabel.toLowerCase(), TERM_RULE_SOURCE));
			state.addFact(LearnedPredicates.tableLabel, asTuple(
				tref2, fkLabel.toLowerCase(), TERM_RULE_SOURCE));
			state.addFact(LearnedPredicates.tableInRelationship, asTuple(
				tref1, relationship, ext.getTerm("?pkPos", result), TERM_RULE_SOURCE));
			state.addFact(LearnedPredicates.tableInRelationship, asTuple(
				tref2, relationship, ext.getTerm("?fkPos", result), TERM_RULE_SOURCE));
			
			SelectNode select = state.getAst();
			if( _log.isDebugEnabled() ) _log.debug(Markers.METARULE, "Original query state: {}", QueryUtils.nodeToString(select));
			QueryManip.deleteCondition(state, binop);
			if( _log.isDebugEnabled() ) _log.debug(Markers.METARULE, "New query state: {}", QueryUtils.nodeToString(select));
		}
		
		return true;
	}
	
	private boolean detectLookupJoins() {
		final boolean DEBUG = _log.isDebugEnabled(Markers.METARULE);
		
		// reuse int terms
		final ITerm ZERO = Factory.CONCRETE.createInt(0);
		final ITerm ONE  = Factory.CONCRETE.createInt(1);
		
		IQuery query = Factory.BASIC.createQuery(
			literal(joinRuleLookup, "?rel", 
				"?tref1", "?tname1", "?attr1",
				"?tref2", "?tname2", "?attr2",
				"?tref3", "?tname3", "?attr3",
				"?tref4", "?tname4", "?attr4",
				"?eq1", "?eq2"),
			literal(ERPredicates.erRelationshipEdgeLabel, "?rel", ZERO, "?pkLabelLeft"),
			literal(ERPredicates.erRelationshipEdgeLabel, "?rel", ONE, "?pkLabelRight")
		);
		List<IVariable> bindings = new ArrayList<IVariable>(joinRuleLookup.getArity());
		IRelation results = null;
		try {
			if( DEBUG ) _log.debug(Markers.DATALOG, "Evaluating query: {}", query);
			results = state.getKnowledgeBase().execute(query, bindings);
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
		
		if( results.size() == 0 )
			return false;
		
		if( DEBUG ) _log.debug(Markers.METARULE, "JOIN RULE LOOKUP Results: {}", results);
		
		RelationExtractor ext = new RelationExtractor(bindings);
		ext.setSqlFacts(state.getSqlFacts());
		for( int i = 0, ilen = results.size(); i < ilen; ++i ) {
			ITuple result = results.get(i);
			ITerm relationship = ext.getTerm("?rel", result);
			_log.debug(Markers.METARULE, "Matched on relationship: {}", relationship);
			
			ITerm pkRefLeft = ext.getTerm("?tref1", result), 
					pkRefRight = ext.getTerm("?tref3", result);
			String pkLabelLeft = ((IStringTerm)ext.getTerm("?pkLabelLeft", result)).getValue(),
					pkLabelRight = ((IStringTerm)ext.getTerm("?pkLabelRight", result)).getValue();
			BinaryRelationalOperatorNode binop1 = (BinaryRelationalOperatorNode)ext.getNode("?eq1", result);
			BinaryRelationalOperatorNode binop2 = (BinaryRelationalOperatorNode)ext.getNode("?eq2", result);
			
			if( DEBUG ) {
				FromBaseTable t1Table = (FromBaseTable)ext.getNode("?tref1", result);
				FromBaseTable t2Table = (FromBaseTable)ext.getNode("?tref2", result);
				FromBaseTable t3Table = (FromBaseTable)ext.getNode("?tref3", result);
				FromBaseTable t4Table = (FromBaseTable)ext.getNode("?tref4", result);
				
				_log.debug(Markers.METARULE, "t1Table: {}\nt2Table: {}\neq1: {}\nt3Table: {}\nt4Table: {}\neq2: {}", 
					t1Table, t2Table, binop1, t3Table, t4Table, binop2);
			}

			// remove the join conditions
			SelectNode select = state.getAst();
			if( DEBUG ) _log.debug(Markers.METARULE, "Original query state: {}", QueryUtils.nodeToString(select));
			QueryManip.deleteCondition(state, binop1);
			if( DEBUG ) _log.debug(Markers.METARULE, "Intermediate query state: {}", QueryUtils.nodeToString(select));
			QueryManip.deleteCondition(state, binop2);
			if( DEBUG ) _log.debug(Markers.METARULE, "New query state: {}", QueryUtils.nodeToString(select));
			
			// add label and relationship facts
			// left edge
			state.addFact(LearnedPredicates.tableLabel,
				asTuple(pkRefLeft, pkLabelLeft.toLowerCase(), TERM_RULE_SOURCE));
			state.addFact(LearnedPredicates.tableInRelationship, 
				asTuple(pkRefLeft, relationship, ZERO, TERM_RULE_SOURCE));
			// right edge
			state.addFact(LearnedPredicates.tableLabel,
				asTuple(pkRefRight, pkLabelRight.toLowerCase(), TERM_RULE_SOURCE));
			state.addFact(LearnedPredicates.tableInRelationship, 
				asTuple(pkRefRight, relationship, ONE, TERM_RULE_SOURCE));
		}
		
		return false;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
}
