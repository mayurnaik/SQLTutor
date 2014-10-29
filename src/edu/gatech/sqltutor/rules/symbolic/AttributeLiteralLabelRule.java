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

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;
import java.util.List;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.er.ERAttribute.DescriptionType;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.util.Literals;

public class AttributeLiteralLabelRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(AttributeLiteralLabelRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?token", SymbolicType.ATTRIBUTE),
		literal(SymbolicPredicates.parentOf, "?parent", "?token", "?pos")
	);
	
	public AttributeLiteralLabelRule() {
	}
	
	public AttributeLiteralLabelRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		while ( ext.nextTuple() ) {
			AttributeToken token = ext.getToken("?token");
			ISymbolicToken parent = ext.getToken("?parent");
			int pos = ext.getInteger("?pos");
			PartOfSpeech speech = token.getPartOfSpeech();
			String label;
			if( speech.isSingular() )
				label = token.getSingularLabel();
			else if( speech.isPlural() )
				label = token.getPluralLabel();
			else
				throw new SymbolicException("Can't tell if singular or plural: " + token);
			
			ISymbolicToken replacement;
			
			// FIXME what about multi-word labels?
			LiteralToken literal = new LiteralToken(label, token.getPartOfSpeech());
			
			List<ISymbolicToken> siblings = parent.getChildren();
			LiteralToken determiner = null;
			if( needsDeterminer(token) )
				determiner = Literals.the(); // FIXME "a"/"an"?
			
			if( parent.getType() == SymbolicType.ATTRIBUTE_LIST || pos == 0 ) {
				if( determiner != null ) {
					SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);
					seq.addChild(determiner);
					seq.addChild(literal);
					replacement = seq;
				} else {
					replacement = literal;
				}
			} else {
				replacement = literal;
				
				if( determiner != null ) {
					_log.debug(Markers.SYMBOLIC, "Inserting {} in front of {}", determiner, parent);
					siblings.add(0, determiner);
				}
			}
			
			SymbolicUtil.replaceChild(parent, token, replacement);
			_log.debug(Markers.SYMBOLIC, "Replaced token {} with {}", token, replacement);
		}
		return true;
		
	}
	
	private boolean needsDeterminer(ISymbolicToken token) {
		ISymbolicToken before = SymbolicUtil.getPotentialDeterminer(token);
		if( before == null )
			return true;
		PartOfSpeech pos = before.getPartOfSpeech();
		if( pos.isPossessive() || pos == PartOfSpeech.DETERMINER || pos == PartOfSpeech.ADJECTIVE )
			return false;
		return true;
	}
	
	@Override
	protected IQuery getQuery() { return QUERY; }
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.PARTIAL_LOWERING;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
}
