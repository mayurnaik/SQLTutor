package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;

public class SimplifyConjunctionsRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(SimplifyConjunctionsRule.class);
	
	private static final StaticRules staticRules = new StaticRules(SimplifyConjunctionsRule.class);
	
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleSimplifyConjunctions", 4);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?parent", "?child", "?pos", "?type")
	);

	public SimplifyConjunctionsRule() {
	}

	public SimplifyConjunctionsRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		ITuple result = relation.get(0);
		ext.setCurrentTuple(result);
		
		ISymbolicToken parent = ext.getToken("?parent"), child = ext.getToken("?child");
		
		List<ISymbolicToken> parentChildren = parent.getChildren();
		List<ISymbolicToken> grandChildren = child.getChildren();
		int newChildrenCount = parentChildren.size() + grandChildren.size() - 1;
		
		List<ISymbolicToken> mergedChildren = new ArrayList<ISymbolicToken>(newChildrenCount);
		for( ISymbolicToken oldChild: parentChildren ) {
			if( oldChild != child ) {
				mergedChildren.add(oldChild);
			} else {
				for( ISymbolicToken grandchild: grandChildren )
					mergedChildren.add(grandchild);
			}
		}
		
		parentChildren.clear();
		for( ISymbolicToken newChild: mergedChildren )
			parent.addChild(newChild);
		
		if( _log.isDebugEnabled(Markers.SYMBOLIC) )
			_log.debug(Markers.SYMBOLIC, "Merged {} nodes into {}", ext.getTerm("?type"), parent);
		
		return true;
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
}
