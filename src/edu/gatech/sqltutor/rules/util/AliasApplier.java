package edu.gatech.sqltutor.rules.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.AllResultColumn;
import com.akiban.sql.parser.BinaryListOperatorNode;
import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.FromList;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.ResultColumnList;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.TableName;
import com.akiban.sql.parser.UnaryOperatorNode;
import com.akiban.sql.parser.ValueNode;
import com.akiban.sql.parser.ValueNodeList;

/**
 * Applies aliases to each of the tables and cascades this change to each of its column references.
 * 
 * @author William Holton
 *
 */
public class AliasApplier {
	private static final Logger log = LoggerFactory
			.getLogger(AliasApplier.class);

	public void resolve(SelectNode select) {
		FromList fromList = select.getFromList();
		ResultColumnList resultColumns = select.getResultColumns();
		List<ValueNode> whereNodes = getWhereNodes(select.getWhereClause());

		for (int i = 0; i < fromList.size(); i++) {
			FromBaseTable fromTable = (FromBaseTable)fromList.get(i);
			ResultColumnList tablesResultColumns = getResultColumnsForTableAlias(fromTable.getExposedName(), resultColumns);
			// if we actually updated the table's alias, then we need to update its column references
			if( generateCorrelationName(fromTable) ) {
				for(int j = 0; j < tablesResultColumns.size(); j++) {
					ResultColumn rc = resultColumns.get(j);
					if(rc instanceof AllResultColumn) {
						TableName tName = new TableName();
						tName.init(null, fromTable.getCorrelationName());
						try {
							rc.init(tName);
							log.info("Gave all result column the table alias={}", fromTable.getCorrelationName());
						} catch (StandardException e) {
							e.printStackTrace();
						}
					} else {		
						try {
							ColumnReference cr = generateColumnReference(getColumnName(rc), fromTable.getCorrelationName());
							rc.init(cr, null);
							log.info("Gave column={} the table alias={}", getColumnName(rc), fromTable.getCorrelationName());
						} catch (StandardException e) {
							e.printStackTrace();
						}
					}
				}
				if(whereNodes == null)
					return;
				List<ColumnReference> whereColumnReferences = getWhereColumnReferences(whereNodes);
				for(ColumnReference cr : whereColumnReferences) {
					TableName fromTableName = fromTable.getOrigTableName();
					if(cr.getTableName().equals(fromTableName.getTableName())) {
						TableName crTableName = new TableName();
						crTableName.init(null, fromTable.getCorrelationName());
						cr.setTableNameNode(crTableName);
					}
				}
			}
		}
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
	
	private String getColumnName(ResultColumn resultColumn) {
		String columnName = resultColumn.getColumnName();
		if(columnName == null) {
			if(resultColumn.getExpression() != null) 
				columnName = resultColumn.getExpression().getColumnName();
			if(columnName == null) {
				if(resultColumn.getReference() != null)
					columnName = resultColumn.getReference().getColumnName();
			}
			if(columnName == null)
				log.error("Result column does not have a column name: {}", resultColumn);
		}
		return columnName;
	}

	private ResultColumnList getResultColumnsForTableAlias(String tableAlias,
			ResultColumnList resultColumns) {
		ResultColumnList columns = new ResultColumnList();
		for (int i = 0; i < resultColumns.size(); i++) {
			ResultColumn resultColumn = resultColumns.get(i);
			String columnTableName = getTableName(resultColumn);
			if (tableAlias.equals(columnTableName)) {
				columns.add(resultColumn);
				log.info("Matched column={} to table={}", getColumnName(resultColumn), tableAlias);
			}
		}
		return columns;
	}
	
	private String getTableName(ResultColumn col) {
		// bad implementation keeps table name differently in AllResultColumn subclass
		if( col instanceof AllResultColumn ) {
			TableName tableNameObj = col.getTableNameObject();
			if( tableNameObj != null )
				return tableNameObj.getTableName();
		}
		String tableName = null;
		if(col.getReference() != null)
			tableName = col.getReference().getTableName();
		if( tableName == null && col.getTableNameObject() != null) 
			tableName = col.getTableNameObject().getTableName();
		if( tableName == null ) 
			tableName = col.getTableName();
		if (tableName == null ) 
			log.error("Result column does not have a table name: {}", col);
		return tableName;
	}

	/**
	 * Generates a random correlation name and adds it to the FromTable.
	 * @param fromTable
	 * @return	true if an alias was generated and added to the FromTable, else false.
	 */
	public static boolean generateCorrelationName(FromTable fromTable) {
		boolean generated = false;
		if (fromTable.getCorrelationName() == null) {
			// To avoid overlap, make it random
			final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			final Random rand = new Random();
			final int len = 5;
			StringBuilder sb = new StringBuilder(len);
			for (int i = 0; i < len; i++)
				sb.append(alphabet.charAt(rand.nextInt(alphabet.length())));
			try {
				String correlationName = fromTable.getTableName()
						.getTableName().substring(0, 1)
						+ sb.toString();
				fromTable.setCorrelationName(correlationName);
				log.info("Set correlation name={} for table={}", correlationName, fromTable.getOrigTableName().getTableName());
			} catch (StandardException e) {
				e.printStackTrace();
			}
			generated = true;
		}
		return generated;
	}
	
	private List<ColumnReference> getWhereColumnReferences(List<ValueNode> whereNodes) {
		if (whereNodes == null)
			return null;
		List<ColumnReference> columnReferences = new ArrayList<ColumnReference>();
		for(ValueNode n : whereNodes) {
			if(n instanceof ColumnReference) {
				columnReferences.add((ColumnReference)n);
			}
		}
		return columnReferences;
	}
}