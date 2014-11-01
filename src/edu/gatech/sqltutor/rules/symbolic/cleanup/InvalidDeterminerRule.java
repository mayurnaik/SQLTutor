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

		while( ext.nextTuple() ) {
			final LiteralToken determiner = ext.getToken("?det");
			boolean deleteDeterminer = false;
			
			ISymbolicToken before = SymbolicUtil.getPrecedingToken(determiner);
			if( before != null ) {
				PartOfSpeech bpos = before.getPartOfSpeech();
				if( bpos.isProperNoun() || bpos.isDeterminer() || bpos.isAdjective() || bpos.isPronoun() ) {
					_log.warn("Deleting determiner {} due to preceding token {} with part of speech {}.", determiner, before, bpos);
					deleteDeterminer = true;
				}
			}
			
			if( deleteDeterminer == false ) {
				ISymbolicToken after = SymbolicUtil.getSucceedingToken(determiner);
				if ( after != null ) {
					PartOfSpeech apos = after.getPartOfSpeech();
					
					// FIXME: Sometimes we can not include a determiner based on if the token is plural.
					// Perhaps has something to do with "count vs uncount" nouns
					if( apos.isPronoun() ) {
						_log.warn("Deleting determiner {} due to succeeding token {} with part of speech {}.", determiner, after, apos);
						deleteDeterminer = true;
					}
					
					if( deleteDeterminer == false ) {
						// FIXME: Proper nouns shouldn't always have a determiner.
						ISymbolicToken afterAfter = SymbolicUtil.getSucceedingToken(after);
						if( apos.isProperNoun() && afterAfter != null &&
								SymbolicUtil.getSucceedingToken(after).getPartOfSpeech().isPossessive()) {
							_log.warn("Deleting determiner {} due to succeeding possessive token {} with part of speech {}.", determiner, after, apos);
							deleteDeterminer = true;
						}
					}
				}
			}
			
			if( deleteDeterminer ) {
				determiner.getParent().removeChild(determiner);
				applied = true;
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
