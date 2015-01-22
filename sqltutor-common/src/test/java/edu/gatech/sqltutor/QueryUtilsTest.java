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
package edu.gatech.sqltutor;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.CursorNode;
import com.akiban.sql.parser.FromList;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultColumnList;
import com.akiban.sql.parser.ResultSetNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SelectNode;

import edu.gatech.sqltutor.rules.util.ParserVisitorAdapter;

public class QueryUtilsTest {

	@Test
	public void testFindParent() throws Exception {
		SQLParser parser = QueryUtils.newParser(null);
		String sql = "SELECT a FROM t WHERE b=c GROUP BY d ORDER BY e";
		CursorNode cursor = (CursorNode)parser.parseStatement(sql);
		
		QueryTreeNode parent = QueryUtils.findParent(cursor, cursor);
		Assert.assertNull("Statment node should have no parent.", parent);
		
		assertHasFoundParent(cursor, cursor, cursor.getFetchFirstClause());
		assertHasFoundParent(cursor, cursor, cursor.getOffsetClause());
		assertHasFoundParent(cursor, cursor, cursor.getOrderByList());
		
		ResultSetNode resultSet = cursor.getResultSetNode();
		Assert.assertNotNull("Should have a result set.", resultSet);
		assertHasFoundParent(cursor, cursor, resultSet);
		
		SelectNode select = (SelectNode)resultSet;
		ResultColumnList resultColumns = select.getResultColumns();
		assertHasFoundParent(cursor, select, resultColumns);
		FromList fromList = select.getFromList();
		assertHasFoundParent(cursor, select, fromList);
		
		final ArrayList<ColumnReference> colRefs = new ArrayList<>();
		cursor.accept(new ParserVisitorAdapter() {
			@Override
			public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
				if (node instanceof ColumnReference)
					colRefs.add((ColumnReference)node);
				return node;
			}
		});
		for (ColumnReference ref: colRefs)
			assertLeadsToRoot(cursor, ref);
		
	}
	
	private void assertLeadsToRoot(QueryTreeNode root, QueryTreeNode node) {
		QueryTreeNode parent = node;
		while (parent != root) {
			parent = QueryUtils.findParent(root, parent);
			Assert.assertNotNull("Did not find expected root.", parent);
		}
	}
	
	private void assertHasFoundParent(QueryTreeNode root, QueryTreeNode parent, QueryTreeNode child) {
		if (child != null) {
			QueryTreeNode foundParent = QueryUtils.findParent(root, child);
			Assert.assertEquals("Found wrong parent node.", parent, foundParent);
		}
	}

}
