package edu.gatech.sqltutor.rules.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.IQueryTranslator;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.AbstractQueryTranslator;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;

public class SymbolicFragmentTranslator 
		extends AbstractQueryTranslator implements IQueryTranslator {
	private static final Logger _log = 
		LoggerFactory.getLogger(SymbolicFragmentTranslator.class);
	
	protected ERDiagram erDiagram;
	protected ERMapping erMapping;
	protected boolean withDefaults;
	protected boolean defaultsAdded;

	public SymbolicFragmentTranslator() {
		this(true);
	}
	
	public SymbolicFragmentTranslator(boolean withDefaults) {
		translationRules = new ArrayList<ITranslationRule>();
		this.withDefaults = withDefaults;
	}
	
	@Override
	protected void computeTranslation() throws SQLTutorException {
		if( erDiagram == null ) throw new SQLTutorException("No ER diagram set.");
		if( erMapping == null ) throw new SQLTutorException("No ER-relational mapping set.");
		erMapping.setDiagram(erDiagram);
		
		if( withDefaults && !defaultsAdded ) {
			translationRules.addAll(makeDefaultRules());
			defaultsAdded = true;
		}
		
		StatementNode statement = parseQuery();
		
		sortRules();
		for( ITranslationRule rule: translationRules ) {
			while( rule.apply(statement) ) {
				// apply each rule as many times as possible
				// FIXME non-determinism when precedences match?
				_log.debug("Applied rule: {}", rule);
			}
		}
		
		_log.info("statement: " + statement.statementToString());
		
		throw new SQLTutorException("FIXME: Not implemented.");
	}

	private Collection<ITranslationRule> makeDefaultRules() {
		return Arrays.<ITranslationRule>asList(
			new JoinLabelRule(erDiagram, erMapping)
		);
	}
	
	@Override
	public void clearResult() {
		super.clearResult();
		defaultsAdded = false;
	}

	@Override
	public Object getTranslatorType() {
		return "Symbolic Language Fragments";
	}

	public ERDiagram getERDiagram() {
		return erDiagram;
	}

	public void setERDiagram(ERDiagram erDiagram) {
		this.erDiagram = erDiagram;
		clearResult();
	}

	public ERMapping getERMapping() {
		return erMapping;
	}

	public void setERMapping(ERMapping erMapping) {
		this.erMapping = erMapping;
		clearResult();
	}
}
