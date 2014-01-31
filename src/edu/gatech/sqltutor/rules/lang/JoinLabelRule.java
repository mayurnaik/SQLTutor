package edu.gatech.sqltutor.rules.lang;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.BinaryRelationalOperatorNode;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.ValueNode;
import com.akiban.sql.unparser.NodeToString;
import com.google.common.collect.Lists;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.EREdgeConstraint;
import edu.gatech.sqltutor.rules.er.ERRelationship;
import edu.gatech.sqltutor.rules.er.mapping.ERForeignKeyJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap.ERKeyPair;
import edu.gatech.sqltutor.rules.er.mapping.ERLookupTableJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.rules.er.mapping.ERMergedJoin;
import edu.gatech.sqltutor.rules.graph.TranslationGraph;
import edu.gatech.sqltutor.rules.util.JoinDetector;
import edu.gatech.sqltutor.rules.util.JoinDetector.JoinResult;
import edu.gatech.sqltutor.util.Pair;

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
public class JoinLabelRule implements ITranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(JoinLabelRule.class);
	
	private ERDiagram erDiagram;
	private ERMapping erMapping;
	private SelectNode select;
	
//	private BiMap<ERForeignKeyJoin, JoinDetector> fkDetectors;
	private List<Pair<ERForeignKeyJoin, JoinDetector>> fkDetectors;
	private List<Pair<JoinDetector, JoinDetector>> lookupDetectors;
	
	public JoinLabelRule(ERDiagram erDiagram, ERMapping erMapping) {
		if( erDiagram == null ) throw new NullPointerException("erDiagram is null");
		if( erMapping == null ) throw new NullPointerException("erMapping is null");
		this.erDiagram = erDiagram;
		this.erMapping = erMapping;
		erMapping.setDiagram(erDiagram);
		
		findRelationships();
	}

	@Override
	public int getPrecedence() {
		return DefaultPrecedence.DESTRUCTIVE_UPDATE;
	}

	@Override
	public boolean apply(TranslationGraph graph, StatementNode statement) {
		select = QueryUtils.extractSelectNode(statement);
		
		try {
			if( detectFKJoin() )
				return true;
			
			if( detectLookupJoins() )
				return true;
			
			return false;
		} finally {
			select = null;
		}
	}
	
	private void findRelationships() {
		fkDetectors = Lists.newLinkedList();
//		fkDetectors = HashBiMap.create();
		lookupDetectors = Lists.newLinkedList();
		
		Set<ERJoinMap> joins = erMapping.getJoins();
		for( ERJoinMap join: joins ) {
			switch( join.getMapType() ) {
				case FOREIGN_KEY:
					visitFKJoin((ERForeignKeyJoin)join);
					break;
				case LOOKUP_TABLE:
					visitLookupTableJoin((ERLookupTableJoin)join);
					break;
				case MERGED:
					visitMergedJoin((ERMergedJoin)join);
					break;
			}
		}
	}
	
	private void visitMergedJoin(ERMergedJoin join) {
		throw new SQLTutorException("FIXME: Merged type not implemented.");
	}

	private void visitLookupTableJoin(ERLookupTableJoin join) {
		ERKeyPair left = join.getLeftKeyPair(), right = join.getRightKeyPair();
		String pk1 = left.getPrimaryKey(), fk1 = left.getForeignKey(),
				pk2 = right.getPrimaryKey(), fk2 = right.getForeignKey();
		
		JoinDetector det1 = new JoinDetector(pk1, fk1), det2 = new JoinDetector(pk2, fk2);
		lookupDetectors.add(Pair.make(det1, det2));
		_log.trace("Added lookup detector (pk={}, fk={}) and (pk={}, fk={})", pk1, fk1, pk2, fk2);
	}

	private void visitFKJoin(ERForeignKeyJoin join) {
		String pk = join.getKeyPair().getPrimaryKey();
		String fk = join.getKeyPair().getForeignKey();
		fkDetectors.add(Pair.make(join, new JoinDetector(pk, fk)));
		_log.trace("Added FK detector (pk={}, fk={})", pk, fk);
	}

	private boolean detectFKJoin() {
		// first look for two-table joins
		ListIterator<Pair<ERForeignKeyJoin,JoinDetector>> iter = fkDetectors.listIterator();
		while( iter.hasNext() ) {
			Pair<ERForeignKeyJoin, JoinDetector> next = iter.next();
			JoinDetector detector = next.getSecond();//iter.next();
			JoinResult result = detector.detect(select);
			if( result != null ) {
				detector.skipClause(result.getJoinCondition()); // we're done with this detection
				processFKJoin(next.getFirst(), result);
				_log.warn("TODO: Process match result: " + result);
				return true;
			} else {
				iter.remove();
			}
		}
		
		return false;
	}
	
	private void processFKJoin(ERForeignKeyJoin join, JoinResult result) {
		FromBaseTable leftTable = result.getFirstTable(), rightTable = result.getSecondTable();

		ERRelationship relationship = erMapping.getRelationship(join);
		_log.info("Matched on relationship: {}", relationship.getFullName());
		EREdgeConstraint leftConstraint = relationship.getLeftEdge().getConstraint();
		EREdgeConstraint rightConstraint = relationship.getRightEdge().getConstraint();
		
		_log.info("\nApply {} to table {}\nApply {} to table {}", 
			leftConstraint.getLabel(), leftTable, rightConstraint.getLabel(), rightTable);
		
		NodeToString n2s = new NodeToString();
		try {
			_log.info("Current query state:\n{}", n2s.toString(select));
		} catch( StandardException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BinaryRelationalOperatorNode binop = result.getJoinCondition();
		deleteCondition(binop);
		
		try {
			_log.info("New query state:\n{}", n2s.toString(select));
		} catch( StandardException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void deleteCondition(BinaryRelationalOperatorNode binop) {
		QueryTreeNode parent = QueryUtils.findParent(select, binop);
		_log.info("Found parent: {}", parent);
		
		if( parent instanceof BinaryOperatorNode ) {
			BinaryOperatorNode parentOp = (BinaryOperatorNode)parent;
			if( binop == parentOp.getLeftOperand() ) {
				replaceParent(parentOp, parentOp.getRightOperand());
			} else {
				replaceParent(parentOp, parentOp.getLeftOperand());
			}
		} else if( parent instanceof SelectNode ) {
			_log.debug("Deleting WHERE clause.");
			((SelectNode)parent).setWhereClause(null);
		} else {
			throw new SQLTutorException("FIXME: Unhandled parent type: " + parent.getClass().getName());
		}
	}

	private void replaceParent(BinaryOperatorNode parentOp, ValueNode withOperand) {
		parentOp.setLeftOperand(null);
		parentOp.setRightOperand(null);
		
		QueryTreeNode grandparent = QueryUtils.findParent(select, parentOp);
		if( grandparent instanceof BinaryOperatorNode ) {
			BinaryOperatorNode binop = (BinaryOperatorNode)grandparent;
			if( binop.getLeftOperand() == parentOp )
				binop.setLeftOperand(withOperand);
			else
				binop.setRightOperand(withOperand);
		} else if( grandparent instanceof SelectNode ) {
			_log.debug("Deleting WHERE clause.");
			((SelectNode)grandparent).setWhereClause(null);
		} else {
			throw new SQLTutorException("FIXME: Unhandled parent type: " + grandparent.getClass().getName());
		}
	}

	private boolean detectLookupJoins() {
		// next look for three-table (lookup table) joins
		for( ListIterator<Pair<JoinDetector, JoinDetector>> iter = lookupDetectors.listIterator(); iter.hasNext(); ) {
			Pair<JoinDetector, JoinDetector> next = iter.next();
			JoinDetector detector1 = next.getFirst(), detector2 = next.getSecond();
			
			JoinResult result1 = detector1.detect(select);
			if( result1 != null ) {
				JoinResult result2 = detector2.detect(select);
				if( result2 != null ) {
					detector1.skipClause(result1.getJoinCondition());
					detector2.skipClause(result2.getJoinCondition());
					_log.warn("TODO: Process match results: {} and {}", result1, result2);
					return true;
				}
			}
			
			iter.remove();
		}
		
		return false;
	}
}
