package edu.gatech.sqltutor.rules.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.AndNode;
import com.akiban.sql.parser.BinaryComparisonOperatorNode;
import com.akiban.sql.parser.BinaryListOperatorNode;
import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.BinaryRelationalOperatorNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.ConstantNode;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.FromList;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.ResultColumnList;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.TableName;
import com.akiban.sql.parser.UnaryComparisonOperatorNode;
import com.akiban.sql.parser.UnaryOperatorNode;
import com.akiban.sql.parser.ValueNode;
import com.akiban.sql.parser.ValueNodeList;

import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.mapping.ERForeignKeyJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap;
import edu.gatech.sqltutor.rules.er.mapping.ERLookupTableJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;

public class ForeignKeyReplacer {
	private static final Logger log = LoggerFactory
			.getLogger(ForeignKeyReplacer.class);
	private ERMapping mapping;

	public ForeignKeyReplacer(ERMapping mapping) {
		this.mapping = mapping;
	}

	public void resolve(SelectNode select) {
		FromList fromList = select.getFromList();
		ResultColumnList resultColumns = select.getResultColumns();
		List<ValueNode> whereNodes = getWhereNodes(select.getWhereClause());

		FromList toAddToFromList = new FromList();
		for (int i = 0; i < fromList.size(); i++) {
			FromBaseTable fromTable = (FromBaseTable) fromList.get(i);

			String tableAlias = fromTable.getExposedName();
			ResultColumnList tablesResultColumns = getResultColumnsForTableAlias(
					tableAlias, resultColumns);
			if (tablesResultColumns.size() == 0
					&& (whereNodes == null || whereNodes.size() == 0))
				continue;

			Map<Integer, ResultColumn> addAndRemoveMap = new HashMap<Integer, ResultColumn>();
			for (int j = 0; j < tablesResultColumns.size(); j++) {
				ResultColumn resultColumn = tablesResultColumns.get(j);
				if (!(resultColumn.getNodeType() == NodeTypes.ALL_RESULT_COLUMN)) {
					String attrName = fromTable.getOrigTableName()
							.getTableName()
							+ "."
							+ resultColumn.getExpression().getColumnName();
					ERAttribute erAttr = mapping.getAttribute(attrName);
					// If there is no mapped attribute, then we've found a foreign key.
					if (erAttr == null) {
						// get the corresponding primary key
						ERJoinMap join = mapping.getJoin(attrName);
						String pk = null;
						if (join instanceof ERForeignKeyJoin)
							pk = ((ERForeignKeyJoin) join).getKeyPair()
									.getPrimaryKey();
						else
							pk = ((ERLookupTableJoin) join)
									.getCorrespondingPrimaryKey(attrName);
						// split it into 'table name' and 'column name'.
						String[] split = pk.split("\\.");
						// check whether the query already contains this join, if so return the tables from the query
						// Pair<PRIMARY KEY TABLE, FOREIGN KEY TABLE>
						Pair<String, String> tableNamePair = containsJoin(fromList, whereNodes, pk, attrName);
						FromTable fTable = null;
						if (tableNamePair != null) {
							// Get the FromBaseTable of the primary key from the FromList.
							// Generate a new ResultColumn with the table's correlation name and  
							// the primary key column name.
							for (int k = 0; k < fromList.size(); k++) {
								if (fromList.get(k).getOrigTableName()
										.getTableName().equals(tableNamePair.left)) {
									fTable = fromList.get(k);
									break;
								}
							}
						} else {
							// The join didn't exist, so we generate a table corresponding
							// to the primary key's table.
							fTable = generateFromBaseTable(split[0]);
							toAddToFromList.add(fTable);
						}
						
						// Generate a column using the primary key's column name and the correlation
						// name of the new table.
						ColumnReference cr = generateColumnReference(split[1], fTable.getCorrelationName());
						ResultColumn rc = generateResultColumn(cr);
						addAndRemoveMap.put(j, rc);
						
						if(tableNamePair == null) {
							// Add the join in the where clause (t1.fkey = t2.pkey)
							ValueNode topWhereNode = select.getWhereClause();
							BinaryRelationalOperatorNode joinNode = new BinaryRelationalOperatorNode();
							joinNode.setNodeType(NodeTypes.BINARY_EQUALS_OPERATOR_NODE);
							joinNode.init(cr, resultColumn.getReference());
							AndNode andNode = new AndNode();
							andNode.setNodeType(NodeTypes.AND_NODE);
							andNode.init(joinNode, topWhereNode);
							select.setWhereClause(andNode);
							whereNodes = getWhereNodes(select.getWhereClause());
						}
					}
				}
			}
			// remove the old foreign keys and add the new primary keys
			for (Integer j : addAndRemoveMap.keySet()) {
				resultColumns.remove(j);
				resultColumns.add(j, addAndRemoveMap.get(j));
			}
		}
		// now that we're done iterating over the FromList, add the new table
		for (int i = 0; i < toAddToFromList.size(); i++)
			fromList.add(toAddToFromList.get(i));

		for (int i = 0; whereNodes != null && i < whereNodes.size(); i++) {
			ValueNode node = whereNodes.get(i);
			ColumnReference columnReference = null;
			// FIXME: This is a bit short-sighted, as we only look for
			// two types of nodes. Also, for binary comparison node, I don't 
			// catch non-foreign-key joins (e.g. t1.salary = t2.ssn).
			if (node instanceof BinaryComparisonOperatorNode) {
				BinaryComparisonOperatorNode n = (BinaryComparisonOperatorNode) node;
				ValueNode lNode = n.getLeftOperand();
				ValueNode rNode = n.getRightOperand();
				
				if (lNode instanceof ConstantNode
						&& rNode instanceof ColumnReference) {
					columnReference = (ColumnReference) rNode;
				} else if (rNode instanceof ConstantNode
						&& lNode instanceof ColumnReference) {
					columnReference = (ColumnReference) lNode;
				}
			} else if(node instanceof UnaryComparisonOperatorNode) {
				columnReference = (ColumnReference)((UnaryComparisonOperatorNode)node).getOperand();
			}
			if (columnReference == null)
				continue;
			String tName = getTableNameForAlias(columnReference.getTableName(),
					fromList);
			String attrName = tName + "." + columnReference.getColumnName();

			ERAttribute erAttr = mapping.getAttribute(attrName);
			// If there is no mapped attribute, then we've found a foreign key.
			if (erAttr == null) {
				// get the corresponding primary key
				ERJoinMap join = mapping.getJoin(attrName);
				String pk = null;
				if (join instanceof ERForeignKeyJoin)
					pk = ((ERForeignKeyJoin) join).getKeyPair()
							.getPrimaryKey();
				else
					pk = ((ERLookupTableJoin) join)
							.getCorrespondingPrimaryKey(attrName);
				// split it into 'table name' and 'column name'.
				String[] split = pk.split("\\.");
				// check whether the query already contains this join, if so return the tables from the query
				// Pair<PRIMARY KEY TABLE, FOREIGN KEY TABLE>
				Pair<String, String> tableNamePair = containsJoin(fromList, whereNodes, pk, attrName);
				FromTable fTable = null;
				if (tableNamePair != null) {
					// Get the FromBaseTable of the primary key from the FromList.
					// Generate a new ColumnReference with the table's correlation name and  
					// the primary key column name.
					for (int k = 0; k < fromList.size(); k++) {
						if (fromList.get(k).getOrigTableName()
								.getTableName().equals(tableNamePair.left)) {
							fTable = fromList.get(k);
							break;
						}
					}
				} else {
					// The join didn't exist, so we generate a table corresponding
					// to the primary key's table.
					fTable = generateFromBaseTable(split[0]);
					fromList.add(fTable);
				}
				// Generate a column using the primary key's column name and the correlation
				// name of the primary key table
				ColumnReference cr = generateColumnReference(split[1],
						fTable.getCorrelationName());
				if (node instanceof BinaryComparisonOperatorNode) {
					BinaryComparisonOperatorNode n = (BinaryComparisonOperatorNode) node;
					ValueNode lNode = n.getLeftOperand();
					
					if (columnReference.equals(lNode))
						n.setLeftOperand(cr);
					else
						n.setRightOperand(cr);
				} else if(node instanceof UnaryComparisonOperatorNode) {
					((UnaryComparisonOperatorNode)node).setOperand(cr);
				}
				
				if(tableNamePair == null) {
					// Add the join in the where clause (t1.fkey = t2.pkey)
					ValueNode topWhereNode = select.getWhereClause();
					BinaryRelationalOperatorNode joinNode = new BinaryRelationalOperatorNode();
					joinNode.setNodeType(NodeTypes.BINARY_EQUALS_OPERATOR_NODE);
					joinNode.init(cr, columnReference);
					AndNode andNode = new AndNode();
					andNode.setNodeType(NodeTypes.AND_NODE);
					andNode.init(joinNode, topWhereNode);
					select.setWhereClause(andNode);
					whereNodes = getWhereNodes(select.getWhereClause());
				}
			}
		}
	}

