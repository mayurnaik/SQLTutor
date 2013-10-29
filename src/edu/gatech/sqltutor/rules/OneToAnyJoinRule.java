package edu.gatech.sqltutor.rules;

import java.util.ArrayList;
import java.util.List;
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
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.TableName;
import com.akiban.sql.parser.ValueNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.graph.LabelNode;
import edu.gatech.sqltutor.rules.graph.TranslationGraph;

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
				log.info("Query: {}", arg);
				
//				System.out.println("Before types:");
//				node.treePrint();
//				node = p.parseStatement(arg);
//				node.accept(new TypeComputer());
//				System.out.println("After types:");
//				node.treePrint();
				TranslationGraph graph = new TranslationGraph(QueryUtils.extractSelectNode(node));
				new OneToAnyJoinRule("supervisor", "employee", "employee", "manager_ssn", "ssn").apply(graph, node);
			} catch( RuntimeException e ) {
				System.err.println("Failed to parse: " + arg);
				e.printStackTrace();
			}
		}
	}
	
	protected List<String> oneLabels = new ArrayList<String>(0);
	protected List<String> anyLabels = new ArrayList<String>(0);
	
	protected String label;
	protected String oneTable;
	protected String anyTable;
	
	// FIXME multiple column keys?
	protected String oneAttribute;
	protected String anyAttribute;
	
	protected String oneAlias;
	protected String anyAlias;
	protected SelectNode select;
	protected TranslationGraph graph;
	
	public OneToAnyJoinRule() { }	
	
	public OneToAnyJoinRule(String label, String oneTable, String anyTable, 
			String oneAttribute, String anyAttribute) {
		this.label = label;
		this.oneTable = oneTable;
		this.anyTable = anyTable;
		this.oneAttribute = oneAttribute;
		this.anyAttribute = anyAttribute;
	}

	@Override
	public int getPrecedence() {
		return 100;
	}
	
	protected void reset() {
		this.select = null;
		this.graph = null;
		this.oneAlias = null;
		this.anyAlias = null;
	}
	
	private boolean isBaseTable(String tableName, ResultSetNode node) 
			throws StandardException {
		if( node instanceof FromBaseTable ) {
			FromBaseTable fromBaseTable = (FromBaseTable)node;
			TableName fromName = fromBaseTable.getOrigTableName();
			if( fromName == null ) {
				log.trace("No orig table name, using exposed table name.");
				fromName = fromBaseTable.getExposedTableName();
			}
			if( tableName.equals(fromName.getTableName()) )
				return true;
		}
		return false;
	}
	
	private boolean checkInnerJoin(JoinNode join) throws StandardException {
		log.trace("Checking inner join node.");
		ResultSetNode leftResult = join.getLogicalLeftResultSet(),
				rightResult = join.getLogicalRightResultSet();
		
		if( !isBaseTable(oneTable, leftResult) || !isBaseTable(anyTable, rightResult) ) {
			log.debug("Rejected join node based on table operands.");
			return false;
		}
		
		ValueNode on = join.getJoinClause();
		
		// don't check if it's already been handled
		if( QueryUtils.isHandled(on) )
			return false;
		
		if( checkInnerJoin(leftResult, rightResult, on) )
			return true;
		if( checkInnerJoin(rightResult, leftResult, on) )
			return true;
		
		return false;
	}
		
	private boolean checkInnerJoin(ResultSetNode leftResult, ResultSetNode rightResult, ValueNode on) {
		oneAlias  = ((FromBaseTable)leftResult).getExposedName();
		anyAlias = ((FromBaseTable)rightResult).getExposedName();
		
		if( QueryUtils.hasContributed(this, rightResult) ) {
			return false;
		}
		
		log.debug("Trying oneAlias={}, anyAlias={}", oneAlias, anyAlias);
		
		// TODO for now only match simple equality
		if( NodeTypes.BINARY_EQUALS_OPERATOR_NODE != on.getNodeType() ) {
			log.debug("Rejected join based on clause type: {}", on);
			return false;
		}
		
		BinaryRelationalOperatorNode binop = (BinaryRelationalOperatorNode)on;
		if( !checkBinaryEquality(binop) ) {
			log.debug("Rejected join based on columns used");
			return false;
		}
		
		RuleMetaData rightMeta = QueryUtils.getOrInitMetaData(rightResult);
		LabelNode rightTable = graph.getTableVertex(anyAlias);
		rightTable.getLocalChoices().add(this.label);
		rightTable.modified();
//		rightMeta.setLabel(this.label);
//		rightMeta.setOfAlias(oneAlias);
		rightMeta.addContributor(this);
		
		log.debug("Labeled based on explicit INNER JOIN");
		
		return true;
	}
	
	private boolean checkEqualsConstraint(String leftTable, String leftAttr, 
			String rightTable, String rightAttr) {
		return (leftTable.equals(this.oneAlias) && leftAttr.equals(this.oneAttribute)
				&& rightTable.equals(this.anyAlias) && rightAttr.equals(this.anyAttribute)) 
				|| // side of equality doesn't matter
				(rightTable.equals(this.oneAlias) &&  rightAttr.equals(this.oneAttribute)
				&& leftTable.equals(this.anyAlias) && leftAttr.equals(this.anyAttribute));
	}
	
	private boolean checkImplicitJoin(FromBaseTable leftRef, FromBaseTable rightRef) 
			throws StandardException {
		if( QueryUtils.hasContributed(this, rightRef) )
			return false;
		
		oneAlias = leftRef.getExposedName();
		anyAlias = rightRef.getExposedName();
		
		ValueNode where = select.getWhereClause();
		
		if( where != null ) {
			Stack<ValueNode> nodesToCheck = new Stack<ValueNode>();
			nodesToCheck.push(where);
			
			while( !nodesToCheck.isEmpty() ) {
				ValueNode toCheck = nodesToCheck.pop();
				switch(toCheck.getNodeType()) {
					case NodeTypes.BINARY_EQUALS_OPERATOR_NODE:
						if( checkBinaryEquality((BinaryRelationalOperatorNode)toCheck)) {
							RuleMetaData rightMeta = QueryUtils.getOrInitMetaData(rightRef);
							rightMeta.setLabel(this.label);
							rightMeta.setOfAlias(oneAlias);
							LabelNode rightTable = graph.getTableVertex(anyAlias);
							rightTable.getLocalChoices().add(this.label);
							rightTable.modified();
							rightMeta.addContributor(this);
							
							log.debug("Label based on implicit join and WHERE clause");
							return true;
						}
						break;
					case NodeTypes.AND_NODE: {
						AndNode and = (AndNode)where;
						nodesToCheck.push(and.getLeftOperand());
						nodesToCheck.push(and.getRightOperand());
						break;
					}
				}
			}
		}
		
		log.debug("Rejecting due to missing key condition in WHERE clause.");
		
		return false;
	}
	
	private boolean checkBinaryEquality(BinaryRelationalOperatorNode eq) {
		ValueNode leftOp = eq.getLeftOperand(), rightOp = eq.getRightOperand();
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
		RuleMetaData onMeta = QueryUtils.getOrInitMetaData(eq);
		onMeta.setHandled(true);
		onMeta.addContributor(this);
		
		log.debug("Labeled right-table {} ({}) as \"{}\" of {} ({})", 
			this.anyTable, this.anyAlias, this.label, this.oneTable, this.oneAlias);
		
		return true;
	}

	@Override
	public boolean apply(TranslationGraph graph, StatementNode statement) {
		reset();
		
		try {
			select = QueryUtils.extractSelectNode(statement);
			this.graph = graph;
			
			List<FromBaseTable> potentialLefts = new ArrayList<FromBaseTable>(1);
			List<FromBaseTable> potentialRights = new ArrayList<FromBaseTable>(1);
			FromList fromList = select.getFromList();
			for( FromTable fromTable: fromList ) {
				switch( fromTable.getNodeType() ) {
					case NodeTypes.JOIN_NODE:
						if( checkInnerJoin((JoinNode)fromTable) )
							return true;
						break;
					case NodeTypes.FROM_BASE_TABLE:
						if( isBaseTable(oneTable, fromTable) )
							potentialLefts.add((FromBaseTable)fromTable);
						if( isBaseTable(anyTable, fromTable) )
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
							return true;
					}
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
		return oneTable;
	}

	public void setLeftTable(String leftTable) {
		this.oneTable = leftTable;
	}

	public String getRightTable() {
		return anyTable;
	}

	public void setRightTable(String rightTable) {
		this.anyTable = rightTable;
	}

	public String getLeftAttribute() {
		return oneAttribute;
	}

	public void setLeftAttribute(String leftAttribute) {
		this.oneAttribute = leftAttribute;
	}

	public String getRightAttribute() {
		return anyAttribute;
	}

	public void setRightAttribute(String rightAttribute) {
		this.anyAttribute = rightAttribute;
	}
	
}
