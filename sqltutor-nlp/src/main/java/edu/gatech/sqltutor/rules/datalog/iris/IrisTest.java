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
package edu.gatech.sqltutor.rules.datalog.iris;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.asTuple;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deri.iris.EvaluationException;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;
import com.google.common.collect.Maps;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;

public class IrisTest {
	static final Logger _log = LoggerFactory.getLogger(IrisTest.class);
	
	private static final SQLRules sqlRules = SQLRules.getInstance();
	
	public static void main(String[] args) {
		SQLParser p = new SQLParser();
		for( String arg: args ) {
			try {
				_log.info("SQL: {}", arg);
				StatementNode st = p.parseStatement(arg);
				IrisTest test = new IrisTest(st);
				test.evaluate();
			} catch( StandardException e ) {
				e.printStackTrace();
			}
		}
	}
	
	private SelectNode select;
	private SQLFacts sqlFacts = new SQLFacts();

	public IrisTest(StatementNode node) {
		this(QueryUtils.extractSelectNode(node));
	}
	
	public IrisTest(SelectNode select) {
		this.select = select;
		init();
	}
	
	public void evaluate() {
		try {
			long duration = -System.currentTimeMillis();
			Map<IPredicate, IRelation> facts = Maps.newHashMap();
			facts.putAll(sqlFacts.getFacts());
			facts.putAll(sqlRules.getFacts());
			List<IRule> rules = new ArrayList<IRule>(sqlRules.getRules());
			IKnowledgeBase kb = KnowledgeBaseFactory.createKnowledgeBase(facts, rules);
			_log.info("Knowledge based created in {} ms.", duration + System.currentTimeMillis());
			
			IPredicate joinRuleFK = Factory.BASIC.createPredicate("joinRuleFK", 5);
			ILiteral joinRuleLit = Factory.BASIC.createLiteral(true, joinRuleFK, asTuple(
				"?t1", "ssn", "?t2", "manager_ssn", "?eq"
			));
			ILiteral t1Name = literal(SQLPredicates.tableName, "?t1", "employee");
			ILiteral t2Name = literal(SQLPredicates.tableName, "?t2", "employee");
			IQuery query = Factory.BASIC.createQuery(joinRuleLit, t1Name, t2Name);
			
			_log.info("Evaluating: " + query);
			List<IVariable> vars = new ArrayList<IVariable>();
			duration = -System.currentTimeMillis();
			IRelation result = kb.execute(query, vars);
			_log.info("Evaluation took {} ms.", duration + System.currentTimeMillis());
			_log.info("Result: " + result);
			_log.info("Bindings: " + vars);
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
	}
	
	private void init() {
		long duration = -System.currentTimeMillis();
		sqlFacts.generateFacts(select, false);
		_log.info("Fact generation in {} ms.", duration += System.currentTimeMillis());
	}
}
