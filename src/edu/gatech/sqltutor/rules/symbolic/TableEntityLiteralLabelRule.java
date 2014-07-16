package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;

public class TableEntityLiteralLabelRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(TableEntityLiteralLabelRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?tableEntity", SymbolicType.TABLE_ENTITY)
	);
	
	public TableEntityLiteralLabelRule() {
		super(DefaultPrecedence.LOWERING);
	}
	
	public TableEntityLiteralLabelRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		// FIXME need to account for context, for now just substitute the labels
		boolean applied = false;
		while( ext.nextTuple() ) {
			TableEntityToken token = ext.getToken("?tableEntity");
			PartOfSpeech pos = token.getPartOfSpeech();
			String label = null;
			switch(pos) {
			case NOUN_SINGULAR_OR_MASS:
				label = token.getSingularLabel();
				break;
			case NOUN_PLURAL:
				label = token.getPluralLabel();
				break;
			default:
				break;
			}
			if( label != null ) {
				LiteralToken literal = new LiteralToken(label, pos);
				SymbolicUtil.replaceChild(token.getParent(), token, literal);
				_log.info(Markers.SYMBOLIC, "Replaced token {} with {}", token, literal);
				applied = true;
			}
		}
		return applied;
	}
	
	@Override
	protected IQuery getQuery() { return QUERY; }
	
	@Override
	protected int getVariableEstimate() {
		return 1;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
}
