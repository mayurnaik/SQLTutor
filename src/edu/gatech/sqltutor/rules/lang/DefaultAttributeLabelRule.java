package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.builtins.EqualBuiltin;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	// ruleDefaultAttributeLabel(?entity,?attribute,?singular,?plural)
	private static final IPredicate rulePredicate = IrisUtil.predicate("ruleDefaultAttributeLabel", 4);
	
	protected StaticRules rules = new StaticRules(DefaultAttributeLabelRule.class);

	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(rulePredicate, "?ent", "?attr", "?singular", "?plural"),
		literal(new EqualBuiltin(IrisUtil.asTerms("?rule", TERM_RULE_SOURCE))),
		literal(false, LearnedPredicates.attributeLabel, "?ent", "?attr", "?singular", "?plural", "?rule")
	);
	
	public DefaultAttributeLabelRule() { }
	
	@Override
	public boolean apply(SQLState state) {
		this.state = state;
		try {
			RelationExtractor ext = IrisUtil.executeQuery(QUERY, state);
			IRelation results = ext.getRelation();
			if( results.size() == 0 )
				return false;
						
			final boolean debug = _log.isDebugEnabled(Markers.DATALOG_FACTS);
			while( ext.nextTuple() ) {
				ITuple fact = IrisUtil.asTuple(ext.getTerm("?ent"), ext.getTerm("?attr"), 
					ext.getTerm("?singular"), ext.getTerm("?plural"), TERM_RULE_SOURCE);
				state.addFact(LearnedPredicates.attributeLabel, fact);
				if( debug )
					_log.debug(Markers.DATALOG_FACTS, "Added label fact: {}{}", rulePredicate, fact);
			}
			return true;
		} finally {
			this.state = null;
		}
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return rules.getRules();
	}
}
