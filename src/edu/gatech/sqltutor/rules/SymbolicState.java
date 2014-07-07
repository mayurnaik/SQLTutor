package edu.gatech.sqltutor.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.datalog.iris.ERFacts;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicFacts;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.RootToken;

public class SymbolicState {
	private static final Logger _log = LoggerFactory.getLogger(SymbolicState.class);
	
	private RootToken rootToken;
	// FIXME support tokens not rooted in the tree
	private List<ISymbolicToken> extraTokens = new ArrayList<ISymbolicToken>();
	
	private SymbolicFacts symbolicFacts;
	private IKnowledgeBase knowledgeBase;
	private ERDiagram erDiagram;
	private ERMapping erMapping;
	private ERFacts erFacts;
	private Map<IPredicate, IRelation> ruleFacts = Maps.newHashMap();
	private List<IRule> rules = Lists.newArrayList();
	
	public SymbolicState() { }
	
	/**
	 * Add a new metarule-generated fact.
	 * 
	 * @param predicate
	 * @param values
	 */
	public void addFact(IPredicate predicate, ITuple values) {
		int arity = predicate.getArity(), nvals = values.size();
		if( arity != nvals ) {
			_log.error(Markers.DATALOG_FACTS, "Arity mismatch ({} vs {}) for predicate {} with vals: {}", 
				arity, nvals, predicate, values);
			throw new SQLTutorException("Length of values (" + nvals + ") does not match arity (" 
				+ arity + ") of predicate " + predicate);
		}
		IRelation rel = ruleFacts.get(predicate);
		if( rel == null )
			ruleFacts.put(predicate, rel = IrisUtil.relation());
		rel.add(values);
		_log.debug(Markers.DATALOG_FACTS, "New fact: {}{}", predicate.getPredicateSymbol(), values);
	}
	
	/**
	 * Get facts added by metarules.
	 * @return
	 */
	public Map<IPredicate, IRelation> getRuleFacts() {
		return ruleFacts;
	}
	
	public void addRule(IRule rule) {
		rules.add(rule);
		_log.debug(Markers.DATALOG_RULES, "New rule: {}", rule);
	}
	
	/**
	 * Get datalog rules add by the metarules.
	 * @return
	 */
	public List<IRule> getRules() {
		return rules;
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

	public RootToken getRootToken() {
		return rootToken;
	}

	public void setRootToken(RootToken rootToken) {
		this.rootToken = rootToken;
	}

	public IKnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}
	public void setKnowledgeBase(IKnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}
	
	public SymbolicFacts getSymbolicFacts() {
		return symbolicFacts;
	}
	
	public void setSymbolicFacts(SymbolicFacts symbolicFacts) {
		this.symbolicFacts = symbolicFacts;
	}

	public ERFacts getErFacts() {
		return erFacts;
	}

	public void setErFacts(ERFacts erFacts) {
		this.erFacts = erFacts;
	}
}
