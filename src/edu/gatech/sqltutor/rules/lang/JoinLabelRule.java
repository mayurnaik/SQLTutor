package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;
import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.SymbolicState;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.ERPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.symbolic.tokens.InRelationshipToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLNounToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;
import edu.gatech.sqltutor.rules.util.NLUtil;

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
public class JoinLabelRule extends AbstractSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(JoinLabelRule.class);
	
	private static final StaticRules staticRules = new StaticRules(JoinLabelRule.class);
	
	// rules defined statically
	private static final IPredicate joinRuleFK = 
			Factory.BASIC.createPredicate("joinRuleFK", 8);	
	private static final IPredicate joinRuleLookup = 
			Factory.BASIC.createPredicate("joinRuleLookup", 15);

	/** Query used to detect foreign-key joins. */
	private static final IQuery FK_QUERY = Factory.BASIC.createQuery(
		literal(joinRuleFK, "?rel", 
			"?tref1", "?tname1", "?attr1",
			"?tref2", "?tname2", "?attr2",
			"?eq"),
		literal(ERPredicates.erFKJoinSides, "?rel", "?pkPos", "?fkPos"),
		literal(ERPredicates.erRelationshipEdgeLabel, "?rel", "?pkPos", "?pkLabel"),
		literal(ERPredicates.erRelationshipEdgeLabel, "?rel", "?fkPos", "?fkLabel")
	);
	
	/** Query used to detect lookup-table style joins. */
	private static final IQuery LOOKUP_QUERY = Factory.BASIC.createQuery(
		literal(joinRuleLookup, "?rel", 
			"?tref1", "?tname1", "?attr1",
			"?tref2", "?tname2", "?attr2",
			"?tref3", "?tname3", "?attr3",
			"?tref4", "?tname4", "?attr4",
			"?eq1", "?eq2"),
		literal(ERPredicates.erRelationshipEdgeLabel, "?rel", 0, "?pkLabelLeft"),
		literal(ERPredicates.erRelationshipEdgeLabel, "?rel", 1, "?pkLabelRight")
	);
	
	public JoinLabelRule() {
	}
	
	@Override
	public boolean apply(SymbolicState state) {
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
		RelationExtractor ext = IrisUtil.executeQuery(FK_QUERY, state);
		if( ext.getRelation().size() == 0 )
			return false;
		while( ext.nextTuple() ) {
			ITerm relationship = ext.getTerm("?rel");
			_log.debug(Markers.METARULE, "Matched on relationship: {}", relationship);
			SQLToken binop = ext.getToken("?eq");
			String pkLabel = ext.getString("?pkLabel").toLowerCase(),
				fkLabel = ext.getString("?fkLabel").toLowerCase();
			
			SQLNounToken pkTable = ext.getToken("?tref1"), fkTable = ext.getToken("?tref2");
			// FIXME allow plural overrides in ER
			String pkPlural = NLUtil.pluralize(pkLabel), fkPlural = NLUtil.pluralize(fkLabel);
			pkTable.setSingularLabel(pkLabel);
			pkTable.setPluralLabel(pkPlural);
			fkTable.setSingularLabel(fkLabel);
			fkTable.setPluralLabel(fkPlural);
			
			InRelationshipToken inRelationship = new InRelationshipToken();
			// FIXME set up this node and replace condition, need to use {TABLE_ENTITY}
			// FIXME for now, we'll just delete it
			state.deleteNode(binop);
		}
		
		return true;
	}
	
	private boolean detectLookupJoins() {
		final boolean DEBUG = _log.isDebugEnabled(Markers.METARULE);
		
		RelationExtractor ext = IrisUtil.executeQuery(LOOKUP_QUERY, state);
		if( ext.getRelation().size() == 0 )
			return false;
		
		while( ext.nextTuple() ) {
			ITerm relationship = ext.getTerm("?rel");
			if( DEBUG ) _log.debug(Markers.METARULE, "Matched on relationship: {}", relationship);
			
			SQLNounToken pkTableLeft = ext.getToken("?tref1"),
			            pkTableRight = ext.getToken("?tref3");
			String pkLabelLeft = ext.getString("?pkLabelLeft").toLowerCase(),
			      pkLabelRight = ext.getString("?pkLabelRight").toLowerCase();
			SQLToken binop1 = ext.getToken("?eq1"),
			         binop2 = ext.getToken("?eq2");
			
			// remove the join conditions
			state.deleteNode(binop2);
			InRelationshipToken inRelationship = new InRelationshipToken();
			// FIXME replace first condition with this token, for now just delete
			state.deleteNode(binop1);
//			
			// update labels
			// FIXME allow ER override, handle {TABLE_ENTITY}
			String pkLeftPlural = NLUtil.pluralize(pkLabelLeft), 
			      pkRightPlural = NLUtil.pluralize(pkLabelRight);
			pkTableLeft.setSingularLabel(pkLabelLeft);
			pkTableLeft.setPluralLabel(pkLeftPlural);
			pkTableRight.setSingularLabel(pkLabelRight);
			pkTableRight.setPluralLabel(pkRightPlural);
		}
		
		return false;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.SQL_ANALYSIS);
	}
}
