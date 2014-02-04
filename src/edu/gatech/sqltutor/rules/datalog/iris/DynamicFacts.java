package edu.gatech.sqltutor.rules.datalog.iris;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public abstract class DynamicFacts {
	private static final Logger _log = LoggerFactory.getLogger(DynamicFacts.class);

	/** Generated facts. */
	protected Map<IPredicate, IRelation> facts = Maps.newHashMap();

	public DynamicFacts() {
	}

	public void reset() {
		facts.clear();
	}

	public Map<IPredicate, IRelation> getFacts() {
		return facts;
	}

	protected void addFact(IPredicate pred, Object... vals) {
		ITuple tuple = IrisUtil.asTuple(vals); 
		IRelation rel = facts.get(pred);
		if( rel == null )
			facts.put(pred, rel = IrisUtil.relation());
		rel.add(tuple);
		_log.debug("Added fact: {}{}", pred.getPredicateSymbol(), tuple);
	}

}