package edu.gatech.sqltutor.rules;

import org.deri.iris.api.IKnowledgeBase;

import com.akiban.sql.parser.SelectNode;

import edu.gatech.sqltutor.rules.datalog.iris.SQLFacts;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;

public class SQLState {
	private ERDiagram erDiagram;
	private ERMapping erMapping;
	private IKnowledgeBase knowledgeBase;
	private SelectNode ast;
	private SQLFacts sqlFacts;

	public SQLState() {
	}

	public ERDiagram getErDiagram() {
		return erDiagram;
	}

	public void setErDiagram(ERDiagram erDiagram) {
		this.erDiagram = erDiagram;
	}

	public ERMapping getErMapping() {
		return erMapping;
	}

	public void setErMapping(ERMapping erMapping) {
		this.erMapping = erMapping;
	}

	public IKnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	public void setKnowledgeBase(IKnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public SelectNode getAst() {
		return ast;
	}

	public void setAst(SelectNode ast) {
		this.ast = ast;
	}

	public SQLFacts getSqlFacts() {
		return sqlFacts;
	}

	public void setSqlFacts(SQLFacts sqlFacts) {
		this.sqlFacts = sqlFacts;
	}
}
