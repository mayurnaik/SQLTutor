/*
 *   Copyright (c) 2015 Program Analysis Group, Georgia Tech
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
package edu.gatech.sqltutor.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.akiban.sql.StandardException;
import com.akiban.sql.compiler.BooleanNormalizer;
import com.akiban.sql.compiler.TypeComputer;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SQLParserContext;
import com.akiban.sql.parser.StatementNode;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;

public class QueryNormalizer {

	public QueryNormalizer() {
	}

	public String normalize(String query) throws SQLTutorException {
		SQLParser parser = new SQLParser();
		try {
			StatementNode statement = parser.parseStatement(query);
			statement = normalize(statement);
			return QueryUtils.nodeToString(statement);
		} catch (StandardException e) {
			throw new SQLTutorException(e);
		}
	}
	
	public List<String> normalize(List<String> queries) throws SQLTutorException {
		List<String> normalizedQueries = new ArrayList<String>(queries.size());
		for(String query : queries) {
			normalizedQueries.add(normalize(query));
		}
		return normalizedQueries;
	}
	
	public Multiset<String> getNormalizedSet(List<String> queries) throws SQLTutorException {
		List<String> normalizedQueries = normalize(queries);
		Multiset<String> normalizedSet = HashMultiset.create(normalizedQueries);
		return normalizedSet;
	}
	
	public StatementNode normalize(StatementNode node) {
		SQLParserContext context = node.getParserContext();
		try {
			node.accept(new BooleanNormalizer(context));
			node.accept(new TypeComputer());
			node.accept(new ComparisonDirectionNormalizer(context));
			node.accept(new RemoveDanglingBooleansNormalizer(context));
			node.accept(new ExpressionSorter(context));
		} catch (StandardException e ) {
			throw new SQLTutorException(e);
		}
		return node;
	}
}
