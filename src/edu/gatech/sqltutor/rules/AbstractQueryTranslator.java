package edu.gatech.sqltutor.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import objects.DatabaseTable;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.SelectNode;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.gatech.sqltutor.IQueryTranslator;
import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;

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
	protected Map<String, FromTable> tableAliases;
	protected Multimap<FromTable, ResultColumn> fromToResult = HashMultimap.create();

	protected SelectNode select;

	public AbstractQueryTranslator() {
		super();
	}

	protected void buildMaps() throws StandardException {
		tableAliases = QueryUtils.buildTableAliasMap(select);
		mapResultToFrom();
	}

	private void mapResultToFrom() {
		fromToResult = HashMultimap.create();
		for( ResultColumn resultColumn: select.getResultColumns() ) {
			String tableName = resultColumn.getTableName();
			if( tableName == null ) {
				log.error("Result column does not have a table name: {}", resultColumn);
				continue;
			}
			FromTable fromTable = tableAliases.get(tableName);
			if( fromTable != null ) {
				fromToResult.put(fromTable, resultColumn);
			} else {
				log.error("No table is aliased by {} for col: {}", tableName, resultColumn);
			}
		}
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
		this.fromToResult = null;
		this.select = null;
		this.tableAliases = null;
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
}