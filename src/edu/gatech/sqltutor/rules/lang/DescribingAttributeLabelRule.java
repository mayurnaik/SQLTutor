package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.predicate;

import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLNounToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;
import edu.gatech.sqltutor.rules.util.NLUtil;


public class DescribingAttributeLabelRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(DescribingAttributeLabelRule.class);
	
	// ruleAttributeDescribes(?table,?eq,?value,?type,?eqParent)
	private static final IPredicate ruleAttributeDescribes = predicate("ruleAttributeDescribes", 5);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(ruleAttributeDescribes, "?table", "?eq", "?value", "?type", "?eqParent")
	);
	
	private static final StaticRules staticRules = new StaticRules(DescribingAttributeLabelRule.class);
	
	public DescribingAttributeLabelRule() {
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		final boolean DEBUG = _log.isDebugEnabled(Markers.SYMBOLIC);
		while( ext.nextTuple() ) {
			// FIXME needs to handle cscopes / {TABLE_ENTITY}
			// and should insert {IS} token, simplifying if possible
			SQLNounToken fromTable = ext.getToken("?table");
			SQLToken binop = ext.getToken("?eq"),
					parent = ext.getToken("?eqParent");
			String value = ext.getString("?value"),
				type = ext.getString("?type");
			
			// format the result
			String singular = fromTable.getSingularLabel(),
				plural = fromTable.getPluralLabel();
			if( "prepend".equalsIgnoreCase(type) ) {
				singular = value + " " + singular;
				plural = value + " " + plural;
			} else if( "replace".equalsIgnoreCase(type) ) {
				singular = value;
				plural = NLUtil.pluralize(value);
			} else {
				throw new SymbolicException("Unsupported describing-attribute type: " + type);
			}
			
			fromTable.setSingularLabel(singular);
			fromTable.setPluralLabel(plural);
			
			if( DEBUG ) _log.debug(Markers.SYMBOLIC, "Updated labels in: {}", fromTable);
			
			// delete the comparison
			QueryTreeNode parentNode = parent.getAstNode();
			if( parentNode instanceof BinaryOperatorNode ) {
				List<ISymbolicToken> siblings = parent.getChildren();
				int eqPos = siblings.indexOf(binop);
				ISymbolicToken sibling;
				if( eqPos == 0 )
					sibling = siblings.get(1);
				else if( eqPos == 1 )
					sibling = siblings.get(0);
				else
					throw new SymbolicException("Expected two children in: " + parent);
				
				ISymbolicToken grandParent = state.getSymbolicFacts().getParent(parent, 
					state.getKnowledgeBase());
				SymbolicUtil.replaceChild(grandParent, parent, sibling);
				if( DEBUG )
					_log.debug(Markers.SYMBOLIC, "Deleted {} in {}", parent, grandParent);
			}
		}
		
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
}
