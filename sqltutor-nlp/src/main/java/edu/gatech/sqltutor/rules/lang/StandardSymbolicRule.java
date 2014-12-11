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
package edu.gatech.sqltutor.rules.lang;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.storage.IRelation;

import edu.gatech.sqltutor.rules.SymbolicState;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;

/**
 * Base for standard symbolic rules that evaluate a single query and 
 * act if the result is non-empty.
 */
public abstract class StandardSymbolicRule extends AbstractSymbolicRule {

	public StandardSymbolicRule() {
	}
	
	public StandardSymbolicRule(int precedence) {
		super(precedence);
	}
	
	/**
	 * Runs the query for this rule and calls {@link #handleResult(IRelation, RelationExtractor)} 
	 * if the resulting relation is non-empty.
	 */
	@Override
	public boolean apply(SymbolicState state) {
		this.state = state;
		try {
			IQuery query = getQuery();
			List<IVariable> bindings = new ArrayList<IVariable>(getVariableEstimate());
			IRelation relation = state.getKnowledgeBase().execute(query, bindings);
			if( relation.size() < 1 )
				return false;
			
			RelationExtractor ext = new RelationExtractor(bindings);
//			ext.setNodeMap(state.getSqlState().getSqlFacts().getNodeMap());
			ext.setTokenMap(state.getSymbolicFacts().getTokenMap());
			ext.setRelation(relation);
			
			return handleResult(relation, ext);
		} catch( EvaluationException e ) {
			throw new SymbolicException(e);
		} finally {
			this.state = null;
		}
	}
	
	/**
	 * Returns the query to evaluate for this rule.
	 * @return the query
	 */
	protected abstract IQuery getQuery();
	
	/**
	 * Called to perform the rewrite action, only if the query relation 
	 * contains at least one result.
	 * 
	 * @param relation the resulting relation
	 * @param ext      the variable extractor
	 * @return if the rule was actually applied
	 */
	protected abstract boolean handleResult(IRelation relation, RelationExtractor ext);
	
	/**
	 * Returns the estimated number of query variables.
	 */
	protected int getVariableEstimate() { return 5; }
}
