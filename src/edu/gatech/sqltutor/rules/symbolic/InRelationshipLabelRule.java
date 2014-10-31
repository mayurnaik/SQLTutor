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

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.er.ERRelationship;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.InRelationshipToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.util.NLUtil;

public class InRelationshipLabelRule extends StandardSymbolicRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(InRelationshipLabelRule.class);
	
	private static final String PREFIX = "ruleInRelationshipLabel_";
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleInRelationshipLabel", 4);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?inrel", "?leftEntity", "?rightEntity", "?which")
	);
	
	private static final StaticRules staticRules = new StaticRules(InRelationshipLabelRule.class);

	public InRelationshipLabelRule() {
	}

	public InRelationshipLabelRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		SymbolicQueries queries = state.getQueries();
		
		while( ext.nextTuple() ) {
			InRelationshipToken inrel = ext.getToken("?inrel");
			TableEntityToken leftEntity = ext.getToken("?leftEntity"),
				rightEntity = ext.getToken("?rightEntity");
			
			ERRelationship relationship = inrel.getRelationship();
			
			if( !inrel.isLeftParticipating() || !inrel.isRightParticipating() )
				return false;
			
			boolean isLeftPossessor = ext.getInteger("?which") == 0;
			TableEntityToken possessorEntity, possessedEntity;
			String possessedLabel;
			if( isLeftPossessor ) {
				possessorEntity = leftEntity;
				possessedEntity = rightEntity;
				possessedLabel = relationship.getRightEdge().getConstraint().getLabel();
			} else {
				possessorEntity = rightEntity;
				possessedEntity = leftEntity;
				possessedLabel = relationship.getLeftEdge().getConstraint().getLabel();
			}
			
			// don't process if no special label or label change is a no-op
			if( possessedLabel == null || possessedLabel.isEmpty() 
					|| possessedLabel.equalsIgnoreCase(possessedEntity.getSingularLabel()) ) {
				continue;
			}
			possessedLabel = possessedLabel.toLowerCase();
			// FIXME should probably skip if current label is non-default as well
			
			possessedEntity.setSingularLabel(possessedLabel);
			possessedEntity.setPluralLabel(NLUtil.pluralize(possessedLabel)); // FIXME allow ER override
			
			inrel.getParent().removeChild(inrel);
			List<TableEntityRefToken> refs = queries.getTableEntityReferences(possessedEntity);
			for( TableEntityRefToken ref: refs ) {
				makePossessiveRef(ref, possessorEntity);
			}
			
			return true;
		}
		return false;
	}

	private void makePossessiveRef(TableEntityRefToken ref,
			TableEntityToken possessor) {
		TableEntityRefToken possessed = new TableEntityRefToken(possessor);
		
		SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);
		seq.addChild(possessed);

		String posExpr = possessor.getSingularLabel().endsWith("s") ? "'" : "'s";
		LiteralToken literal = new LiteralToken(posExpr, PartOfSpeech.POSSESSIVE_ENDING); 
		seq.addChild(literal);
		
		seq.addChild(new TableEntityRefToken(ref));
		
		SymbolicUtil.replaceChild(ref, seq);
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
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.FRAGMENT_ENHANCEMENT;
	}

}
