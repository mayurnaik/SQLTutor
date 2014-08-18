package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.UNUSED;
import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.builtins.EqualBuiltin;
import org.deri.iris.builtins.NotEqualBuiltin;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.AndToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeListToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.util.NLUtil;

public class SimplifyRepeatedAttributesRule extends StandardSymbolicRule {
	private static final Logger _log = LoggerFactory.getLogger(SimplifyRepeatedAttributesRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?attrList1", SymbolicType.ATTRIBUTE_LIST),
		literal(SymbolicPredicates.type, "?attrList2", SymbolicType.ATTRIBUTE_LIST),
		literal(new NotEqualBuiltin(IrisUtil.asTerms("?attrList1", "?attrList2"))),
		literal(SymbolicPredicates.parentOf, "?parent1", "?attrList1", "?pos1"),
		literal(SymbolicPredicates.parentOf, "?parent2", "?attrList2", "?pos2"),
		literal(SymbolicPredicates.parentOf, "?gparent", "?parent1", "?gpos1"),
		literal(SymbolicPredicates.parentOf, "?gparent", "?parent2", "?gpos2"),
		literal(SymbolicPredicates.lastChild, "?parent1", "?lastChild1", UNUSED),
		literal(SymbolicPredicates.lastChild, "?parent2", "?lastChild2", UNUSED)
		
	);
	
	private SymbolicReader tokenReader = new SymbolicReader();

	public SimplifyRepeatedAttributesRule() {
	}

	public SimplifyRepeatedAttributesRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		while( ext.nextTuple() ) {
			AttributeListToken attrList1 = ext.getToken("?attrList1"),
				attrList2 = ext.getToken("?attrList2");
			if( !haveSameContents(attrList1, attrList2) )
				continue;
			ISymbolicToken gparent = ext.getToken("?gparent"), 
					parent1 = ext.getToken("?parent1"),
					parent2 = ext.getToken("?parent2"),
					lastChild1 = ext.getToken("?lastChild1"),
					lastChild2 = ext.getToken("?lastChild2");
			
			if( lastChild1 instanceof AndToken ) {
				if( lastChild2 instanceof AndToken ) {
					lastChild1.addChildren(lastChild2.getChildren());
				} else {
					lastChild1.addChild(lastChild2);
				}
				gparent.removeChild(parent2);
				checkPlurality(attrList1);
				_log.debug(Markers.SYMBOLIC, "Consolidated attributes to: {}", parent1);
			} else {
				if( lastChild2 instanceof AndToken ) {
					lastChild2.addChild(lastChild1);
				} else {
					AndToken newToken = new AndToken();
					parent2.removeChild(lastChild2);
					newToken.addChild(lastChild1);
					newToken.addChild(lastChild2);
					parent2.addChild(newToken);
				}
				gparent.removeChild(parent1);
				checkPlurality(attrList2);
				_log.debug(Markers.SYMBOLIC, "Consolidated attributes to: {}", parent2);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Converts any singular nouns to plural form.
	 * @param parent the ancestor token to check
	 */
	private void checkPlurality(ISymbolicToken parent) {
		Integer parentId = state.getSymbolicFacts().getTokenMap().getObjectId(parent);
		IQuery query = Factory.BASIC.createQuery(
			literal(SymbolicPredicates.ancestorOf, "?parent", "?descendent", UNUSED),
			literal(SymbolicPredicates.partOfSpeech, "?descendent", PartOfSpeech.NOUN_SINGULAR_OR_MASS),
			literal(IrisUtil.builtin(EqualBuiltin.class, "?parent", parentId))
		);
		
		RelationExtractor ext = IrisUtil.executeQuery(query, state);
		while( ext.nextTuple() ) {
			ISymbolicToken token = ext.getToken("?descendent");
			token.setPartOfSpeech(PartOfSpeech.NOUN_PLURAL);
			if( token instanceof LiteralToken ) {
				LiteralToken literal = (LiteralToken)token;
				literal.setExpression(NLUtil.pluralize(literal.getExpression()));
			} 
		}
	}

	private boolean haveSameContents(AttributeListToken attrList1, AttributeListToken attrList2) {
		try {
			// FIXME this is too crude, does not account for ordering differences
			String attrRead1 = tokenReader.readToken(attrList1),
					attrRead2 = tokenReader.readToken(attrList2);
			return attrRead1.equals(attrRead2);
		} catch( UnhandledSymbolicTypeException e ) {
			return false;
		}
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.SIMPLIFYING_LOWERED;
	}

}
