package edu.gatech.sqltutor.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import objects.DatabaseTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.IQueryTranslator;
import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;

public class RuleBasedTranslator implements IQueryTranslator {
	private static final Logger log = LoggerFactory.getLogger(RuleBasedTranslator.class);
	
	private String query;
	private List<DatabaseTable> tables;
	private String result;
	private List<ITranslationRule> translationRules;
	private SelectNode select;
	
	/** Counter for applications of a rule. */
	private static class RuleCount {
		private ITranslationRule rule;
		private int count;
		public RuleCount(ITranslationRule rule) {
			this.rule = rule;
		}
		
		public void increment() { ++count; }
		public ITranslationRule getRule() { return rule; }
		public int getCount() { return count; }
		
		@Override
		public String toString() {
			return String.format("({}x) {}", count, rule);
		}
	}
	
	private static class RuleCounter {
		private List<RuleCount> counts = new ArrayList<RuleCount>();
		public RuleCounter() { }
		public void ruleApplied(ITranslationRule rule) {
			RuleCount count = null;
			if( counts.size() > 0 )
				count = counts.get(counts.size()-1);
			if( count == null || !count.getRule().equals(rule) ) {
				count = new RuleCount(rule);
				counts.add(count);
			}
			count.increment();
		}
		
		@Override
		public String toString() {
			return "RuleCounter{" + counts + "}";
		}
	}
	
	public RuleBasedTranslator() { }
	
	public RuleBasedTranslator(String query) {
		setQuery(query);
	}
	
	public RuleBasedTranslator(String query, List<DatabaseTable> tables) {
		setQuery(query);
		setSchemaMetaData(tables);
	}
	
	protected void computeTranslation() {
		if( query == null )
			throw new IllegalStateException("Query must be set before evaluation.");
		if( translationRules == null )
			throw new IllegalStateException("No translation rules provided.");
		
		SQLParser parser = new SQLParser();
		try {
			StatementNode statement = parser.parseStatement(query);
			try {
				select = QueryUtils.extractSelectNode(statement);
			} catch( IllegalArgumentException e ) {
				throw new SQLTutorException("Wrong query type for: " + query, e);
			}
			
			
			RuleCounter counter = new RuleCounter();
			
			sortRules();
			for( ITranslationRule rule: translationRules ) {
				while( rule.apply(statement) ) {
					// apply each rule as many times as possible
					counter.ruleApplied(rule);
					// FIXME non-determinism when precedences match?
				}
			}
			
			log.info("{}", counter);
			
			// TODO now lower into natural language
			
		} catch( StandardException e ) {
			throw new SQLTutorException("Could not parse query: " + query, e);
		}
	}
	
	/** Sort rules by decreasing precedence. */
	protected void sortRules() {
		Collections.sort(translationRules, new Comparator<ITranslationRule>() {
			@Override
			public int compare(ITranslationRule o1, ITranslationRule o2) {
				int p1 = o1.getPrecedence(), p2 = o2.getPrecedence();
				return (p1 < p2 ? 1 : (p1 == p2 ? 0 : -1));
			}
		});
	}
	
	public void setTranslationRules(List<ITranslationRule> translationRules) {
		this.translationRules = translationRules;
	}
	
	public List<ITranslationRule> getTranslationRules() {
		return translationRules;
	}
	
	public void addTranslationRule(ITranslationRule rule) {
		if( translationRules == null )
			translationRules = new ArrayList<ITranslationRule>();
		translationRules.add(rule);
	}

	@Override
	public void setQuery(String sql) {
		this.query = (sql == null ? null : QueryUtils.sanitize(sql));
	}

	@Override
	public String getQuery() {
		return this.query;
	}

	@Override
	public void setSchemaMetaData(List<DatabaseTable> tables) {
		this.tables = tables;
	}

	@Override
	public List<DatabaseTable> getSchemaMetaData() {
		return tables;
	}

	@Override
	public Object getTranslatorType() {
		return "Parsing Rule-based";
	}

	@Override
	public String getTranslation() throws SQLTutorException {
		if( result == null )
			computeTranslation();
		return result;
	}
}