	private Pair<String, String> containsJoin(FromList fromList, List<ValueNode> whereNodes,
			String primaryKey, String foreignKey) {
		for (ValueNode node : whereNodes) {
			if (!(node instanceof BinaryComparisonOperatorNode))
				continue;
			BinaryComparisonOperatorNode n = (BinaryComparisonOperatorNode) node;
			ValueNode lNode = n.getLeftOperand();
			ValueNode rNode = n.getRightOperand();
			if (lNode instanceof ColumnReference
					&& rNode instanceof ColumnReference) {
				ColumnReference lRef = (ColumnReference) lNode;
				String lName = getTableNameForAlias(lRef.getTableName(),
						fromList) + "." + lRef.getColumnName();
				ColumnReference rRef = (ColumnReference) rNode;
				String rName = getTableNameForAlias(rRef.getTableName(),
						fromList) + "." + rRef.getColumnName();
				if (lName.equals(primaryKey) && rName.equals(foreignKey)) {
					return new Pair<String, String>(lRef.getTableName(), rRef.getTableName());
				} else if(rName.equals(primaryKey) && lName.equals(foreignKey)) {
					return new Pair<String, String>(rRef.getTableName(), lRef.getTableName());
				}
			}
		}
		return null;
	}

	private String getTableNameForAlias(String tableAlias, FromList fList) {
		for (int i = 0; i < fList.size(); i++) {
			if (fList.get(i).getCorrelationName().equals(tableAlias)) {
				return fList.get(i).getOrigTableName().getTableName();
			}
		}
		return tableAlias;
	}

