package edu.gatech.sqltutor.rules.lang;

import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.rules.graph.TranslationGraph;

public abstract class AbstractSQLRule implements ITranslationRule {
	protected ERDiagram erDiagram;
	protected ERMapping erMapping;
	protected SelectNode select;

	public AbstractSQLRule(ERDiagram erDiagram, ERMapping erMapping) {
		if( erDiagram == null ) throw new NullPointerException("erDiagram is null");
		if( erMapping == null ) throw new NullPointerException("erMapping is null");
		this.erDiagram = erDiagram;
		this.erMapping = erMapping;
		erMapping.setDiagram(erDiagram);
	}
	
	@Override
	public int getPrecedence() {
		return DefaultPrecedence.DESTRUCTIVE_UPDATE;
	}
	
	@Override
	@Deprecated
	public boolean apply(TranslationGraph graph, StatementNode statement) {
		return apply(statement);
	}
}
