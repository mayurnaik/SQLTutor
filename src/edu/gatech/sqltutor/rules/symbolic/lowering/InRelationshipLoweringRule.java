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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
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
			
			TableEntityRefToken leftRef = new TableEntityRefToken(inRelToken.getLeftEntity());
			TableEntityRefToken rightRef = new TableEntityRefToken(inRelToken.getRightEntity());
			TableEntityRefToken participater = null;
			TableEntityRefToken nonparticipater = null;
			String nonparticipaterLabel = null;
			if( inRelToken.isLeftParticipating() ) {
				participater = leftRef;
				nonparticipater = rightRef;
				nonparticipaterLabel = rel.getRightEdge().getConstraint().getLabel();
			} else {
				participater = rightRef;
				nonparticipater = leftRef;
				nonparticipaterLabel = rel.getLeftEdge().getConstraint().getLabel();
			}
		
			SequenceToken seq = new SequenceToken(PartOfSpeech.VERB_PHRASE);
			if( !(inRelToken.isLeftParticipating() ^ inRelToken.isRightParticipating()) ) {
				// TODO is this ordering fixed?
				seq.addChild(leftRef);
				verbalizeRelationship(rel, seq, leftRef.getPartOfSpeech(), false);
				seq.addChild(Literals.any());
				seq.addChild(rightRef);
			} else if( nonparticipaterLabel != null && !nonparticipaterLabel.isEmpty() &&
					!nonparticipaterLabel.equalsIgnoreCase(nonparticipater.getTableEntity().getSingularLabel()) ) {
				seq.addChild(participater);
				seq.addChild(Literals.does());
				seq.addChild(Literals.not());
				seq.addChild(Literals.have());
				seq.addChild(Literals.any());
				seq.addChild(new LiteralToken(nonparticipaterLabel.toLowerCase(), nonparticipater.getPartOfSpeech()));
			} else {
				seq.addChild(participater);
				verbalizeRelationship(rel, seq, participater.getPartOfSpeech(), true);
				seq.addChild(Literals.any());
				seq.addChild(nonparticipater);
			}
			
			if( DEBUG ) _log.debug(Markers.SYMBOLIC, "Replacing {} with {}", inRelToken, seq);
			SymbolicUtil.replaceChild(inRelToken, seq);
		}
		return true;
	}
	
	private void verbalizeRelationship(ERRelationship rel, ISymbolicToken parent, PartOfSpeech beforePartOfSpeech, boolean negated) {
		String verb = null;
		if( negated ) {
			verb = beforePartOfSpeech == PartOfSpeech.NOUN_SINGULAR_OR_MASS ? rel.getMetadata().getNegatedSingularVerbForm() :
																				rel.getMetadata().getNegatedPluralVerbForm();
			if( verb == null ) {
				String unnegatedVerb = beforePartOfSpeech == PartOfSpeech.NOUN_SINGULAR_OR_MASS ? rel.getMetadata().getAlternateSingularVerbForm() :
																					rel.getMetadata().getAlternatePluralVerbForm();
				if( verb == null)
					unnegatedVerb = rel.getVerbForm() == null ? rel.getName().toLowerCase().replace('_', ' ') : rel.getVerbForm();
				
				String[] parts = unnegatedVerb.split("\\s+");
				switch(parts[0]) {
					case "do":
						verb = unnegatedVerb.replaceFirst("do", "do not");
						break;
					case "does":
						verb = unnegatedVerb.replaceFirst("does", "does not");
						break;
					case "is":
						verb = unnegatedVerb.replaceFirst("is", "is not");
						break;
					default:
						throw new SymbolicException("Unable to find negated verbalization for: " + unnegatedVerb);
				}
			}
		} else {
			verb = beforePartOfSpeech == PartOfSpeech.NOUN_SINGULAR_OR_MASS ? rel.getMetadata().getAlternateSingularVerbForm() :
																				rel.getMetadata().getAlternatePluralVerbForm();
			if( verb == null ) 
				verb = rel.getVerbForm() == null ? rel.getName().toLowerCase().replace('_', ' ') : rel.getVerbForm();
		}
		
		String[] parts = verb.split("\\s+");
		for(int i = 0; i < parts.length; i++) {
			try {
				Method method = Literals.class.getMethod(parts[i]);
				LiteralToken verbToken = (LiteralToken) method.invoke(Literals.getInstance());
				parent.addChild(verbToken);
				continue;
			} catch (NoSuchMethodException e) {}
			catch (SecurityException e) {}
			catch (IllegalAccessException e) {}
			catch (IllegalArgumentException e) {} 
			catch (InvocationTargetException e) {}

			// try to guess:
			// FIXME: For the sake of correctness, we probably shouldn't be guessing.
			
			// likely a "works for", "works on", "works with"... case
			if( parts.length == 2 && i == 1 ) {
				parent.addChild(new LiteralToken(parts[i], PartOfSpeech.PREPOSITION_OR_SUBORDINATING_CONJUNCTION));
				continue;
			}
			
			// guess based off of preceding token's plurality
			LiteralToken verbToken = new LiteralToken(parts[i], beforePartOfSpeech == PartOfSpeech.NOUN_SINGULAR_OR_MASS ? PartOfSpeech.VERB_RD_PERSON_SINGULAR_PRESENT : PartOfSpeech.VERB_BASE_FORM);
			parent.addChild(verbToken);
		}
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
