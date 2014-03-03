package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.predicate;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.BinaryOperatorNode;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.ISQLTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.SQLState;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.util.QueryManip;


public class DescribingAttributeLabelRule extends AbstractSQLRule implements ISQLTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(DescribingAttributeLabelRule.class);
	
	// ruleAttributeDescribes(?table,?eq,?value,?type)
	private static final IPredicate ruleAttributeDescribes = predicate("ruleAttributeDescribes", 4);
	public static final IPredicate ruleAttributeDescribesLabel = predicate("ruleAttributeDescribesLabel", 3); 
	
	public static final String RULE_SOURCE = DescribingAttributeLabelRule.class.getSimpleName();
	private static final ITerm TERM_RULE_SOURCE = IrisUtil.asTerm(RULE_SOURCE);
	
	private static final StaticRules staticRules = new StaticRules(DescribingAttributeLabelRule.class);
	
	public DescribingAttributeLabelRule() {
	}

	@Override
	public boolean apply(SQLState state) {
		final boolean DEBUG = _log.isDebugEnabled(Markers.METARULE);
		
		IQuery query = Factory.BASIC.createQuery(
			literal(ruleAttributeDescribes, "?table", "?eq", "?value", "?type")
		);
		List<IVariable> bindings = new ArrayList<IVariable>(4);
		IRelation rel = null;
		try {
			rel = state.getKnowledgeBase().execute(query, bindings);
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
		int size = rel.size();
		if( size < 1 )
			return false;
		
		RelationExtractor ext = new RelationExtractor(bindings);
		ext.setNodeMap(state.getSqlFacts().getNodeMap());
		for( int i = 0; i < size; ++i ) {
			ITuple result = rel.get(i);
			
			BinaryOperatorNode binop = (BinaryOperatorNode)ext.getNode("?eq", result);
			QueryManip.deleteCondition(state, binop);
			
			ITuple fact = IrisUtil.asTuple(ext.getTerm("?table", result), 
				ext.getTerm("?value", result), ext.getTerm("?type", result));
			state.addFact(ruleAttributeDescribesLabel, fact);
			
			if( DEBUG ) _log.debug(Markers.METARULE, "Added fact: {}{}", ruleAttributeDescribesLabel.getPredicateSymbol(), fact);
		}
		
		return true;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
}
