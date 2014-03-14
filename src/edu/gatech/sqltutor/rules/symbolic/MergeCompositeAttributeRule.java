package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeListToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeToken;

public class MergeCompositeAttributeRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(MergeCompositeAttributeRule.class);
	
	private static final StaticRules staticRules = new StaticRules(MergeCompositeAttributeRule.class);
	
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleMergeCompositeAttribute", 9);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?attrList",
			"?attrToken1", "?attrPos1", "?attrToken2", "?attrPos2",
			"?tokenEntity", "?tokenAttribute1", "?tokenAttribute2", "?parentAttr")
	);
	
	public MergeCompositeAttributeRule() { }

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		_log.info(Markers.SYMBOLIC, "Extractor variables: {}", ext.getVariables());
		_log.info(Markers.SYMBOLIC, "Could apply to results: {}", relation);
		
		ITuple result = relation.get(0);
		ext.setCurrentTuple(result);
		
		AttributeListToken attrList = ext.getToken("?attrList");
		AttributeToken attrToken1 = ext.getToken("?attrToken1"),
				attrToken2 = ext.getToken("?attrToken2");
		
		String attrName1 = ext.getString("?tokenAttribute1");
		
		ERDiagram erDiagram = state.getSqlState().getErDiagram();
		
		String entityName = ext.getString("?tokenEntity");
		String parentAttr = ext.getString("?parentAttr");
		String parentFullName = entityName + "." + parentAttr;
		
		// always delete the second token
		attrList.removeChild(attrToken2);
		
		if( attrName1.equals(parentAttr) ) {
			// first attribute is the composite in this case, no other action
			_log.debug(Markers.SYMBOLIC, "Merged {} with {} in {}", attrToken2, attrToken1, attrList);
		} else {
			// replacing with the composite
			ERAttribute composite = erDiagram.getAttribute(parentFullName);
			if( composite == null )
				throw new SQLTutorException("Could not find parent attribute: " + parentFullName);
			AttributeToken compositeToken = new AttributeToken(composite);
			attrList.replaceChild(attrToken1, compositeToken);
			_log.debug(Markers.SYMBOLIC, "Replaced {} and {} with {} in {}", attrToken1, attrToken2, compositeToken, attrList);
		}
		
		return true;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	protected int getVariableEstimate() { return 10; }
}
