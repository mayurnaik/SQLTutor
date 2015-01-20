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

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.AndNode;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.OrNode;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SQLParserContext;
import com.akiban.sql.parser.Visitable;
import com.akiban.sql.parser.Visitor;

import edu.gatech.sqltutor.sql.ContextVisitorAdapter;

/**
 * Removes unnecessary boolean constants.  
 * 
 * <ul>
 *   <li><code><i>expr</i> AND TRUE</code> &rArr; <code><i>expr</i></code></li>
 *   <li><code><i>expr</i> OR FALSE</code> &rArr; <code><i>expr</i></code></li>
 * </ul>
 * 
 * @author Jake Cobb
 */
public class RemoveDanglingBooleansNormalizer extends ContextVisitorAdapter
		implements Visitor {
	
	public RemoveDanglingBooleansNormalizer(SQLParserContext context) {
		super(context);
	}

	@Override
	public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
		switch( node.getNodeType() ) {
		case NodeTypes.AND_NODE: {
			AndNode and = (AndNode)node;
			if( and.getRightOperand().isBooleanTrue() ) {
				return and.getLeftOperand();
			} else if( and.getLeftOperand().isBooleanTrue() ) {
				return and.getRightOperand();
			}
			break;
		}
		case NodeTypes.OR_NODE: {
			OrNode or = (OrNode)node;
			if( or.getRightOperand().isBooleanFalse() ) {
				return or.getLeftOperand();
			} else if( or.getLeftOperand().isBooleanFalse() ) {
				return or.getRightOperand();
			}
			break;
		}
		}
		return node;
	}
	
	@Override
	public boolean visitChildrenFirst(Visitable node) {
		return true;
	}
}
