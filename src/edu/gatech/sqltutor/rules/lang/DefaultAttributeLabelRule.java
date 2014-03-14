package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.builtins.EqualBuiltin;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.ISQLTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.SQLState;
import edu.gatech.sqltutor.rules.datalog.iris.EntityLabelFormat;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.LearnedPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;

/**
 * For a column named <em>c</em> referencing attribute <em>a</em>, 
 * use <em>c</em> and <em>a</em> as potential labels.  Label names 
 * are first processed by {@link EntityLabelFormat}.
 */
public class DefaultAttributeLabelRule 
		extends AbstractSQLRule implements ISQLTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(DefaultAttributeLabelRule.class);
	
	public static final String RULE_SOURCE = DefaultAttributeLabelRule.class.getSimpleName();
	
	private static final ITerm TERM_RULE_SOURCE = IrisUtil.asTerm(RULE_SOURCE);
	
	// ruleDefaultAttributeLabel(?entity,?attribute,?label)
	private static final IPredicate rulePredicate = IrisUtil.predicate("ruleDefaultAttributeLabel", 3);
	
	protected StaticRules rules = new StaticRules(DefaultAttributeLabelRule.class);	
	
	public DefaultAttributeLabelRule() { }
	
	@Override
	public boolean apply(SQLState state) {
		this.state = state;
		try {
			boolean applied = queryForLabel();
			return applied;
		} finally {
			this.state = null;
		}
	}
	
	private boolean queryForLabel() {
		IQuery query = Factory.BASIC.createQuery(
			literal(rulePredicate, "?ent", "?attr", "?label"),
			literal(new EqualBuiltin(IrisUtil.asTerms("?rule", TERM_RULE_SOURCE))),
			literal(false, LearnedPredicates.attributeLabel, "?ent", "?attr", "?label", "?rule")
		);

		List<IVariable> bindings = new ArrayList<IVariable>(4);
		IRelation results = null;
		try {
			_log.debug(Markers.DATALOG, "Evaluating query: {}", query);
			results = state.getKnowledgeBase().execute(query, bindings);
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
		
		if( results.size() < 1 )
			return false;
		
		RelationExtractor ext = new RelationExtractor(bindings);
		for( int i = 0; i < results.size(); ++i ) {
			ITuple result = results.get(i);
			
			ITuple fact = IrisUtil.asTuple(ext.getTerm("?ent", result), ext.getTerm("?attr", result), 
				ext.getTerm("?label", result), TERM_RULE_SOURCE);
			state.addFact(LearnedPredicates.attributeLabel, fact);
			_log.info(Markers.DATALOG_FACTS, "Added label fact: {}{}", LearnedPredicates.attributeLabel, fact);
		}
		return true;			
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return rules.getRules();
	}
}
