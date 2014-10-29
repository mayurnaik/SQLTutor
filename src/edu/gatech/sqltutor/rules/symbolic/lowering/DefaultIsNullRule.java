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

import java.util.Arrays;
import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.lang.StandardLoweringRule;
import edu.gatech.sqltutor.rules.symbolic.PartOfSpeech;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.util.Literals;

/**
 * Replaces <code><i>x</i> IS NULL</code> or <code><i>x</i> IS NOT NULL</code> with 
 * e.g. "<i>x</i> exists" or "<i>x</i> does not exist".
 */
public class DefaultIsNullRule extends StandardLoweringRule implements
		ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(DefaultIsNullRule.class);
	
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleDefaultIsNull", 1);
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		IrisUtil.literal(PREDICATE, "?token"));
	
	private static final StaticRules staticRules = new StaticRules(DefaultIsNullRule.class);

	public DefaultIsNullRule() {
	}

	public DefaultIsNullRule(int precedence) {
		super(precedence);
	}
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		boolean applied = false;
		final boolean TRACE = _log.isTraceEnabled(Markers.SYMBOLIC);
		String origToken = null;
		while( ext.nextTuple() ) {
			SQLToken token = ext.getToken("?token");
			QueryTreeNode astNode = token.getAstNode();
			if( TRACE )
				origToken = token.toString();
			
			SequenceToken seq = new SequenceToken(PartOfSpeech.VERB_PHRASE);
			seq.addChildren(token.getChildren());
			switch( astNode.getNodeType() ) {
			case NodeTypes.IS_NOT_NULL_NODE:
				seq.addChild(new LiteralToken("exists", PartOfSpeech.VERB_RD_PERSON_SINGULAR_PRESENT));
				break;
			case NodeTypes.IS_NULL_NODE:
				seq.addChildren(Arrays.asList(
					Literals.does(), Literals.not(),
					new LiteralToken("exist", PartOfSpeech.VERB_BASE_FORM)
				));
				break;
			default:
				// should never happen
				throw new SymbolicException("Unexpected node type in token: " + token);
			}
			
			SymbolicUtil.replaceChild(token, seq);
			if( TRACE ) _log.trace(Markers.SYMBOLIC, "Replaced {} with {}", origToken, seq);
			applied = true;
		}
		return applied;
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
}
