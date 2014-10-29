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
package edu.gatech.sqltutor.rules.symbolic.lowering;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.builtins.GreaterBuiltin;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.er.EntityType;
import edu.gatech.sqltutor.rules.lang.StandardLoweringRule;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.WhereToken;
import edu.gatech.sqltutor.rules.util.Literals;

/**
 * Converts all references to a base entity instance to anaphoric references.
 * 
 * <p>This rule acts on a single entity instance reference <i>ref</i>, followed by all conditions 
 * after the <tt>{WHERE}</tt> token being phrases that are either: 
 * <ul><li>
 *     a) possessive references to the same entity, 
 *     e.g. "<i>ref</i>'s" or
 *   </li><li> 
 *     b) verb phrases with a ref to the same entity in the leading position, 
 *     e.g. "<i>ref</i> works for ..."
 * </li></ul>
 * 
 * <p>This rule replaces the above with anaphoric references based on the type of 
 * the entity.  For example, from:<br />
 * "... of each employee where the employee works for ... and the employee's salary is ..."<br />
 * to<br />
 * "... of each employee who works for ... and whose salary is ...."
 * 
 * @author Jake Cobb
 */
public class SingleReferenceAnaphoraRule extends StandardLoweringRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(SingleReferenceAnaphoraRule.class);
	
	private static final StaticRules staticRules = new StaticRules(SingleReferenceAnaphoraRule.class);
	
	private static final String PREFIX = "ruleSingleReferenceAnaphora_";
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleSingleReferenceAnaphora", 2);
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?ref", "?where")
	);
	
	private static final IQuery getVerbPhrasesAfterWhere = Factory.BASIC.createQuery(
		literal(IrisUtil.predicate(PREFIX + "isVerbPhrase", 1), "?token"),
		literal(SymbolicPredicates.type, "?where", SymbolicType.WHERE),
		literal(GreaterBuiltin.class, "?token", "?where")
	);
	
	private static enum PhraseType {
		POSSESSIVE {
			@Override
			public boolean acceptsLiteralRef(LiteralToken theRef) {
				return theRef.getPartOfSpeech() == PartOfSpeech.POSSESSIVE_PRONOUN;
			}
			@Override
			public boolean acceptsTokenFollowingRef(ISymbolicToken after) {
				return after.getPartOfSpeech() == PartOfSpeech.POSSESSIVE_ENDING;
				
			}
		},
		
		VERBALIZING {
			@Override
			public boolean acceptsLiteralRef(LiteralToken theRef) {
				return theRef.getPartOfSpeech() == PartOfSpeech.PERSONAL_PRONOUN;
			}
			
			@Override
			public boolean acceptsTokenFollowingRef(ISymbolicToken after) {
				return after.getPartOfSpeech().isVerb();
			}
		};

		public abstract boolean acceptsLiteralRef(LiteralToken theRef);
		public abstract boolean acceptsTokenFollowingRef(ISymbolicToken after);
	}

	public SingleReferenceAnaphoraRule() {
	}

	public SingleReferenceAnaphoraRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		if( relation.size() != 1 )
			throw new SymbolicException("Should only ever have one result, got: " + relation);
		ext.nextTuple();
		
		TableEntityRefToken ref = ext.getToken("?ref");
		WhereToken where = ext.getToken("?where");
		List<ISymbolicToken> verbPhrases = getVerbPhrasesAfterWhere();
		if( areAllPhraseType(ref, verbPhrases, EnumSet.of(PhraseType.POSSESSIVE, PhraseType.VERBALIZING)) ) {
			_log.debug(Markers.SYMBOLIC, "Using single-reference anaphoric base: {}", ref);
			replaceAllPhrases(ref, verbPhrases);
			_log.debug(Markers.SYMBOLIC, "Deleting {}", where);
			where.getParent().removeChild(where);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Digs through any nested noun-phrases and returns either the verb phrase 
	 * or the inner-most noun phrase.
	 * @param verbPhrase
	 * @return
	 */
	private ISymbolicToken getNounStartPhrase(ISymbolicToken verbPhrase) {
		ISymbolicToken phrase = verbPhrase;
		while( true ) {
			List<ISymbolicToken> children = phrase.getChildren();
			if( children.size() == 0 )
				break;
			ISymbolicToken child = children.get(0);
			if( child.getPartOfSpeech() == PartOfSpeech.NOUN_PHRASE ) {
				phrase = child;
			} else {
				break;
			}
		}
		return phrase;
	}
	
	/**
	 * Gets the phrase type of a verb phrase with respect to a 
	 * base reference <code>ref</code>.
	 *  
	 * @param ref         the base entity reference being considered
	 * @param verbPhrase  the candidate verb phrase
	 * @return {@link PhraseType#VERBALIZING}, {@link PhraseType#POSSESSIVE} or <code>null</code>
	 */
	private PhraseType getPhraseType(TableEntityRefToken ref, ISymbolicToken verbPhrase) {
		TableEntityToken entity = ref.getTableEntity();
		ISymbolicToken nounParent = getNounStartPhrase(verbPhrase);
		ISymbolicToken theRef = getPotentialReference(nounParent);
		if( theRef == null )
			return null;
		if( theRef instanceof LiteralToken ) {
			LiteralToken literal = (LiteralToken)theRef;
			return PhraseType.POSSESSIVE.acceptsLiteralRef(literal)  ? PhraseType.POSSESSIVE :
			       PhraseType.VERBALIZING.acceptsLiteralRef(literal) ? PhraseType.VERBALIZING :
			       null;
		}
		
		TableEntityRefToken otherRef = (TableEntityRefToken)theRef;
		// must be the base reference regardless of phrase type
		if( otherRef.getTableEntity() != entity )
			return null;
		
		ISymbolicToken after = SymbolicUtil.getFollowingToken(otherRef);
		return PhraseType.POSSESSIVE.acceptsTokenFollowingRef(after)  ? PhraseType.POSSESSIVE :
		       PhraseType.VERBALIZING.acceptsTokenFollowingRef(after) ? PhraseType.VERBALIZING :
		       null;
	}
	
	/**
	 * Indicates whether all verb phrases are candidate anaphoric references for <code>ref</code> 
	 * and of one of the specified types.
	 */
	private boolean areAllPhraseType(TableEntityRefToken ref, List<ISymbolicToken> verbPhrases, 
			EnumSet<PhraseType> phraseTypes) {
		if( verbPhrases.size() < 1 )
			return false;
		for( ISymbolicToken verbPhrase: verbPhrases ) {
			PhraseType phraseType = getPhraseType(ref, verbPhrase);
			if( !phraseTypes.contains(phraseType) ) {
				return false;
			}
		}
		return true;
		
	}
	
	private void replaceAllPhrases(TableEntityRefToken ref, List<ISymbolicToken> verbPhrases) {
		final boolean TRACE = _log.isTraceEnabled(Markers.SYMBOLIC);
		
		EntityType entityType = state.getQueries().getReferencedEntity(ref.getTableEntity()).getEntityType();
		
		PhraseType lastType = null;
		for( ISymbolicToken verbPhrase: verbPhrases ) {
			if( TRACE ) _log.trace(Markers.SYMBOLIC, "Processing phrase:\n{}", SymbolicUtil.prettyPrint(verbPhrase));
			
			ISymbolicToken nounPhrase = getNounStartPhrase(verbPhrase);
			PhraseType phraseType = getPhraseType(ref, verbPhrase);
			
			// WH_PRONOUN will be inserted if phrase type is changing, e.g.:
			// "who <verbalize>, <verbalize>, whose <possessive>, and <possessive>"
			ISymbolicToken replacement = null;
			if( phraseType != lastType ) {
				switch( phraseType ) {
				case POSSESSIVE:
					replacement = Literals.whose();
					break;
				case VERBALIZING:
					if( entityType == EntityType.PERSON ) {
						replacement = new LiteralToken("who", PartOfSpeech.WH_PRONOUN);
					} else {
						replacement = new LiteralToken("which", PartOfSpeech.WH_PRONOUN);
					}
					break;
				}
			}
			
			List<ISymbolicToken> children = nounPhrase.getChildren();
			ISymbolicToken token = children.get(0);
			
			if( token.getPartOfSpeech() == PartOfSpeech.DETERMINER ) {
				_log.debug(Markers.SYMBOLIC, "Deleted determiner: {}", token);
				token.getParent().removeChild(token);
				token = children.get(0);
			}

			if( token.getPartOfSpeech().isPronoun() ) {
				// remove e.g. "their" or "they", inserting WH_PRONOUN if needed
				if( replacement == null ) {
					_log.debug(Markers.SYMBOLIC, "Deleting pronoun: {}", token);
					token.getParent().removeChild(token);
				} else {
					_log.debug(Markers.SYMBOLIC, "Replacing pronoun {} with {}", token, replacement);
					SymbolicUtil.replaceChild(token, replacement);
				}
			} else {
				// remove the entity reference 
				if( !(token instanceof TableEntityRefToken) )
					throw new SymbolicException("Expecting a reference token, not: " + token);
				if( replacement == null ) {
					_log.debug(Markers.SYMBOLIC, "Deleting reference: {}", token);
					token.getParent().removeChild(token);
				} else {
					_log.debug(Markers.SYMBOLIC, "Replacing reference {} with {}", token, replacement);
					SymbolicUtil.replaceChild(token, replacement);
				}
				
				// cleanup the possessive token for possessive phrases
				if( phraseType == PhraseType.POSSESSIVE ) {
					token = children.get(replacement == null ? 0 : 1);
					if( token.getPartOfSpeech() != PartOfSpeech.POSSESSIVE_ENDING )
						throw new SymbolicException("Expecting a possessive ending, not: " + token);
					_log.debug(Markers.SYMBOLIC, "Deleting possessive ending: {}", token);
					token.getParent().removeChild(token);
				}
			}
			
			lastType = phraseType;
		}
	}
	
	private ISymbolicToken getPotentialReference(ISymbolicToken nounParent) {
		List<ISymbolicToken> nounChildren = nounParent.getChildren();
		ISymbolicToken theRef = null;
		PartOfSpeech partOfSpeech = null;
		searchLoop:
		for( int i = 0, len = nounChildren.size(); i < len; ++i ) {
			theRef = nounChildren.get(i);
			partOfSpeech = theRef.getPartOfSpeech();
			if( partOfSpeech == PartOfSpeech.DETERMINER ) {
				theRef = null;
			} else if ( partOfSpeech.isNoun() || partOfSpeech.isPronoun() ) {
				break searchLoop;
			} else {
				_log.warn(Markers.SYMBOLIC, "Unexpected token: {}", theRef);
				return null;
			}
		}
		return theRef;
	}
	
	private List<ISymbolicToken> getVerbPhrasesAfterWhere() {
		RelationExtractor ext = IrisUtil.executeQuery(getVerbPhrasesAfterWhere, state);
		ArrayList<ISymbolicToken> verbPhrases = new ArrayList<ISymbolicToken>(ext.getRelation().size());
		while( ext.nextTuple() ) {
			verbPhrases.add(ext.getToken("?token"));
		}
		return verbPhrases;
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.PARTIAL_LOWERING - 1;
	}

}
