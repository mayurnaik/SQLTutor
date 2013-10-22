package edu.gatech.sqltutor.rules;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.BinaryRelationalOperatorNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.FromList;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.JoinNode;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultSetNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.TableName;
import com.akiban.sql.parser.ValueNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;

/**
 * Meta-rule for labeling one-to-many join entities.
 * 
 * <p>
 * Given an inner join of the form:
 * </p><p>
 * <i>t<sub>1</sub></i> <tt>INNER JOIN</tt> <i>t<sub>2</sub></i> 
 * <tt>ON</tt> <i>t<sub>1</sub>.a</i> <tt>=</tt> <i>t<sub>2</sub>.b</i>
 * </p><p>
 * Where <i>t<sub>1</sub>.a</i> and <i>t<sub>2</sub>.b</i> form a 
 * one-to-one or one-to-many foreign-key relationship, there is a specified 
 * name or label for the <i>t<sub>2</sub></i> entity in the context 
 * of this join.
 * </p><p>
 * For example, in a company database, the join:
 * </p><p>
 * <code>employee AS e1 INNER JOIN employee e2 ON e1.manager_ssn=e2.ssn</code> 
 * implies that <code>e2</code> is the "manager" of <code>e1</code>.
 * </p>
 */
public class OneToAnyJoinRule implements ITranslationRule {
	private static final Logger log = LoggerFactory.getLogger(OneToAnyJoinRule.class);
	
	public static void main(String[] args) throws Exception {
		SQLParser p = new SQLParser();
		for( String arg: args ) {
			try {
				arg = QueryUtils.sanitize(arg);
				StatementNode node = p.parseStatement(arg);
				
//				System.out.println("Before types:");
//				node.treePrint();
//				node = p.parseStatement(arg);
//				node.accept(new TypeComputer());
//				System.out.println("After types:");
//				node.treePrint();
				
				new OneToAnyJoinRule("supervisor", "employee", "employee", "manager_ssn", "ssn").apply(node);
			} catch( RuntimeException e ) {
				System.err.println("Failed to parse: " + arg);
				e.printStackTrace();
			}
		}
	}
	
	private static String nodeTypeString(QueryTreeNode node) {
		return nodeTypeToString(node.getNodeType());
	}
	
	private static String nodeTypeToString(int nodeType) {
		Field[] fields = NodeTypes.class.getDeclaredFields();
		for( Field f: fields ) {
			if( 0 == (f.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) )
				continue;
			if( !f.getType().equals(int.class) )
				continue;
			
			try {
				if( nodeType == (Integer)f.get(null) )
					return "NodeTypes." + f.getName();
			} catch( Exception e ) { throw new RuntimeException(e); }
		}
		return "UNKNOWN";
	}
	
	
	protected String label;
	protected String leftTable;
	protected String rightTable;
	
	// FIXME multiple column keys?
	protected String leftAttribute;
	protected String rightAttribute;
	
	protected String leftAlias;
	protected String rightAlias;
	
	public OneToAnyJoinRule() { }	
	
	public OneToAnyJoinRule(String label, String leftTable, String rightTable, 
			String leftAttribute, String rightAttribute) {
		this.label = label;
		this.leftTable = leftTable;
		this.rightTable = rightTable;
		this.leftAttribute = leftAttribute;
		this.rightAttribute = rightAttribute;
	}

	@Override
	public int getPrecedence() {
		return 100;
	}
	
	protected void reset() {
		this.leftAlias = null;
		this.rightAlias = null;
	}
	
	private boolean isBaseTable(String tableName, ResultSetNode node) 
			throws StandardException {
		log.trace("Checking tableName={} against node={}", tableName, node);
		if( node instanceof FromBaseTable ) {
			FromBaseTable fromBaseTable = (FromBaseTable)node;
			TableName fromName = fromBaseTable.getOrigTableName();
			if( fromName == null ) {
				log.trace("No orig table name, using exposed table name.");
				fromName = fromBaseTable.getExposedTableName();
			}
			TableName fromBaseTableName = fromBaseTable.getTableName(),
					origTableName = fromBaseTable.getOrigTableName();
			log.trace("fromBaseTableName is {} (getTableName={})", 
				fromBaseTableName, fromBaseTableName.getTableName());
			log.trace("origTableName is {} (getTableName={})", 
				origTableName, origTableName.getTableName());
			
			if( tableName.equals(fromName.getTableName()) )
				return true;
		}
		return false;
	}
	
