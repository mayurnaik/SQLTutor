/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.builtin;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import org.deri.iris.api.basics.ILiteral;
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
import edu.gatech.sqltutor.rules.SymbolicState;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicFacts.TokenMap;
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
	private static final IPredicate maybeBlockingPredicate = predicate(PREFIX + "maybeBlockingRefs", 3);
	private static final ILiteral maybeBlockingLiteral = literal(maybeBlockingPredicate, "?baseRef", "?nextRef", "?blockingRef");
	
	private static final IQuery possessivePronouns = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.partOfSpeech, "?token", PartOfSpeech.POSSESSIVE_PRONOUN)
	);
	
	private static class ReferenceCandidate {
		TableEntityRefToken baseRef;
		List<TableEntityRefToken> refsToReplace;
		public ReferenceCandidate(TableEntityRefToken baseRef, List<TableEntityRefToken> refsToReplace) {
			this.baseRef = baseRef;
			this.refsToReplace = refsToReplace;
		}
		public TableEntityRefToken getBaseRef() {
			return baseRef;
		}
		public List<TableEntityRefToken> getRefsToReplace() {
			return refsToReplace;
		}
	}
	
	private Random random = new Random();

	public AnaphoricPronounRule() {
	}

	public AnaphoricPronounRule(int precedence) {
		super(precedence);
	}
	
	@Override
	public boolean apply(SymbolicState state) {
		this.state = state;
		try {
			if( arePossessivePronounsUsed() )
				return false;
			return super.apply(state);
		} finally {
			this.state = null;
		}
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		List<ReferenceCandidate> candidates = new ArrayList<ReferenceCandidate>(ext.getRelation().size());
		while( ext.nextTuple() ) {
			TableEntityRefToken ref = ext.getToken("?firstRef");
			List<TableEntityRefToken> otherRefs = getUseableRefs(ref);
			if( otherRefs.size() == 0 ) {
				_log.debug(Markers.SYMBOLIC, "Reject candidate with no useable references: {}", ref);
				continue;
			}
			
			candidates.add(new ReferenceCandidate(ref, otherRefs));
		}
		
		int nCandidates = candidates.size();
		if( nCandidates == 0 )
			return false;
		ReferenceCandidate candidate;
		if( nCandidates == 1 ) {
			candidate = candidates.get(0);
		} else {
			_log.warn(Markers.SYMBOLIC, "Choosing from {} candidates.", nCandidates);
			// FIXME maybe use the one with greatest number of replaceable refs
//			candidate = candidates.get(random.nextInt(nCandidates));
			candidate = candidates.get(0);
		}
		
		TableEntityRefToken ref = candidate.getBaseRef();
		_log.debug(Markers.SYMBOLIC, "Using anaphoric reference base: {}", ref);
		replaceReferences(ref, candidate.getRefsToReplace());
			
		return true;
	}
	
	private void replaceReferences(TableEntityRefToken ref,
			List<TableEntityRefToken> otherRefs) {
		boolean trace = _log.isTraceEnabled(Markers.SYMBOLIC);
		
		_log.debug(Markers.SYMBOLIC, "Preparing to replace references to {}", ref);
		if( trace )
			_log.trace(Markers.SYMBOLIC, "Current state:\n{}", SymbolicUtil.prettyPrint(state.getRootToken()));
		
		SymbolicQueries queries = state.getQueries();
		
		
		TableEntityToken tableEntity = ref.getTableEntity();
		EREntity entity = queries.getReferencedEntity(tableEntity);
		EntityType type = entity.getEntityType();
		
		for( TableEntityRefToken otherRef: otherRefs ) {
			ISymbolicToken parent = otherRef.getParent(),
				after = SymbolicUtil.getSucceedingToken(otherRef),
				before = SymbolicUtil.getPrecedingToken(otherRef),
				replacement;
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
			
			// we rely on another rule cleaning up bad determiners, e.g. "each employee's supervisor" -> "each their supervisor"
			// see InvalidDeterminerRule
		}
		

		if( trace )
			_log.trace(Markers.SYMBOLIC, "After state:\n{}", SymbolicUtil.prettyPrint(state.getRootToken()));
	}
	

	/**
	 * Returns all the other ref tokens referring to the same entity and not 
	 * blocked by other references.
	 * 
	 * @param baseToken the main ref token
	 * @return the other ref tokens
	 */
	private List<TableEntityRefToken> getUseableRefs(TableEntityRefToken baseToken) {
		final TokenMap tokenMap = state.getSymbolicFacts().getTokenMap();
		Integer baseId = tokenMap.getObjectId(baseToken);
		
		RelationExtractor ext = getOtherRefs(baseId);
		int nRefs = ext.getRelation().size();
		if( nRefs == 0 )
			return Collections.emptyList();
		
		List<TableEntityRefToken> otherRefs = new ArrayList<TableEntityRefToken>(nRefs);
		while( ext.nextTuple() ) {
			TableEntityRefToken ref = ext.getToken("?otherRef");
			if( isPossessed(ref) )
				continue;
			Integer otherRefId = tokenMap.getObjectId(ref);
			if( isBlockedRef(baseId, otherRefId) )
				continue;
			otherRefs.add(ref);
		}
		return otherRefs;
	}

	/**
	 * Returns all the other ref tokens referring to the same entity.
	 * @param baseToken the main ref token
	 * @return the other ref tokens
	 */
	private RelationExtractor getOtherRefs(Integer tokenId) {
		IQuery query = Factory.BASIC.createQuery(
			literal(otherRefsPredicate, "?baseRef", "?otherRef"),
			literal(builtin(EqualBuiltin.class, "?baseRef", tokenId))
		);
		return IrisUtil.executeQuery(query, state);
	}
	
	
	private boolean isPossessed(ISymbolicToken token) {
		ISymbolicToken before = SymbolicUtil.getPrecedingToken(token);
		return before != null && before.getPartOfSpeech().isPossessive();
	}
	
	private boolean arePossessivePronounsUsed() {
		RelationExtractor ext = IrisUtil.executeQuery(possessivePronouns, state);
		return ext.getRelation().size() > 0;
	}
	
	private RelationExtractor getPotentialBlockers(Integer firstRefId, Integer nextRefId) {
		IQuery query = Factory.BASIC.createQuery(
			maybeBlockingLiteral,
			literal(builtin(EqualBuiltin.class, "?baseRef", firstRefId)),
			literal(builtin(EqualBuiltin.class, "?nextRef", nextRefId))
		);
		return IrisUtil.executeQuery(query, state);
	}
	
	/**
	 * Checks whether another entity reference blocks anaphoric referencing between two references.
	 * @param firstRefId the base reference
	 * @param nextRefId  the anaphoric reference candidate
	 * @return <code>true</code> if these references are blocked, <code>false</code> otherwise
	 */
	private boolean isBlockedRef(Integer firstRefId, Integer nextRefId) {
		final boolean DEBUG = _log.isDebugEnabled(Markers.SYMBOLIC);
		final boolean TRACE = _log.isTraceEnabled(Markers.SYMBOLIC);
		
		// start with all the potential blockers
		RelationExtractor ext = getPotentialBlockers(firstRefId, nextRefId);
		
		IRelation rel = null;
		if( TRACE ) {
			rel = ext.getRelation();
			_log.trace(Markers.SYMBOLIC, "Considering {} potential blockers: {}", rel.size(), rel);
		}
		
		boolean applied = false;
		while( ext.nextTuple() ) {
			TableEntityRefToken firstRef = ext.getToken("?baseRef"), 
				blockingRef = ext.getToken("?blockingRef");
			
			// if the blocking ref is posssessed by the same entity as our two refs, it does not block
			// e.g. {firstRef:ent1} ... {ref:ent1} {'s} {blockingRef:ent2} ... {nextRef:ent1} 
			ISymbolicToken before = SymbolicUtil.getPrecedingToken(blockingRef);
			PartOfSpeech beforePos = before.getPartOfSpeech();
			if( beforePos == PartOfSpeech.POSSESSIVE_ENDING ) {
				ISymbolicToken possessor = SymbolicUtil.getPrecedingToken(before);
				if( possessor.getType() == SymbolicType.TABLE_ENTITY_REF ) {
					TableEntityRefToken possessorRef = (TableEntityRefToken)possessor;
					if( possessorRef.getTableEntity().equals(firstRef.getTableEntity()) )
						continue;
				}
			}
			
			applied = true;
		}
		return applied;
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
		return DefaultPrecedence.PARTIAL_LOWERING - 1;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}

}
