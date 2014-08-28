package edu.gatech.sqltutor.rules.symbolic.cleanup;

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
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;

/**
 * Cleans up invalid use of determiners.  This is to allow other rules to 
 * be a bit sloppy in their substitutions.
 */
public class InvalidDeterminerRule extends StandardSymbolicRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(InvalidDeterminerRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.partOfSpeech, "?det", PartOfSpeech.DETERMINER)
	);

	public InvalidDeterminerRule() {
	}

	public InvalidDeterminerRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		boolean applied = false;
		
		mainLoop:
		while( ext.nextTuple() ) {
			LiteralToken determiner = ext.getToken("?det");
			ISymbolicToken after = SymbolicUtil.getFollowingToken(determiner);
			switch( after.getPartOfSpeech() ) {
			case PERSONAL_PRONOUN:
			case POSSESSIVE_PRONOUN:
			case PROPER_NOUN_SINGULAR:
			case PROPER_NOUN_PLURAL:
				_log.debug(Markers.SYMBOLIC, "Deleting determiner {} followed by {}", determiner, after);
				determiner.getParent().removeChild(determiner);
				applied = true;
				continue mainLoop;
			default:
				break;
			}
			
		}
		return applied;
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.CLEANUP;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}

}
