package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
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

/**
 * If there is an attribute list containing all child attributes of a 
 * composite attribute, replace those children with the composite attribute.
 */
public class MergeCompositeAttributeRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(MergeCompositeAttributeRule.class);
	
	private static final String NS_BASE = "ruleMergeCompositeAttribute";
	
	private static final StaticRules staticRules = new StaticRules(MergeCompositeAttributeRule.class);
	
	/** (?attrList, ?entity, ?composite) */
	private static final IPredicate hasAllCompositeChildren = IrisUtil.predicate(NS_BASE + "_hasAllCompositeChildren", 3);
	
	/** (?attrList, ?entity, ?composite, ?attrToken, ?childAttr) */
	private static final IPredicate compositeChild = IrisUtil.predicate(NS_BASE + "_compositeChild", 5);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(hasAllCompositeChildren, "?attrList", "?entity", "?composite")
	);
	
	public MergeCompositeAttributeRule() { }

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		ITuple result = relation.get(0);
		ext.setCurrentTuple(result);
		
		List<AttributeToken> childTokens = getChildren(ext.getTerm("?attrList"), 
			ext.getTerm("?entity"), ext.getTerm("?composite"));

		AttributeListToken attrList = ext.getToken("?attrList");
		// replacing with the composite
		String parentFullName = ext.getString("?entity") + "." + ext.getString("?composite");
		ERDiagram erDiagram = state.getErDiagram();
		ERAttribute composite = erDiagram.getAttribute(parentFullName);
		if( composite == null )
			throw new SQLTutorException("Could not find parent attribute: " + parentFullName);
		AttributeToken compositeToken = new AttributeToken(composite);
		
		_log.debug(Markers.SYMBOLIC, "Merging {} into {}", childTokens, compositeToken);
		for( int i = 1; i < childTokens.size(); ++i )
			attrList.removeChild(childTokens.get(i));
		attrList.replaceChild(childTokens.get(0), compositeToken);
		
		return true;
	}
	
	/** Return the attribute tokens for the children of a composite entity. */
	private List<AttributeToken> getChildren(ITerm attrListId, ITerm entity, ITerm composite) {
		IQuery getChildren = Factory.BASIC.createQuery(
			literal(compositeChild, attrListId, entity, composite, "?attrToken", "?childAttr")
		);
		
		ArrayList<IVariable> bindings = new ArrayList<IVariable>(2);
		IRelation childResult = null;
		try {
			childResult = state.getKnowledgeBase().execute(getChildren, bindings);
		} catch( EvaluationException e ) {
			throw new SymbolicException("Could not get children.", e);
		}
		RelationExtractor ext = new RelationExtractor(bindings);
		ext.setTokenMap(state.getSymbolicFacts().getTokenMap());
		int nChildren = childResult.size();
		
		ArrayList<AttributeToken> tokens = new ArrayList<AttributeToken>(nChildren);
		for( int i = 0; i < nChildren; ++i ) {
			tokens.add(ext.<AttributeToken>getToken("?attrToken", childResult.get(i)));
		}
		return tokens;
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
	protected int getVariableEstimate() { return 3; }
}
