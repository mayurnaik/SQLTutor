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
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.NotInRelationshipToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;
import edu.gatech.sqltutor.rules.util.Literals;

public class NotInRelationshipLoweringRule extends StandardSymbolicRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(NotInRelationshipLoweringRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?notInRelToken", SymbolicType.NOT_IN_RELATIONSHIP)
	);

	public NotInRelationshipLoweringRule() {
	}

	public NotInRelationshipLoweringRule(int precedence) {
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
			NotInRelationshipToken notInRelToken = ext.getToken("?notInRelToken");
			TableEntityRefToken leftRef = new TableEntityRefToken(notInRelToken.getLeftEntity());
			TableEntityRefToken rightRef = new TableEntityRefToken(notInRelToken.getRightEntity());

			SequenceToken seq = new SequenceToken(PartOfSpeech.VERB_PHRASE);
			seq.addChild(rightRef);
			seq.addChild(Literals.does());
			seq.addChild(Literals.not());
			seq.addChild(Literals.have());
			//FIXME: do we want to take into account "any" vs "a" ?
			seq.addChild(leftRef);
			
			if( DEBUG ) _log.debug(Markers.SYMBOLIC, "Replacing {} with {}", notInRelToken, seq);
			SymbolicUtil.replaceChild(notInRelToken, seq);
		}
		return true;
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
