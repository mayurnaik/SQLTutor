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

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.builtin;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.builtins.LessBuiltin;
import org.deri.iris.builtins.NotEqualBuiltin;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityRefToken;

public class TableEntityRefNeedsIdRule extends StandardSymbolicRule {
	private static final Logger _log = LoggerFactory.getLogger(TableEntityRefNeedsIdRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		// two table entity references
		literal(SymbolicPredicates.type, "?ref1", SymbolicType.TABLE_ENTITY_REF),
		literal(SymbolicPredicates.type, "?ref2", SymbolicType.TABLE_ENTITY_REF),
		// referencing different tables
		literal(SymbolicPredicates.refsTableEntity, "?ref1", "?tableEntity1"),
		literal(SymbolicPredicates.refsTable, "?tableEntity1", "?table1"),
		literal(SymbolicPredicates.refsTableEntity, "?ref2", "?tableEntity2"),
		literal(SymbolicPredicates.refsTable, "?tableEntity2", "?table2"),
		literal(builtin(NotEqualBuiltin.class, "?table1", "?table2")),
		// with the same label
		literal(SymbolicPredicates.singularLabel, "?tableEntity1", "?label"),
		literal(SymbolicPredicates.singularLabel, "?tableEntity2", "?label"),
		// don't return in both orders
		literal(builtin(LessBuiltin.class, "?ref1", "?ref2"))
	);

	public TableEntityRefNeedsIdRule() {
	}

	public TableEntityRefNeedsIdRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		boolean applied = false;
		
		while( ext.nextTuple() ) {
			TableEntityRefToken ref1 = ext.getToken("?ref1"),
			                    ref2 = ext.getToken("?ref2");
			applied |= setNeedsId(ref1);
			applied |= setNeedsId(ref2);
		}
		return applied;
	}
	
	private boolean setNeedsId(TableEntityRefToken ref) {
		if( ref.getNeedsId() )
			return false;
		ref.setNeedsId(true);
		_log.trace(Markers.SYMBOLIC, "Needs id: {}", ref);
		return true;
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.LOWERING + 1; // want to fire before TableEntityRefLiteralRule
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}

}
