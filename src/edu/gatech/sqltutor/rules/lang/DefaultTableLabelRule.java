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

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.ISQLTranslationRule;
import edu.gatech.sqltutor.rules.SQLState;
import edu.gatech.sqltutor.rules.datalog.iris.ERPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.EntityLabelFormat;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SQLPredicates;

public class DefaultTableLabelRule extends AbstractSQLRule implements ISQLTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(DefaultTableLabelRule.class);
	
	public static final IPredicate PREDICATE = IrisUtil.predicate("rulDefaultTableLabel", 2); 
	
	public DefaultTableLabelRule() {
	}

	@Override
	public boolean apply(SQLState state) {
		this.state = state;
		try {
			IQuery query = Factory.BASIC.createQuery(
				literal(SQLPredicates.tableName, "?tref", "?tname"),
				literal(ERPredicates.erTableRefsEntity, "?tname", "?ent"),
				literal(new EntityLabelFormat("?ent", "?label")),
				literal(false, PREDICATE, "?tref", "?label")
			);
			List<IVariable> bindings = new ArrayList<IVariable>(3);
			IRelation results = null;
			try {
				_log.debug("Evaluating query: {}", query);
				results = state.getKnowledgeBase().execute(query, bindings);
			} catch( EvaluationException e ) {
				throw new SQLTutorException(e);
			}
			
			if( results.size() == 0 )
				return false;
			
			RelationExtractor ext = new RelationExtractor(bindings);
			for( int i = 0; i < results.size(); ++i ) {
				ITuple result = results.get(i);
				ITuple fact = IrisUtil.asTuple(ext.getTerm("?tref", result), 
					ext.getTerm("?label", result));
				state.addFact(PREDICATE, fact);
				_log.info("Added label fact: {}{}", PREDICATE.getPredicateSymbol(), fact);
			}
			return true;
		} finally {
			this.state = null;
		}
	}
}
