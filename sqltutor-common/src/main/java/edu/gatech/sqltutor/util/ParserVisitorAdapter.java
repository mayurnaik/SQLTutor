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
package edu.gatech.sqltutor.util;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.Visitable;
import com.akiban.sql.parser.Visitor;

/**
 * Common visitor structure that handles casting for the parser 
 * and is top-down by default.
 */
public abstract class ParserVisitorAdapter implements Visitor {
	public ParserVisitorAdapter() { }
	
	/**
	 * Pre-cast version of {@link #visit(Visitable)}.
	 * @param node
	 * @return
	 * @throws StandardException
	 */
	public abstract QueryTreeNode visit(QueryTreeNode node) throws StandardException;
	
	/** @see Visitor#visitChildrenFirst(Visitable) */
	public boolean visitChildrenFirst(QueryTreeNode node) {
		return false;
	}
	
	/** @see Visitor#skipChildren(Visitable) */
	public boolean skipChildren(QueryTreeNode node) {
		return false;
	}

	@Override
	public Visitable visit(Visitable node) throws StandardException {
		return visit((QueryTreeNode)node);
	}

	@Override
	public boolean visitChildrenFirst(Visitable node) {
		return visitChildrenFirst((QueryTreeNode)node);
	}

	@Override
	public boolean stopTraversal() {
		return false;
	}

	@Override
	public boolean skipChildren(Visitable node) throws StandardException {
		return skipChildren((QueryTreeNode)node);
	}
}