	private List<ValueNode> getWhereNodes(ValueNode node) {
		if (node == null)
			return null;
		List<ValueNode> whereNodes = new ArrayList<ValueNode>();
		whereNodes.add(node);
		if (node instanceof BinaryOperatorNode) {
			List<ValueNode> leftNodes = getWhereNodes(((BinaryOperatorNode) node)
					.getLeftOperand());
			List<ValueNode> rightNodes = getWhereNodes(((BinaryOperatorNode) node)
					.getRightOperand());
			if (leftNodes != null)
				whereNodes.addAll(leftNodes);
			if (rightNodes != null)
				whereNodes.addAll(rightNodes);
		} else if(node instanceof BinaryListOperatorNode) {
			whereNodes.addAll(getWhereNodes(((BinaryListOperatorNode)node).getLeftOperand()));
			ValueNodeList l = ((BinaryListOperatorNode) node).getRightOperandList();
			for(int i = 0; i < l.size(); i++) 
				whereNodes.addAll(getWhereNodes(l.get(i)));
		} else if (node instanceof UnaryOperatorNode) {
			whereNodes.addAll(getWhereNodes(((UnaryOperatorNode)node).getOperand()));
		}
		return whereNodes;
	}

	private FromTable generateFromBaseTable(String tableName) {
		FromTable fTable = new FromBaseTable();
		fTable.setNodeType(NodeTypes.FROM_BASE_TABLE);
		TableName tName = new TableName();
		tName.init(null, tableName);
		try {
			fTable.init(tName, null, null);
		} catch (StandardException e) {
			e.printStackTrace();
		}
		AliasApplier.generateCorrelationName(fTable);
		return fTable;
	}

	private ResultColumn generateResultColumn(ColumnReference columnReference) {
		ColumnReference cr = columnReference;
		ResultColumn rc = new ResultColumn();
		rc.setNodeType(NodeTypes.RESULT_COLUMN);
		try {
			rc.init(cr, null);
		} catch (StandardException e) {
			e.printStackTrace();
		}
		return rc;
	}

	private ColumnReference generateColumnReference(String columnName,
			String correlationName) {
		ColumnReference cr = new ColumnReference();
		cr.setNodeType(NodeTypes.COLUMN_REFERENCE);
		TableName cName = new TableName();
		cName.init(null, correlationName);
		cr.init(columnName, cName);
		return cr;
	}

	private ResultColumnList getResultColumnsForTableAlias(String tableAlias,
			ResultColumnList resultColumns) {
		ResultColumnList columns = new ResultColumnList();
		for (int i = 0; i < resultColumns.size(); i++) {
			ResultColumn resultColumn = resultColumns.get(i);
			String columnTableName = resultColumn.getTableName();
			if (tableAlias.equals(columnTableName))
				columns.add(resultColumn);
		}
		return columns;
	}
	
	private class Pair<A, B> {
	    public A left;
	    public B right;

	    public Pair(A left, B right) {
	    	super();
	    	this.left = left;
	    	this.right = right;
	    }
	}
}