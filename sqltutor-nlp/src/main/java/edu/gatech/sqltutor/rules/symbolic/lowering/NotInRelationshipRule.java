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

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.InRelationshipToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;

public class NotInRelationshipRule extends StandardSymbolicRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(NotInRelationshipRule.class);
	
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleDefaultIsNull", 1);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		IrisUtil.literal(PREDICATE, "?isNull"),
		
		literal(SymbolicPredicates.type, "?entityRef", SymbolicType.TABLE_ENTITY_REF),
		literal(SymbolicPredicates.ancestorOf, "?isNull", "?entityRef", "_"),
		
		literal(SymbolicPredicates.type, "?attr", SymbolicType.ATTRIBUTE),
		literal(SymbolicPredicates.ancestorOf, "?isNull", "?attr", "_"),
		
		literal(SymbolicPredicates.type, "?inRel", SymbolicType.IN_RELATIONSHIP)
	);
	
	public NotInRelationshipRule() {
	}

	public NotInRelationshipRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		final boolean debug = _log.isDebugEnabled(Markers.SYMBOLIC);
		boolean applied = false;
		
		while( ext.nextTuple() ) {
			SQLToken isNullToken = ext.getToken("?isNull");
			InRelationshipToken inRelToken = ext.getToken("?inRel");
			TableEntityRefToken ref = ext.getToken("?entityRef");
			AttributeToken attr = ext.getToken("?attr");
			
			// if the attribute is a key value, get which side of the relationship it belongs to
			boolean leftIsNull = inRelToken.isLeftParticipating();
			boolean rightIsNull = inRelToken.isRightParticipating();
			if( attr.getAttribute().isKey() ) {
				String isNullRefId = ref.getTableEntity().getId();
				String inRelLeftRefId = inRelToken.getLeftEntity().getId();
				String inRelRightRefId = inRelToken.getRightEntity().getId();
				if( isNullRefId.equals(inRelLeftRefId) ) {
					leftIsNull = false;
				}
				if( isNullRefId.equals(inRelRightRefId) ) {
					rightIsNull = false;
				} 
				if( !isNullRefId.equals(inRelLeftRefId) && !isNullRefId.equals(inRelRightRefId) ) {
					// FIXME: where returning false, we could probably catch this in .dlog
					// the value doesn't belong to this relationship
					continue; 
				}
			} else {
				// the value was not a key, so it doesn't imply anything about the relationship
				continue; 
			}
			
			QueryTreeNode isNullNode = isNullToken.getAstNode();
			
			if( isNullNode.getNodeType() == NodeTypes.NOT_NODE ) {
				ISymbolicToken notToken = isNullToken;
				// switch the token to the child IsNullNode or IsNotNullNode
				isNullToken = (SQLToken) notToken.getChildren().get(0);
				isNullNode = isNullToken.getAstNode();
				// inverse the type
				int newType = isNullNode.getNodeType() == NodeTypes.IS_NULL_NODE ? NodeTypes.IS_NOT_NULL_NODE : NodeTypes.IS_NULL_NODE;
				isNullNode.setNodeType(newType);
				if( debug ) _log.debug(Markers.SYMBOLIC, "Replacing {}, as it has been consumed by {}", notToken, isNullToken);
				SymbolicUtil.replaceChild(notToken, isNullToken);
			}
			
			switch( isNullNode.getNodeType() ) {
				case NodeTypes.IS_NOT_NULL_NODE:
					// this is already implied by the relationship
					if( debug ) _log.debug(Markers.SYMBOLIC, "Removing {}, as it is implied by {}", isNullToken, inRelToken);
					isNullToken.getParent().removeChild(isNullToken);
					break;
				case NodeTypes.IS_NULL_NODE:
					inRelToken.setLeftParticipating(leftIsNull);
					inRelToken.setRightParticipating(rightIsNull);
					if( debug ) _log.debug(Markers.SYMBOLIC, "Removing {}. Updated {}", isNullToken, inRelToken);
					isNullToken.getParent().removeChild(isNullToken);
					break;
				default:
					throw new SymbolicException("Unexpected node type in token: " + isNullToken);
			}
			
			applied = true;
		}
		return applied;
	}
	
	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.FRAGMENT_ENHANCEMENT + 1;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
}
