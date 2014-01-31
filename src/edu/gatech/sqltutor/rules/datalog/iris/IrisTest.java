package edu.gatech.sqltutor.rules.datalog.iris;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.asTuple;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.newLiteral;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;
import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.Utils;
import edu.gatech.sqltutor.rules.util.GetChildrenVisitor;
import edu.gatech.sqltutor.rules.util.ParserVisitorAdapter;

public class IrisTest {
	static final Logger _log = LoggerFactory.getLogger(IrisTest.class);
	
	private static final String STATIC_RULES;
	static {
		InputStream in = IrisTest.class.getResourceAsStream("/iristest.dlog");
		try {
			STATIC_RULES = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
		} catch( IOException e ) {
			throw new RuntimeException(e);
		} finally {
			Utils.tryClose(in);
		}
	}
	
	public static void main(String[] args) {
		SQLParser p = new SQLParser();
		for( String arg: args ) {
			try {
				_log.info("SQL: {}", arg);
				StatementNode st = p.parseStatement(arg);
				IrisTest test = new IrisTest(st);
				test.evaluate();
			} catch( StandardException e ) {
				e.printStackTrace();
			}
		}
	}
	
	private static final IRelationFactory relationFactory = new SimpleRelationFactory();
	
	private SelectNode select;
	private BiMap<Integer, QueryTreeNode> nodeIds = HashBiMap.create();
	
	private Map<IPredicate, IRelation> facts = Maps.newHashMap();
	private List<IRule> rules = Lists.newArrayList();

	public IrisTest(StatementNode node) {
		this(QueryUtils.extractSelectNode(node));
	}
	
	public IrisTest(SelectNode select) {
		this.select = select;
		init();
	}
	
	public void evaluate() {
		long duration = -System.currentTimeMillis();
		Configuration irisConfig = KnowledgeBaseFactory.getDefaultConfiguration();
		try {
			IKnowledgeBase kb = KnowledgeBaseFactory.createKnowledgeBase(facts, rules);
			_log.info("Knowledge based created in {} ms.", duration + System.currentTimeMillis());
			
			IPredicate joinRuleFK = Factory.BASIC.createPredicate("joinRuleFK", 5);
			ILiteral joinRuleLit = Factory.BASIC.createLiteral(true, joinRuleFK, asTuple(
				"?t1", "ssn", "?t2", "manager_ssn", "?eq"
			));
			ILiteral t1Name = newLiteral(SQLPredicates.tableName, "?t1", "employee");
			ILiteral t2Name = newLiteral(SQLPredicates.tableName, "?t2", "employee");
			IQuery query = Factory.BASIC.createQuery(joinRuleLit, t1Name, t2Name);
			
			_log.info("Evaluating: " + query);
			List<IVariable> vars = new ArrayList<IVariable>();
			duration = -System.currentTimeMillis();
			IRelation result = kb.execute(query, vars);
			_log.info("Evaluation took {} ms.", duration + System.currentTimeMillis());
			_log.info("Result: " + result);
			_log.info("Bindings: " + vars);
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
	}
	
	private void init() {
		long duration = -System.currentTimeMillis();
		try {
			// assign ids to all nodes, from the top down
			select.accept(new ParserVisitorAdapter() {
				int nextId = 0;
				@Override
				public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
					nodeIds.put(nextId++, node);
					return node;
				}
			});
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
		_log.info("ID assignment in {} ms.", duration + System.currentTimeMillis());
		
		duration = -System.currentTimeMillis();
		addStaticRules();
		_log.info("Static rules added in {} ms.", duration + System.currentTimeMillis());
		
		duration = -System.currentTimeMillis();
		addFacts(select);
		_log.info("Facts generated in {} ms.", duration + System.currentTimeMillis());
	}
	
	private void addStaticRules() {
		Parser p = new Parser();
		try {
			p.parse(STATIC_RULES);
		} catch( ParserException e ) {
			throw new SQLTutorException("Could not parse static rules.", e);
		}
		
		facts.putAll(p.getFacts());
		rules.addAll(p.getRules());
	}
	
	private void addFacts(QueryTreeNode node) {
		try {
			node.accept(new ParserVisitorAdapter() {
				GetChildrenVisitor childVisitor = new GetChildrenVisitor();
				
				List<QueryTreeNode> getChildren(QueryTreeNode node) throws StandardException {
					childVisitor.reset();
					node.accept(childVisitor);
					return childVisitor.getChildren();
				}
				
				@Override
				public QueryTreeNode visit(QueryTreeNode node) throws StandardException {
					Integer nodeId = nodeIds.inverse().get(node);
					addLocalFacts(node);
					for( QueryTreeNode child: getChildren(node) ) {
						Integer childId = nodeIds.inverse().get(child);
						addFact(SQLPredicates.parentOf, nodeId, childId);
					}
					return node;
				}
			});
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
	}
	
	private void addLocalFacts(QueryTreeNode node) {
		Integer nodeId = nodeIds.inverse().get(node);
		String nodeType = node.getClass().getName().replaceAll("^.*\\.", "");
		addFact(SQLPredicates.nodeHasType, nodeId, nodeType);
		
		if( node instanceof ColumnReference )
			addColumnReferenceFacts(nodeId, (ColumnReference)node);
		if( node instanceof BinaryOperatorNode )
			addBinopFacts(nodeId, (BinaryOperatorNode)node);
		if( node instanceof FromBaseTable )
			addTableFacts(nodeId, (FromBaseTable)node);
	}
	
	private void addBinopFacts(Integer nodeId, BinaryOperatorNode binop) {
		String op = binop.getOperator();
		addFact(SQLPredicates.operator, nodeId, op);
	}
	
	private void addTableFacts(Integer nodeId, FromBaseTable table) {
		addFact(SQLPredicates.tableName, nodeId, table.getOrigTableName().getTableName());
		addFact(SQLPredicates.tableAlias, nodeId, table.getExposedName());
	}
	
	private void addColumnReferenceFacts(Integer nodeId, ColumnReference col) {
		addFact(SQLPredicates.tableAlias, nodeId, col.getTableName());
		addFact(SQLPredicates.columnName, nodeId, col.getColumnName());
	}
	
	private void addFact(IPredicate pred, Object... vals) {
		assert pred != null : "pred is null";
		ITuple tuple = IrisUtil.asTuple(vals); 
		IRelation rel = facts.get(pred);
		if( rel == null )
			facts.put(pred, rel = relationFactory.createRelation());
		rel.add(tuple);
		_log.info("Added fact: {}{}", pred.getPredicateSymbol(), tuple);
	}
}
