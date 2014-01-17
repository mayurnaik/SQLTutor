package edu.gatech.sqltutor.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import objects.DatabaseTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.ValueNode;

import edu.gatech.sqltutor.IQueryTranslator;
import edu.gatech.sqltutor.rules.graph.LabelNode;
import edu.gatech.sqltutor.rules.graph.ListFormatNode;
import edu.gatech.sqltutor.rules.graph.TemplateEdge;
import edu.gatech.sqltutor.rules.graph.TranslationEdge;
import edu.gatech.sqltutor.rules.graph.TranslationGraph;

public class RuleBasedTranslator extends AbstractQueryTranslator implements IQueryTranslator {
	static final Logger log = LoggerFactory.getLogger(RuleBasedTranslator.class);
	
	public static Collection<ITranslationRule> getDefaultRules() {
		return Arrays.asList(
			new DefaultSelectRule(),
			new DefaultLabelRule()
		);
	}
	
	public static void main(String[] args) {
		// set -Dorg.slf4j.simpleLogger.logFile=<path> if you want logging to a file
		AbstractQueryTranslator translator = new RuleBasedTranslator();
		translator.addTranslationRule(new OneToAnyJoinRule(
			"employee", "employee", "manager_ssn", "ssn",
			Arrays.asList("supervisor", "manager"),
			null
		));
		translator.addTranslationRule(new ResultColumnLabelingRule(
			Arrays.asList("name", "first and last name"),
			Arrays.asList("employee.first_name", "employee.last_name")
		));
		for( String arg: args ) {
			try {
				translator.setQuery(arg);
				String result = translator.getTranslation();
				
				System.out.println("QUERY:\n" + arg + "\nRESULT:\n" + result + "\n");
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}
	
	TranslationGraph graph;
	
	public RuleBasedTranslator() { this(false); }
	
	public RuleBasedTranslator(boolean withoutDefaults) {
		if( !withoutDefaults ) {
			for( ITranslationRule rule: getDefaultRules() ) {
				this.addTranslationRule(rule);
			}
		}
	}
	
	public RuleBasedTranslator(String query) {
		setQuery(query);
	}
	
	public RuleBasedTranslator(String query, List<DatabaseTable> tables) {
		setQuery(query);
		setSchemaMetaData(tables);
	}
	
	protected void constructGraph() {
		graph = new TranslationGraph(select);
		
		// create one parent attributes node per from table
		LabelNode resultListNode = graph.getVertexForAST(select.getResultColumns());
		assert resultListNode != null : "result list node is missing";
		
		TranslationEdge edge = null;
		for( Map.Entry<FromTable, Collection<ResultColumn>> entry : 
				fromToResult.asMap().entrySet() ) {
			LabelNode tableNode = graph.getVertexForAST(entry.getKey());
			if( tableNode == null ) {
				log.warn("No table node for: {}", entry.getKey());
				throw new NullPointerException("tableNode is null");
			}
			
			// e.g. "$list of each $table"
			LabelNode attrTemplate = new LabelNode();
			// FIXME make these rule-based
			attrTemplate.addLocalChoices(Arrays.asList(
				"the ${list} of each ${table}",
				"the ${list} of every ${table}",
				"the ${list} of all ${table}s",
				"the ${list} for each ${table}",
				"the ${list} for every ${table}",
				"the ${list} for all ${table}s",
				"the ${list} of any ${table}"
			));
			graph.addVertex(attrTemplate);
			graph.addEdge(attrTemplate, tableNode, 
				new TemplateEdge(attrTemplate, tableNode, "table"));
			
			// add as child of result list node
			edge = new TranslationEdge(resultListNode, attrTemplate);
			edge.setChildEdge(true);
			graph.addEdge(resultListNode, attrTemplate, edge);
			
			// merge node for list of attributes
			LabelNode attrsNode = new ListFormatNode();
			graph.addVertex(attrsNode);
			graph.addEdge(attrTemplate, attrsNode, 
				new TemplateEdge(attrTemplate, attrsNode, "list"));
			
			for( ResultColumn col: entry.getValue() ) {
				ValueNode expr = col.getExpression();
				switch( expr.getNodeType() ) {
					case NodeTypes.COLUMN_REFERENCE: {
						LabelNode colNode = new LabelNode();
						colNode.setAstNode(expr);
						graph.addVertex(colNode);
						
						edge = new TranslationEdge(attrsNode, colNode);
						edge.setChildEdge(true);
						graph.addEdge(attrsNode, colNode, edge);
						break;
					}
					default:
						log.warn("Unhandled column (type={}): {}", expr.getClass().getSimpleName(), col);
				}
			}
		}
		System.out.println("graph: " + graph);
	}
	
	protected void computeTranslation() {
		if( query == null )
			throw new IllegalStateException("Query must be set before evaluation.");
		if( translationRules == null )
			throw new IllegalStateException("No translation rules provided.");
			
		StatementNode statement = parseQuery();
		
		buildMaps();
		constructGraph();
		
		sortRules();
		for( ITranslationRule rule: translationRules ) {
			while( rule.apply(graph, statement) ) {
				// apply each rule as many times as possible
				// FIXME non-determinism when precedences match?
			}
		}
		
		List<String> result = graph.testPullTerms();
		log.info("# of translations: {}", result.size());
		if( result.size() > 0 && log.isInfoEnabled() ) {
			log.info("1st translation: {}", result.get(0));
			log.info("last translation: {}", result.get(result.size()-1));
			
			int[] indexes = getRandomIndexes(result.size(), 10);
			for( int index: indexes )
				log.info("random translation [{}]: {}", index+1, result.get(index));
			this.result = result.get(indexes[0]);
		}
	}
	
	@Override
	public void clearResult() {
		super.clearResult();
		graph = null;
	}
	
	private static int[] getRandomIndexes(int maxIndex, int maxItems) {
		int[] result;
		if( maxIndex < maxItems ) {
			result = new int[maxIndex];
			for( int i = 0; i < maxIndex; ++i )
				result[i] = i;
			return result;
		}

		List<Integer> indexes = new ArrayList<Integer>(maxIndex);
		for( int i = 0; i < maxIndex; ++i )
			indexes.add(i);
		Collections.shuffle(indexes);
		
		result = new int[maxItems];
		for( int i = 0; i < maxItems; ++i )
			result[i] = indexes.get(i);
		return result;
	}
	
	@Override
	public Object getTranslatorType() {
		return "Parsing Rule-based";
	}
}
