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
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicQueries;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;

public class TableEntityRefLiteralRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(TableEntityRefLiteralRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?ref", SymbolicType.TABLE_ENTITY_REF)
	);
	
	public TableEntityRefLiteralRule() {}
	public TableEntityRefLiteralRule(int precedence) { super(precedence); }
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		// FIXME need to account for context, for now just substitute the labels
		boolean applied = false;
		
		final SymbolicQueries queries = state.getQueries();
		
		while( ext.nextTuple() ) {
			TableEntityRefToken ref = ext.getToken("?ref");
			TableEntityToken token = ref.getTableEntity();
			
			PartOfSpeech pos = ref.getPartOfSpeech();
			boolean isSingular;
			switch( pos ) {
			case NOUN_SINGULAR_OR_MASS:
			case PROPER_NOUN_SINGULAR:
				isSingular = true;
				break;
			case NOUN_PLURAL:
			case PROPER_NOUN_PLURAL:
				isSingular = false;
				break;
			default:
				throw new SymbolicException("FIXME: Can't handle part of speech: " + pos);
			}

			ISymbolicToken replacementToken = null;

			if( ref.getNeedsId() ) {
				TableEntityRefToken earliest = queries.getEarliestRef(token);
				String idLabel = "_" + token.getId() + "_";
				LiteralToken idToken = new LiteralToken(idLabel, 
					isSingular ? PartOfSpeech.PROPER_NOUN_SINGULAR : PartOfSpeech.PROPER_NOUN_PLURAL);
				
				if( ref == earliest ) {
					_log.trace(Markers.SYMBOLIC, "Replacing earliest id'd ref: {}", ref);
					SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);
					seq.addChild(new LiteralToken(isSingular ? token.getSingularLabel() : token.getPluralLabel(), 
							ref.getPartOfSpeech()));
					seq.addChild(idToken);
					replacementToken = seq;
				} else {
					_log.trace(Markers.SYMBOLIC, "Replacing non-earliest id'd ref: {}", ref);
					replacementToken = idToken;
				}
				
			} else {
				_log.trace(Markers.SYMBOLIC, "Non-id reference: {}", ref);
				replacementToken = new LiteralToken(isSingular ? token.getSingularLabel() : token.getPluralLabel(), ref.getPartOfSpeech());
			}
			
			SymbolicUtil.replaceChild(ref, replacementToken);
			
			_log.debug(Markers.SYMBOLIC, "Replaced token {} with {}", ref, replacementToken);
			applied = true;
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
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.LOWERING;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
}