	private boolean checkInnerJoin(JoinNode join) throws StandardException {
		log.trace("Checking inner join node.");
		ResultSetNode leftResult = join.getLogicalLeftResultSet(),
				rightResult = join.getLogicalRightResultSet();
		
		if( !isBaseTable(leftTable, leftResult) || !isBaseTable(rightTable, rightResult) ) {
			log.debug("Rejected join node based on table operands.");
			return false;
		}
		
		leftAlias  = ((FromBaseTable)leftResult).getExposedName();
		rightAlias = ((FromBaseTable)rightResult).getExposedName();
		
		log.debug("leftAlias={}, rightAlias={}", leftAlias, rightAlias);
		
		ValueNode on = join.getJoinClause();
		
		// don't check if it's already been handled
		if( QueryUtils.isHandled(on) )
			return false;
		
		// TODO for now only match simple equality
		if( NodeTypes.BINARY_EQUALS_OPERATOR_NODE != on.getNodeType() ) {
			log.debug("Rejected join based on clause type: {}", on);
			return false;
		}
		
		BinaryRelationalOperatorNode binop = (BinaryRelationalOperatorNode)on;
		ValueNode leftOp = binop.getLeftOperand(), rightOp = binop.getRightOperand();
		if( NodeTypes.COLUMN_REFERENCE != leftOp.getNodeType() || 
				NodeTypes.COLUMN_REFERENCE != rightOp.getNodeType() ) {
			log.debug("Rejected join based on '=' operator types.");
			return false;
		}
		
		ColumnReference leftRef = (ColumnReference)leftOp, rightRef = (ColumnReference)rightOp;
		
		String leftRefTable = leftRef.getTableName(), rightRefTable = rightRef.getTableName();
		String leftColName = leftRef.getColumnName(), rightColName = rightRef.getColumnName();
		
		if( !checkEqualsConstraint(leftRefTable, leftColName, rightRefTable, rightColName) ) {
			log.debug("Rejected join based on columns used, saw {}.{} = {}.{}",
				leftRefTable, leftColName, rightRefTable, rightColName);
			return false;
		}
		
		// consume join condition and label right-hand join table
		RuleMetaData onMeta = QueryUtils.getOrInitMetaData(on);
		onMeta.setHandled(true);
		onMeta.addContributor(this);
		
		RuleMetaData rightMeta = QueryUtils.getOrInitMetaData(rightResult);
		rightMeta.setLabel(this.label);
		rightMeta.setOfAlias(leftAlias);
		rightMeta.addContributor(this);
		
		log.debug("Labeled right-table {} ({}) as \"{}\" of {} ({}) based on explicit INNER JOIN", 
			this.rightTable, this.rightAlias, this.label, this.leftTable, this.leftAlias);
		
		return true;
	}
	
	private boolean checkEqualsConstraint(String leftTable, String leftAttr, 
			String rightTable, String rightAttr) {
		return (leftTable.equals(this.leftAlias) && leftAttr.equals(this.leftAttribute)
				&& rightTable.equals(this.rightAlias) && rightAttr.equals(this.rightAttribute)) 
				|| // side of equality doesn't matter
				(rightTable.equals(this.leftAlias) &&  rightAttr.equals(this.leftAttribute)
				&& leftTable.equals(this.rightAlias) && leftAttr.equals(this.rightAttribute));
	}

	@Override
	public boolean apply(StatementNode statement) {
		reset();
		
		try {
			SelectNode select = QueryUtils.extractSelectNode(statement);
			
			FromList fromList = select.getFromList();
			for( FromTable fromTable: fromList ) {
				switch( fromTable.getNodeType() ) {
					case NodeTypes.JOIN_NODE:
						if( checkInnerJoin((JoinNode)fromTable) )
							return true;
						break;
				}
			}
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
		return false;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLeftTable() {
		return leftTable;
	}

	public void setLeftTable(String leftTable) {
		this.leftTable = leftTable;
	}

	public String getRightTable() {
		return rightTable;
	}

	public void setRightTable(String rightTable) {
		this.rightTable = rightTable;
	}

	public String getLeftAttribute() {
		return leftAttribute;
	}

	public void setLeftAttribute(String leftAttribute) {
		this.leftAttribute = leftAttribute;
	}

	public String getRightAttribute() {
		return rightAttribute;
	}

	public void setRightAttribute(String rightAttribute) {
		this.rightAttribute = rightAttribute;
	}
	
}
