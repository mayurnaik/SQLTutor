package edu.gatech.sqltutor.rules.symbolic.lowering;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.er.ERRelationship;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.InRelationshipToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;
import edu.gatech.sqltutor.rules.util.NLUtil;

public class InRelationshipLoweringRule extends StandardSymbolicRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(InRelationshipLoweringRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?token", SymbolicType.IN_RELATIONSHIP)
	);

	public InRelationshipLoweringRule() {
	}

	public InRelationshipLoweringRule(int precedence) {
		super(precedence);
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		final boolean DEBUG = _log.isDebugEnabled(Markers.SYMBOLIC);
		while( ext.nextTuple() ) {
			InRelationshipToken token = ext.getToken("?token");
			TableEntityRefToken leftRef = new TableEntityRefToken(token.getLeftEntity());
			TableEntityRefToken rightRef = new TableEntityRefToken(token.getRightEntity());
			
			SequenceToken seq = new SequenceToken(PartOfSpeech.VERB_PHRASE);
			// TODO is this ordering fixed?
			seq.addChild(leftRef);
			verbalizeRelationship(token.getRelationship(), seq);
			seq.addChild(rightRef);
			
			if( DEBUG ) _log.debug(Markers.SYMBOLIC, "Replacing {} with {}", token, seq);
			SymbolicUtil.replaceChild(token, seq);
		}
		return true;
	}
	
	private void verbalizeRelationship(ERRelationship rel, ISymbolicToken parent) {
		String verb = NLUtil.getVerbForm(rel);
		String[] parts = verb.split("\\s+");
		if( parts.length > 2 )
			_log.warn("Unexpected number of verb tokens in: {}", verb);
		LiteralToken verbToken = new LiteralToken(parts[0], PartOfSpeech.VERB_RD_PERSON_SINGULAR_PRESENT);
		parent.addChild(verbToken);
		if( parts.length > 1 )
			parent.addChild(new LiteralToken(parts[1], PartOfSpeech.PREPOSITION_OR_SUBORDINATING_CONJUNCTION));
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.PARTIAL_LOWERING;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}

}
