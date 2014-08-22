package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.builtin;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.builtins.EqualBuiltin;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.er.EREntity;
import edu.gatech.sqltutor.rules.er.EntityType;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.util.Literals;

/**
 * Rule for using pronouns for anaphoric referencing.  
 * 
 * Anaphoric referencing means referring to entities by implied context, that 
 * is "the employee and the employee's supervisor" can be stated as 
 * "the employee and their supervisor", where "their" is an anaphoric 
 * reference to "the employee".
 * 
 * We have to take special care when using these references or they 
 * can be ambiguous.  When changing this rule, prefer to be conservative and 
 * avoid substitutions that are potentially ambiguous.
 */
public class AnaphoricPronounRule extends StandardSymbolicRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(AnaphoricPronounRule.class);
	
	private static final String PREFIX = "ruleAnaphoricPronoun_";
	
	private static final StaticRules staticRules = new StaticRules(AnaphoricPronounRule.class);
	
	private static final IPredicate ruleCandidatesPredicate = predicate("ruleAnaphoricPronoun", 1);
	private static final IQuery ruleCandidatesQuery = Factory.BASIC.createQuery(
		literal(ruleCandidatesPredicate, "?firstRef")
	);
	
	private static final IPredicate otherRefsPredicate = predicate(PREFIX + "otherRefs", 2);
	
	private static final IQuery possessivePronouns = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.partOfSpeech, "?token", PartOfSpeech.POSSESSIVE_PRONOUN)
	);

	public AnaphoricPronounRule() {
	}

	public AnaphoricPronounRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		if( arePossessivePronounsUsed() )
			return false;
		
		while( ext.nextTuple() ) {
			TableEntityRefToken ref = ext.getToken("?firstRef");
			List<TableEntityRefToken> otherRefs = getOtherRefs(ref);
			
			if( areAnyPossessed(otherRefs) ) {
				_log.debug("Reject candidate which has a possessed reference: {}", ref);
				continue;
			}
			
			_log.info(Markers.SYMBOLIC, "Using anaphoric reference base: {}", ref);
			replaceReferences(ref, otherRefs);
			return true;
		}
		return false;
	}
	
	private void replaceReferences(TableEntityRefToken ref,
			List<TableEntityRefToken> otherRefs) {
		
		SymbolicQueries queries = state.getQueries();
		
		
		TableEntityToken tableEntity = ref.getTableEntity();
		EREntity entity = queries.getReferencedEntity(tableEntity);
		EntityType type = entity.getEntityType();
		
		for( TableEntityRefToken otherRef: otherRefs ) {
			ISymbolicToken parent = otherRef.getParent();
			ISymbolicToken after = SymbolicUtil.getFollowingToken(otherRef);
			ISymbolicToken replacement;
			if( after.getPartOfSpeech() == PartOfSpeech.POSSESSIVE_ENDING ) {
				parent.removeChild(after);
				switch( type ) {
				case PERSON:
					replacement = Literals.their();
					break;
				default:
					replacement = Literals.its();
					break;
				}
			} else {
				switch( type ) {
				case PERSON:
					replacement = Literals.they();
					break;
				default:
					replacement = Literals.it();
				}
			}
			_log.debug(Markers.SYMBOLIC, "Replacing {} with pronoun reference {}", otherRef, replacement);
			SymbolicUtil.replaceChild(otherRef, replacement);
		}
	}

	/**
	 * Returns all the other ref tokens referring to the same entity.
	 * @param baseToken the main ref token
	 * @return the other ref tokens
	 */
	private List<TableEntityRefToken> getOtherRefs(TableEntityRefToken baseToken) {
		Integer tokenId = state.getSymbolicFacts().getTokenMap().getObjectId(baseToken);
		IQuery query = Factory.BASIC.createQuery(
			literal(otherRefsPredicate, "?baseRef", "?otherRef"),
			literal(builtin(EqualBuiltin.class, "?baseRef", tokenId))
		);
		RelationExtractor ext = IrisUtil.executeQuery(query, state);
		
		ArrayList<TableEntityRefToken> otherRefs = new ArrayList<TableEntityRefToken>(ext.getRelation().size());
		while( ext.nextTuple() ) {
			TableEntityRefToken ref = ext.getToken("?otherRef");
			otherRefs.add(ref);
		}
		return otherRefs;
	}
	
	/**
	 * Checks whether any of these tokens are possessed (e.g. proceeded by a possessive).
	 */
	private boolean areAnyPossessed(Collection<? extends ISymbolicToken> tokens) {
		for( ISymbolicToken token: tokens ) {
			ISymbolicToken before = SymbolicUtil.getPrecedingToken(token);
			if( PartOfSpeech.isPossessive(before.getPartOfSpeech()) ) {
				return true;
			}
		}
		return false;
	}
	
	private boolean arePossessivePronounsUsed() {
		RelationExtractor ext = IrisUtil.executeQuery(possessivePronouns, state);
		return ext.getRelation().size() > 0;
	}
	
	@Override
	protected IQuery getQuery() {
		return ruleCandidatesQuery;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.SIMPLIFYING_SYMBOLIC;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}

}
