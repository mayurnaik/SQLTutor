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
import edu.gatech.sqltutor.rules.symbolic.tokens.InRelationshipToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.NotInRelationshipToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;

public class NotInRelationshipRule extends StandardSymbolicRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(NotInRelationshipRule.class);
	
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleDefaultIsNull", 1);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		IrisUtil.literal(PREDICATE, "?isNull"),
		
		literal(SymbolicPredicates.type, "?seq", SymbolicType.SEQUENCE),
		literal(SymbolicPredicates.parentOf, "?isNull", "?seq", "_"),
		
		literal(SymbolicPredicates.type, "?entityRef", SymbolicType.TABLE_ENTITY_REF),
		literal(SymbolicPredicates.parentOf, "?seq", "?entityRef", "_"),
		
		literal(SymbolicPredicates.type, "?attr", SymbolicType.ATTRIBUTE),
		literal(SymbolicPredicates.parentOf, "?seq", "?attr", "_"),
		
		literal(SymbolicPredicates.type, "?inRel", SymbolicType.IN_RELATIONSHIP)
	);
	
	public NotInRelationshipRule() {
	}

	public NotInRelationshipRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		final boolean DEBUG = _log.isDebugEnabled(Markers.SYMBOLIC);

		while( ext.nextTuple() ) {
			SQLToken isNullToken = ext.getToken("?isNull");
			InRelationshipToken inRelToken = ext.getToken("?inRel");
			TableEntityRefToken ref = ext.getToken("?entityRef");
			AttributeToken attr = ext.getToken("?attr");
			
			// TODO: Most of this could be moved into the datalog query
			// if the attribute is a key value, get which side of the relationship it belongs to
			boolean leftEntityIsNull;
			if ( attr.getAttribute().isKey() ) {
				String id = ref.getTableEntity().getId();
				if ( id.equals(inRelToken.getLeftEntity().getId()) ) {
					leftEntityIsNull = true;
				} else if ( id.equals(inRelToken.getRightEntity().getId()) ) {
					leftEntityIsNull = false;
				} else {
					// the value doesn't belong to this relationship
					return false; 
				}
			} else {
				// the value was not a key, so it doesn't imply anything about the relationship
				return false; 
			}
			
			QueryTreeNode isNullNode = isNullToken.getAstNode();
			
			switch( isNullNode.getNodeType() ) {
				case NodeTypes.IS_NOT_NULL_NODE:
					// this is already implied by the relationship
					isNullToken.getParent().removeChild(isNullToken);
					if( DEBUG ) _log.debug(Markers.SYMBOLIC, "Removing {}, as it is implied by {}", isNullToken, inRelToken);
					break;
				case NodeTypes.IS_NULL_NODE:
					NotInRelationshipToken notInRelToken = new NotInRelationshipToken(inRelToken.getLeftEntity(), 
							inRelToken.getRightEntity(), inRelToken.getRelationship(), leftEntityIsNull);
					if( DEBUG ) _log.debug(Markers.SYMBOLIC, "Replacing {} and {} with {}", inRelToken, isNullToken, notInRelToken);
					SymbolicUtil.replaceChild(inRelToken, notInRelToken);
					isNullToken.getParent().removeChild(isNullToken);
					break;
				default:
					throw new SymbolicException("Unexpected node type in token: " + isNullToken);
			}
		}
		return true;
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
