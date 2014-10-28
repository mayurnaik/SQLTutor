package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.predicate;

import java.util.EnumSet;
import java.util.List;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.ERAttribute.DescriptionType;
import edu.gatech.sqltutor.rules.symbolic.SymbolicException;
import edu.gatech.sqltutor.rules.symbolic.SymbolicQueries;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLNounToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.TableEntityToken;
import edu.gatech.sqltutor.rules.util.NLUtil;


public class DescribingAttributeLabelRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(DescribingAttributeLabelRule.class);
	
	// ruleAttributeDescribes(?table,?eq,?value,?type,?eqParent)
	private static final IPredicate ruleAttributeDescribes = predicate("ruleAttributeDescribes", 6);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(ruleAttributeDescribes, "?table", "?eq", "?value", "?type", "?ent", "?attr")
	);
	
	private static final StaticRules staticRules = new StaticRules(DescribingAttributeLabelRule.class);
	
	public DescribingAttributeLabelRule() {
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		final boolean DEBUG = _log.isDebugEnabled(Markers.SYMBOLIC);
		SymbolicQueries queries = state.getQueries();
		boolean applied = false;
		while( ext.nextTuple() ) {
			// FIXME needs to handle cscopes / {TABLE_ENTITY}
			// and should insert {IS} token, simplifying if possible
			SQLNounToken fromTable = ext.getToken("?table");
			SQLToken binop = ext.getToken("?eq");
			String value = ext.getString("?value"),
				type = ext.getString("?type");
			String entName = ext.getString("?ent"), attrName = ext.getString("?attr");
			ERAttribute attr = state.getErDiagram().getAttribute(entName + "." + attrName);
			
			QueryTreeNode cscope = binop.getConjunctScope();
			TableEntityToken tableEntity = queries.getTableEntityForScope(
				((FromBaseTable)fromTable.getAstNode()).getExposedName(), cscope);
			if( tableEntity == null ) {
				System.out.println(state.toPrettyPrintedString());
				System.out.println("Tokens: " + queries.getTableEntitiesForScope(cscope));
				throw new SymbolicException("FIXME: Need to split for cscope.");
			}
			
			// format the result
			String singular = tableEntity.getSingularLabel(),
				plural = tableEntity.getPluralLabel();
			if( singular == null || plural == null )
				continue; // wait for another rule to give a label we can use
			if( "prepend".equalsIgnoreCase(type) ) {
				singular = value + " " + singular;
				plural = value + " " + plural;
				tableEntity.setDescribed(DescriptionType.PREPEND);
			} else if( "replace".equalsIgnoreCase(type) ) {
				singular = value;
				plural = NLUtil.pluralize(value);
				tableEntity.setDescribed(DescriptionType.REPLACE);
			} else if( "append".equalsIgnoreCase(type) ) {
				singular = singular + " " + value;
				plural = plural + " " + value;
				tableEntity.setDescribed(DescriptionType.APPEND);
			} else {
				throw new SymbolicException("Unsupported describing-attribute type: " + type);
			}
			
			tableEntity.setSingularLabel(singular);
			tableEntity.setPluralLabel(plural);
			if( attr.isKey() )
				tableEntity.setCardinality(1);
			
			// FIXME only do this if no split
			fromTable.setSingularLabel(singular);
			fromTable.setPluralLabel(plural);
			
			if( DEBUG ) _log.debug(Markers.SYMBOLIC, "Updated labels in: {}", fromTable);

			// delete the comparison
			state.deleteNode(binop);
			applied = true;
		}
		
		return applied;
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
		return EnumSet.of(TranslationPhase.SQL_ANALYSIS);
	}
}
