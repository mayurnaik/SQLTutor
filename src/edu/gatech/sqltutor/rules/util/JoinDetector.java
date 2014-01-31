package edu.gatech.sqltutor.rules.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.AndNode;
import com.akiban.sql.parser.BinaryRelationalOperatorNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.FromList;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.JoinNode;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.ResultSetNode;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.TableName;
import com.akiban.sql.parser.ValueNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.util.Pair;

/**
 * Helper to find unconditional joins.
 */
public class JoinDetector {
	private static final Logger _log = LoggerFactory.getLogger(JoinDetector.class);	
	/**
	 * The detected join references.
	 */
	public static class JoinResult {
		private final FromBaseTable firstTable;
		private final FromBaseTable secondTable;
		private final BinaryRelationalOperatorNode joinCondition;
		
		public JoinResult(FromBaseTable firstTable, FromBaseTable secondTable, 
				BinaryRelationalOperatorNode joinCondition) {
			this.firstTable = firstTable;
			this.secondTable = secondTable;
			this.joinCondition = joinCondition;
		}
		
		public FromBaseTable getFirstTable() {
			return firstTable;
		}
		
		public FromBaseTable getSecondTable() {
			return secondTable;
		}
		
		public BinaryRelationalOperatorNode getJoinCondition() {
			return joinCondition;
		}
	}
	
	private Pair<String, String> firstKey;
	private Pair<String, String> secondKey;
	
	private Set<ValueNode> matchedClauses = new HashSet<ValueNode>(4);
	
//	// temporaries
	private SelectNode select;
	private String firstAlias;
	private String secondAlias;
	private JoinResult result;
	
	public JoinDetector(String firstTable, String firstAttr, 
			String secondTable, String secondAttr) {
		this.firstKey = Pair.make(firstTable, firstAttr);
		this.secondKey = Pair.make(secondTable, secondAttr);
	}
	
	public JoinDetector(String first, String second) {
		this.firstKey = QueryUtils.splitKeyParts(first);
		this.secondKey = QueryUtils.splitKeyParts(second);
	}
	
	/**
	 * Attempt to detect this join in the given <code>SELECT</code> statement.
	 * 
	 * @param select the <code>SELECT</code> statement
	 * @return the result if a match was found, <code>null</code> otherwise
	 */
	public JoinResult detect(SelectNode select) {
		if( select == null )
			throw new NullPointerException("select is null");
		
		_log.trace("Looking for {} and {}.", firstKey, secondKey);
		
		try {
			this.select = select;
			
			String firstTableName = firstKey.getFirst(), secondTableName = secondKey.getFirst();
			
			List<FromBaseTable> potentialLefts = new ArrayList<FromBaseTable>(1);
			List<FromBaseTable> potentialRights = new ArrayList<FromBaseTable>(1);
			FromList fromList = select.getFromList();
			for( FromTable fromTable: fromList ) {
				switch( fromTable.getNodeType() ) {
					case NodeTypes.JOIN_NODE:
						if( checkInnerJoin((JoinNode)fromTable) )
							return result;
						break;
					case NodeTypes.FROM_BASE_TABLE:
						if( isBaseTable(firstTableName, fromTable) )
							potentialLefts.add((FromBaseTable)fromTable);
						if( isBaseTable(secondTableName, fromTable) )
							potentialRights.add((FromBaseTable)fromTable);
						break;
				}
			}
			
			// saw some implicit inner joins that might match
			if( potentialLefts.size() > 0 && potentialRights.size() > 0 ) {
				for( FromBaseTable leftTable: potentialLefts ) {
					for( FromBaseTable rightTable: potentialRights ) {
						// not a real join
						if( leftTable == rightTable )
							continue;
						
						if( checkImplicitJoin(leftTable, rightTable) )
							return result;
					}
				}
			}
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		} finally {
			clearTemporaryState();
		}
		
		return null;
	}
	
	/**
	 * Adds a clause to the skip list.
	 * <p>This will prevent the detector from matching on the element again.</p>
	 * @param clause
	 */
	public void skipClause(ValueNode clause) {
		matchedClauses.add(clause);
	}
	
	private void clearTemporaryState() {
		select = null;
		firstAlias = secondAlias = null;
		result = null;
	}

