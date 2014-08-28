package edu.gatech.sqltutor.rules.symbolic.cleanup;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;
import java.util.Locale;

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
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;

public class FixVerbTenseRule extends StandardSymbolicRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(FixVerbTenseRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?verb", SymbolicType.LITERAL),
		literal(SymbolicPredicates.partOfSpeech, "?verb", "?partOfSpeech"),
		literal(SymbolicPredicates.isVerb, "?partOfSpeech")
	);


	public FixVerbTenseRule() {
	}

	public FixVerbTenseRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		boolean applied = false;
		while( ext.nextTuple() ) {
			LiteralToken verb = ext.getToken("?verb");
			ISymbolicToken before = SymbolicUtil.getPrecedingToken(verb);
			if( before != null )
				applied |= checkVerb(verb, before);
		}
		return applied;
	}
	
	private boolean checkVerb(LiteralToken verb, ISymbolicToken before) {
		
		_log.trace(Markers.SYMBOLIC, "Checking {} against {}", verb, before);
		PartOfSpeech beforeType = before.getPartOfSpeech();
		switch( beforeType ) {
		case NOUN_SINGULAR_OR_MASS:
			return convertTo(verb, PartOfSpeech.VERB_RD_PERSON_SINGULAR_PRESENT);
		case NOUN_PLURAL:
			return convertTo(verb, PartOfSpeech.VERB_NON_RD_PERSON_SINGULAR_PRESENT);
		case NOUN_PHRASE:
		// FIXME noun phrases?
			break;
		case PERSONAL_PRONOUN:
			if( before instanceof LiteralToken ) {
				LiteralToken beforeLiteral = (LiteralToken)before;
				if( isSingularPronoun(beforeLiteral.getExpression()) )
					return convertTo(verb, PartOfSpeech.VERB_RD_PERSON_SINGULAR_PRESENT);
				return convertTo(verb, PartOfSpeech.VERB_NON_RD_PERSON_SINGULAR_PRESENT);
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	private boolean convertTo(LiteralToken verb,
			PartOfSpeech verbType) {
		if( verb.getPartOfSpeech() == verbType )
			return false;
		
		String expr = verb.getExpression(), nextExpr;
		if( verbType == PartOfSpeech.VERB_RD_PERSON_SINGULAR_PRESENT ) {
			nextExpr = convertToRDSingularPresent(expr);
		} else if ( verbType == PartOfSpeech.VERB_NON_RD_PERSON_SINGULAR_PRESENT ) {
			nextExpr = convertToNonRDSingularPresent(expr);
		} else {
			throw new SymbolicException("Don't know how to convert " + expr + " to " + verbType);
		}
		
		verb.setExpression(nextExpr);
		verb.setPartOfSpeech(verbType);
		return true;
	}
	
	private String convertToRDSingularPresent(String expr) {
		if( "are".equals(expr) || "were".equals(expr) )
			return "is";
		if( !expr.endsWith("s") )
			return expr + "s";
		_log.error(Markers.SYMBOLIC, "Don't know how to convert to RD-singular-present: {}", expr);
		return expr;
	}
	
	private String convertToNonRDSingularPresent(String expr) {
		if( "is".equals(expr) || "was".equals(expr) )
			return "are";
		if (expr.endsWith("s"))
			return expr.substring(0, expr.length() - 1);
		_log.error(Markers.SYMBOLIC, "Don't know how to convert to non-RD-singular-present: {}", expr);
		return expr;
	}
	
	private boolean isSingularPronoun(String expression) {
		expression = expression.toLowerCase(Locale.US).trim();
		if( "he".equals(expression) || "she".equals(expression) || "it".equals(expression) ) {
			return true;
		}
		return false;
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
