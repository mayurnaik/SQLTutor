package edu.gatech.sqltutor.rules.symbolic.lowering;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SQLPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardLoweringRule;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLNounToken;

public class ColumnReferenceLiteralRule extends StandardLoweringRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(ColumnReferenceLiteralRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SQLPredicates.nodeHasType, "?colRef", "ColumnReference"),
		literal(SymbolicPredicates.singularLabel, "?colRef", "?singular"),
		literal(SymbolicPredicates.pluralLabel, "?colRef", "?plural")
	);

	public ColumnReferenceLiteralRule() {
	}

	public ColumnReferenceLiteralRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		final boolean TRACE = _log.isTraceEnabled(Markers.SYMBOLIC);
		boolean applied = false;
		while( ext.nextTuple() ) {
			SQLNounToken colRef = ext.getToken("?colRef");
			PartOfSpeech pos = colRef.getPartOfSpeech();
			String label;
			if( pos == PartOfSpeech.NOUN_SINGULAR_OR_MASS )
				label = colRef.getSingularLabel();
			else if ( pos == PartOfSpeech.NOUN_PLURAL )
				label = colRef.getPluralLabel();
			else
				continue;
			
			LiteralToken literal = new LiteralToken(label, pos);
			SymbolicUtil.replaceChild(colRef.getParent(), literal);
			
			if( TRACE ) _log.trace(Markers.SYMBOLIC, "Replaced {} with {}", colRef, literal);
			
			applied = true;
		}
		return applied;
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
}