	/**
	 * Checks if the table pair and conditional clause given are a match.
	 * If this returns <code>true</code>, then <code>result</code> will 
	 * be populated.
	 * 
	 * @param firstTable   the potential match of the first table
	 * @param secondTable  the potential match of the second table
	 * @param clause       the <code>WHERE</code> or <code>ON</code> clause
	 * @return <code>true</code> if this is a match
	 * @throws StandardException
	 */
	private boolean checkTablePair(FromBaseTable firstTable, FromBaseTable secondTable, ValueNode clause) 
			throws StandardException {
		firstAlias = firstTable.getExposedName();
		secondAlias = secondTable.getExposedName();
		
		if( clause != null ) {
			Stack<ValueNode> nodesToCheck = new Stack<ValueNode>();
			nodesToCheck.push(clause);
			
			while( !nodesToCheck.isEmpty() ) {
				ValueNode toCheck = nodesToCheck.pop();
				switch(toCheck.getNodeType()) {
					case NodeTypes.BINARY_EQUALS_OPERATOR_NODE:
						BinaryRelationalOperatorNode binop = (BinaryRelationalOperatorNode)toCheck;
						// prevent repeatedly matching the same clause
						if( matchedClauses.contains(binop) ) {
							_log.trace("Rejecting previously seen clause: {}", binop);
							break;
						} else if( checkBinaryEquality(binop) ) {
							result = new JoinResult(firstTable, secondTable, binop);
							
							if( _log.isDebugEnabled() ) {
								_log.debug("Matched on operator: {} (0x{})", binop.getOperator(), System.identityHashCode(binop));
							}
							
							return true;
						}
						break;
					case NodeTypes.AND_NODE: {
						AndNode and = (AndNode)toCheck;
						nodesToCheck.push(and.getLeftOperand());
						nodesToCheck.push(and.getRightOperand());
						break;
					}
				}
			}
		}
		
		return false;
	}

	
	private boolean isBaseTable(String tableName, ResultSetNode node) 
			throws StandardException {
		if( node instanceof FromBaseTable ) {
			FromBaseTable fromBaseTable = (FromBaseTable)node;
			TableName fromName = fromBaseTable.getOrigTableName();
			if( fromName == null ) {
				_log.trace("No orig table name, using exposed table name.");
				fromName = fromBaseTable.getExposedTableName();
			}
			if( tableName.equals(fromName.getTableName()) )
				return true;
		}
		return false;
	}
	
	private boolean checkEqualsConstraint(String leftTable, String leftAttr, 
			String rightTable, String rightAttr) {
		String firstAttr = firstKey.getSecond(), secondAttr = secondKey.getSecond();
		return (leftTable.equals(this.firstAlias) && leftAttr.equals(firstAttr)
				&& rightTable.equals(this.secondAlias) && rightAttr.equals(secondAttr)) 
				|| // side of equality doesn't matter
				(rightTable.equals(this.firstAlias) &&  rightAttr.equals(firstAttr)
				&& leftTable.equals(this.secondAlias) && leftAttr.equals(secondAttr));
	}
	
	/**
	 * Check if a binary equality matches our key pair.
	 */
	private boolean checkBinaryEquality(BinaryRelationalOperatorNode eq) {
		if( eq.getOperatorType() != BinaryRelationalOperatorNode.EQUALS_RELOP ) {
			_log.trace("Rejected operator type '{}'", eq.getOperator());
			return false;
		}
		ValueNode leftOp = eq.getLeftOperand(), rightOp = eq.getRightOperand();
		if( NodeTypes.COLUMN_REFERENCE != leftOp.getNodeType() || 
				NodeTypes.COLUMN_REFERENCE != rightOp.getNodeType() ) {
			_log.trace("Rejected join based on '=' operator types.");
			return false;
		}
		
		ColumnReference leftRef = (ColumnReference)leftOp, rightRef = (ColumnReference)rightOp;
		
		String leftRefTable = leftRef.getTableName(), rightRefTable = rightRef.getTableName();
		String leftColName = leftRef.getColumnName(), rightColName = rightRef.getColumnName();
		
		if( !checkEqualsConstraint(leftRefTable, leftColName, rightRefTable, rightColName) ) {
			_log.trace("Rejected join based on columns used, saw {}.{} = {}.{}",
				leftRefTable, leftColName, rightRefTable, rightColName);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check for an explicit join of the form <code>SELECT ... FROM t1 INNER JOIN t2 ON t1.key=t2.key</code>.
	 * 
	 * @param join the join node
	 * @return <code>true</code> if this is a matching join
	 * @throws StandardException
	 */
	private boolean checkInnerJoin(JoinNode join) throws StandardException {
		_log.trace("Checking inner join node.");
		ResultSetNode leftResult = join.getLogicalLeftResultSet(),
				rightResult = join.getLogicalRightResultSet();
		
		String firstTable = firstKey.getFirst(), secondTable = secondKey.getFirst();
		
		ValueNode on = join.getJoinClause();
		
		// check both orders
		if( isBaseTable(firstTable, leftResult) && isBaseTable(secondTable, rightResult) ) {
			_log.trace("Checking left/right combination.");
			if( checkTablePair((FromBaseTable)leftResult, (FromBaseTable)rightResult, on) )
				return true;
		}
		if( isBaseTable(firstTable, rightResult) && isBaseTable(secondTable, leftResult) ) {
			_log.trace("Checking right/left combination.");
			if( checkTablePair((FromBaseTable)rightResult, (FromBaseTable)leftResult, on) )
				return true;
		}
		
		return false;
	}
	
	/**
	 * Check for a join of the form <code>SELECT ... FROM t1, t2 WHERE t1.key=t2.key</code>.
	 * 
	 * @param leftRef   the left table <code>t1</code>
	 * @param rightRef  the right table <code>t2</code>
	 * @return true if there is a matching join for this pair
	 * @throws StandardException
	 */
	private boolean checkImplicitJoin(FromBaseTable leftRef, FromBaseTable rightRef) 
			throws StandardException {
		_log.trace("Checking implicit join, left={}, right={}", leftRef, rightRef);
		return checkTablePair(leftRef, rightRef, select.getWhereClause());
	}
}
