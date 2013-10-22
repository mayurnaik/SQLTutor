package edu.gatech.sqltutor.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;

import objects.DatabaseTable;
import edu.gatech.sqltutor.IQueryTranslator;
import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;

public class RuleBasedTranslator implements IQueryTranslator {
	private String query;
	private List<DatabaseTable> tables;
	private String result;
	private List<ITranslationRule> translationRules;

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
			
			sortRules();
			for( ITranslationRule rule: translationRules ) {
				while( rule.apply(statement) ) {
					// apply each rule as many times as possible
					// FIXME non-determinism when precedences match?
				}
			}
			
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
