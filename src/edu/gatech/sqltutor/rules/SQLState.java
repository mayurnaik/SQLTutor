package edu.gatech.sqltutor.rules;

import java.util.Map;

import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.simple.SimpleRelationFactory;

import com.akiban.sql.parser.SelectNode;
import com.google.common.collect.Maps;

import edu.gatech.sqltutor.rules.datalog.iris.ERFacts;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.SQLFacts;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;

public class SQLState {
	private ERDiagram erDiagram;
	private ERMapping erMapping;
	private IKnowledgeBase knowledgeBase;
	private SelectNode ast;
	private SQLFacts sqlFacts;
	private ERFacts erFacts;
	private Map<IPredicate, IRelation> ruleFacts = Maps.newHashMap();

	public SQLState() {
	}
	
	/**
	 * Add a new metarule-generated fact.
	 * 
	 * @param predicate
	 * @param values
	 */
	public void addFact(IPredicate predicate, ITuple values) {
		IRelation rel = ruleFacts.get(predicate);
		if( rel == null )
			ruleFacts.put(predicate, rel = IrisUtil.relation());
		rel.add(values);
	}
	
	/**
	 * Get facts added by metarules.
	 * @return
	 */
	public Map<IPredicate, IRelation> getRuleFacts() {
		return ruleFacts;
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

	public ERFacts getErFacts() {
		return erFacts;
	}

	public void setErFacts(ERFacts erFacts) {
		this.erFacts = erFacts;
	}
}
