package edu.gatech.sqltutor.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.DatabaseTable;
import edu.gatech.sqltutor.IQueryTranslator;
import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.util.ColumnReferenceResolver;
import edu.gatech.sqltutor.rules.util.StarColumnSplitter;

/**
 * Base class for query translators.  Maintains the query and 
 * basic database metadata.  It also handles alias resolution 
 * in the SQL AST.
 */
public abstract class AbstractQueryTranslator implements IQueryTranslator {
	private static final Logger log = 
		LoggerFactory.getLogger(AbstractQueryTranslator.class);

	protected abstract void computeTranslation() throws SQLTutorException;

	protected String query;
	protected List<DatabaseTable> tables;
	protected String result;
	protected List<ITranslationRule> translationRules;
	protected SQLMaps sqlMaps = new SQLMaps();

	protected SelectNode select;

	public AbstractQueryTranslator() {
		super();
	}
	
	/**
	 * Parse the current query and return its main statement node.
	 * <p>This also sets <code>select</code> and resolves column 
	 * references.</p>
	 * 
	 * @return the statement node
	 * @throws SQLTutorException if there is no query or it could not be parsed
	 */
	protected StatementNode parseQuery() throws SQLTutorException {
		if( query == null )
			throw new IllegalStateException("Query must be set before evaluation.");
		
		SQLParser parser = new SQLParser();
		try {
			log.info("Query: {}", query);
			StatementNode statement = parser.parseStatement(query);
			try {
				select = QueryUtils.extractSelectNode(statement);
			} catch( IllegalArgumentException e ) {
				throw new SQLTutorException("Wrong query type for: " + query, e);
			}
		
			// preprocessing steps, split '*' and *.* columns
			new StarColumnSplitter().split(select);
			// resolve any implicit column references
			new ColumnReferenceResolver(tables).resolve(select);
			
			return statement;
		} catch( StandardException e ) {
			throw new SQLTutorException("Could not parse query: "+ query, e);
		}
	}

	/**
	 * Populates the <code>tableAliases</code> and <code>fromToResult</code> maps.
	 * @throws SQLTutorException if there is an error processing the query
	 */
	protected void buildMaps() throws SQLTutorException {
		sqlMaps.buildMaps(select);
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

	public void clearResult() {
		this.result = null;
		this.sqlMaps.clear();
		this.select = null;
	}

	@Override
	public void setQuery(String sql) {
		this.query = (sql == null ? null : QueryUtils.sanitize(sql));
		clearResult();
	}

	@Override
	public String getQuery() {
		return this.query;
	}

	@Override
	public void setSchemaMetaData(List<DatabaseTable> tables) {
		this.tables = tables;
		clearResult();
	}

	@Override
	public List<DatabaseTable> getSchemaMetaData() {
		return tables;
	}

	@Override
	public String getTranslation() throws SQLTutorException {
		if( result == null )
			computeTranslation();
		return result;
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
}