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
import edu.gatech.sqltutor.rules.util.Literals;
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
			InRelationshipToken inRelToken = ext.getToken("?token");
			
			ERRelationship rel = inRelToken.getRelationship();
			String negatedSingularForm = rel.getMetadata() != null ? rel.getMetadata().getNegatedSingularVerbForm() : null;
			String negatedPluralForm = rel.getMetadata() != null ? rel.getMetadata().getNegatedPluralVerbForm() : null;
			boolean hasNegatedForm = negatedSingularForm != null && negatedPluralForm != null;
			
			TableEntityRefToken leftRef = new TableEntityRefToken(inRelToken.getLeftEntity());
			TableEntityRefToken rightRef = new TableEntityRefToken(inRelToken.getRightEntity());		
			
			SequenceToken seq = new SequenceToken(PartOfSpeech.VERB_PHRASE);
			if( !(inRelToken.isLeftParticipating() ^ inRelToken.isRightParticipating()) ) {
				// TODO is this ordering fixed?
				seq.addChild(leftRef);
				String verb = rel.getVerbForm();
				if( verb == null )
					verb = rel.getName().toLowerCase().replace('_', ' ');
				verbalizeRelationship(NLUtil.getVerbForm(verb), seq);
				seq.addChild(rightRef);
			} else if( hasNegatedForm ) {
				TableEntityRefToken participater = null;
				TableEntityRefToken nonparticipater = null;
				if( inRelToken.isLeftParticipating() ) {
					participater = leftRef;
					nonparticipater = rightRef;
				} else {
					participater = rightRef;
					nonparticipater = leftRef;
				}
				String negatedRelationshipLabel = participater.getPartOfSpeech() == PartOfSpeech.NOUN_SINGULAR_OR_MASS ? rel.getMetadata().getNegatedSingularVerbForm() :
																											rel.getMetadata().getNegatedPluralVerbForm();
				seq.addChild(participater);
				verbalizeRelationship(NLUtil.getVerbForm(negatedRelationshipLabel), seq);
				seq.addChild(Literals.any());
				seq.addChild(nonparticipater);
			} else {
				TableEntityRefToken participater = null;
				TableEntityRefToken nonparticipater = null;
				String nonparticipaterLabel = null;
				if( inRelToken.isLeftParticipating() ) {
					participater = leftRef;
					nonparticipater = rightRef;
					nonparticipaterLabel = rel.getRightEdge().getConstraint().getLabel().toLowerCase();
				} else {
					participater = rightRef;
					nonparticipater = leftRef;
					nonparticipaterLabel = rel.getLeftEdge().getConstraint().getLabel().toLowerCase();
				}
				if( nonparticipaterLabel == null || nonparticipaterLabel.isEmpty() 
						|| nonparticipaterLabel.equalsIgnoreCase(nonparticipater.getTableEntity().getSingularLabel()) ) {
					continue;
				}
				
				seq.addChild(participater);
				seq.addChild(Literals.does());
				seq.addChild(Literals.not());
				seq.addChild(Literals.have());
				seq.addChild(Literals.any());
				seq.addChild(new LiteralToken(nonparticipaterLabel, nonparticipater.getPartOfSpeech()));
			}
			
			if( DEBUG ) _log.debug(Markers.SYMBOLIC, "Replacing {} with {}", inRelToken, seq);
			SymbolicUtil.replaceChild(inRelToken, seq);
		}
		return true;
	}
	
	private void verbalizeRelationship(String verb, ISymbolicToken parent) {
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
