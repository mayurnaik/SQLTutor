package edu.gatech.sqltutor.rules.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.AndNode;
import com.akiban.sql.parser.BinaryComparisonOperatorNode;
import com.akiban.sql.parser.BinaryLogicalOperatorNode;
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
import com.akiban.sql.parser.ValueNode;

import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.mapping.ERForeignKeyJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap;
import edu.gatech.sqltutor.rules.er.mapping.ERLookupTableJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;

public class ForeignKeyReplacer {
	public static final boolean ACTIVE = false;
	private static final Logger log = LoggerFactory.getLogger(ForeignKeyReplacer.class);
	private ERMapping mapping;
	
	public ForeignKeyReplacer(ERMapping mapping) {
		this.mapping = mapping;
	}
	
	public void resolve(SelectNode select) {
		if(!ACTIVE)
			return;
		FromList fromList = select.getFromList();
		ResultColumnList resultColumns = select.getResultColumns();
		List<ValueNode> whereNodes = getWhereNodes(select.getWhereClause());
		
		FromList toAddToFromList = new FromList();
		for(int i = 0; i < fromList.size(); i++) {
			FromBaseTable fromTable = (FromBaseTable)fromList.get(i);
			
			String tableAlias = fromTable.getExposedName();
			ResultColumnList tablesResultColumns = getResultColumnsForTableAlias(tableAlias, resultColumns);
			if( tablesResultColumns.size() == 0 && (whereNodes == null || whereNodes.size() == 0))
				continue;
			
			generateCorrelationName(fromTable);

			Map<Integer, ResultColumn> addAndRemoveMap = new HashMap<Integer, ResultColumn>();
			for(int j = 0; j < tablesResultColumns.size(); j++) {
				ResultColumn resultColumn = tablesResultColumns.get(j);
				if(!(resultColumn.getNodeType() == NodeTypes.ALL_RESULT_COLUMN)) {
					String attrName =
							fromTable.getOrigTableName().getTableName() + 
							"." + resultColumn.getExpression().getColumnName();
					ERAttribute erAttr = mapping.getAttribute(attrName);
					if( erAttr == null ) {
						ERJoinMap join = mapping.getJoin(attrName);
						String pk = null;
						if(join instanceof ERForeignKeyJoin) 
							pk = ((ERForeignKeyJoin) join).getKeyPair().getPrimaryKey();
						else
							pk = ((ERLookupTableJoin) join).getCorrespondingPrimaryKey(attrName);
						String[] split = pk.split("\\.");
						FromTable fTable = null;
						for(int k = 0; k < fromList.size(); k++) {
							if(fromList.get(k).getOrigTableName().getTableName().equals(split[0])) 
								fTable = fromList.get(k);
						}
						if(fTable == null) {
							fTable = generateFromBaseTable(split[0]);
							toAddToFromList.add(fTable);
						}
						
						ResultColumn rc = generateResultColumn(generateColumnReference(split[1], fTable.getCorrelationName()));
						addAndRemoveMap.put(j, rc);
						
						if(!containsJoin(fromList, whereNodes, pk, attrName)) {
							ValueNode topWhereNode = select.getWhereClause();
							BinaryRelationalOperatorNode joinNode = new BinaryRelationalOperatorNode();
							joinNode.setNodeType(NodeTypes.BINARY_EQUALS_OPERATOR_NODE);
							ColumnReference primary = generateColumnReference(resultColumn.getExpression().getColumnName(), fromTable.getCorrelationName());
							joinNode.init(rc.getReference(), primary);
							AndNode andNode = new AndNode();
							andNode.setNodeType(NodeTypes.AND_NODE);
							andNode.init(joinNode, topWhereNode);
							select.setWhereClause(andNode);
							whereNodes = getWhereNodes(select.getWhereClause());
						}
					}
				}
			}
			
			for(Integer j : addAndRemoveMap.keySet())  {
				resultColumns.remove(j);
				resultColumns.add(j, addAndRemoveMap.get(j));
			}	
		}
		
		for(int i = 0; i < toAddToFromList.size(); i++) 
			fromList.add(toAddToFromList.get(i));

		for(int i = 0; whereNodes != null && i < whereNodes.size(); i++) {
			ValueNode node = whereNodes.get(i);
			if(!(node instanceof BinaryComparisonOperatorNode))
				continue;
			BinaryComparisonOperatorNode n = (BinaryComparisonOperatorNode)node;
			ValueNode lNode = n.getLeftOperand();
			ValueNode rNode = n.getRightOperand();
			ColumnReference columnReference = null;
			if(lNode instanceof ConstantNode && rNode instanceof ColumnReference) {
				columnReference = (ColumnReference)rNode;
			} else if(rNode instanceof ConstantNode && lNode instanceof ColumnReference) {
				columnReference = (ColumnReference)lNode;
			}
			if(columnReference == null)
				continue;
			String tName = getTableNameForAlias(columnReference.getTableName(), fromList);
			String attrName = tName + "." + columnReference.getColumnName();
			
			ERAttribute erAttr = mapping.getAttribute(attrName);
			if( erAttr == null ) {
				ERJoinMap join = mapping.getJoin(attrName);

				String pk = null;
				if(join instanceof ERForeignKeyJoin) 
					pk = ((ERForeignKeyJoin) join).getKeyPair().getPrimaryKey();
				else
					pk = ((ERLookupTableJoin) join).getCorrespondingPrimaryKey(attrName);
				String[] split = pk.split("\\.");
				FromTable fTable = null;
				for(int k = 0; k < fromList.size(); k++) {
					if(fromList.get(k).getOrigTableName().getTableName().equals(split[0])) 
						fTable = fromList.get(k);
				}
				if(fTable == null) {
					fTable = generateFromBaseTable(split[0]);
					fromList.add(fTable);
				}

				ColumnReference cr = generateColumnReference(split[1], fTable.getCorrelationName());
				if(columnReference.equals(lNode))
					n.setLeftOperand(cr);
				else
					n.setRightOperand(cr);
				
				if(!containsJoin(fromList, whereNodes, pk, attrName)) {
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
	
	private boolean containsJoin(FromList fromList, List<ValueNode> whereNodes, String primaryKey, String foreignKey) {
		boolean containsJoin = false;
		for(ValueNode node : whereNodes) {
			if(!(node instanceof BinaryComparisonOperatorNode))
				continue;
			BinaryComparisonOperatorNode n = (BinaryComparisonOperatorNode)node;
			ValueNode lNode = n.getLeftOperand();
			ValueNode rNode = n.getRightOperand();
			if(lNode instanceof ColumnReference && rNode instanceof ColumnReference) {
				ColumnReference lRef = (ColumnReference)lNode;
				String lName = getTableNameForAlias(lRef.getTableName(), fromList) + "." + lRef.getColumnName();
				ColumnReference rRef = (ColumnReference)rNode;
				String rName = getTableNameForAlias(rRef.getTableName(), fromList) + "." + rRef.getColumnName();
				if(lName.equals(primaryKey) && rName.equals(foreignKey) || rName.equals(primaryKey) && lName.equals(foreignKey)) {
					return true;
				}
			}
		}
		return containsJoin;
	}
	
	private String getTableNameForAlias(String tableAlias, FromList fList) {
		for(int i = 0; i < fList.size(); i++) {

			if(fList.get(i).getCorrelationName().equals(tableAlias)) {
				return fList.get(i).getOrigTableName().getTableName();
			}
		}
		return tableAlias;
	}
	
	private List<ValueNode> getWhereNodes(ValueNode node) {
		if(node == null)
			return null;
		List<ValueNode> whereNodes = new ArrayList<ValueNode>();
		if(node instanceof BinaryLogicalOperatorNode) {
			List<ValueNode> leftNodes = getWhereNodes(((BinaryLogicalOperatorNode) node).getLeftOperand());
			List<ValueNode> rightNodes = getWhereNodes(((BinaryLogicalOperatorNode) node).getRightOperand());
			if(leftNodes != null)
				whereNodes.addAll(leftNodes);
			if(rightNodes != null)
				whereNodes.addAll(rightNodes);
		} else {
			whereNodes.add(node);
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
		generateCorrelationName(fTable);
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
	
	private ColumnReference generateColumnReference(String columnName, String correlationName) {
		ColumnReference cr = new ColumnReference();		
		cr.setNodeType(NodeTypes.COLUMN_REFERENCE);
		TableName cName = new TableName();
		cName.init(null, correlationName);
		cr.init(columnName, cName);
		return cr;
	}
	
	private ResultColumnList getResultColumnsForTableAlias(String tableAlias, ResultColumnList resultColumns) {
		ResultColumnList columns = new ResultColumnList();
		for(int i = 0; i < resultColumns.size(); i++) {
			ResultColumn resultColumn = resultColumns.get(i);
			String columnTableName = resultColumn.getTableName();
			if( tableAlias.equals(columnTableName) )
				columns.add(resultColumn);
		}
		return columns;
	}

	private void generateCorrelationName(FromTable fromTable) {
		if(fromTable.getCorrelationName() == null) {
			// To avoid overlap, make it random
			final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			final Random rand = new Random();
			final int len = 5;
			StringBuilder sb = new StringBuilder( len );
			for( int i = 0; i < len; i++ ) 
				sb.append( alphabet.charAt( rand.nextInt(alphabet.length()) ) );
			try {
				fromTable.setCorrelationName(fromTable.getTableName().getTableName().substring(0,1) + sb.toString());
			} catch (StandardException e) {
				e.printStackTrace();
			}
		}
	}
}