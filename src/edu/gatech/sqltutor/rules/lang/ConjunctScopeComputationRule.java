package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;

import com.akiban.sql.parser.OrNode;
import com.akiban.sql.parser.QueryTreeNode;

import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SQLPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.symbolic.SymbolicType;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SQLToken;

/**
 * Preprocessing rule that assigns conjunct scopes to all SQL tokens.
 */
public class ConjunctScopeComputationRule extends StandardSymbolicRule
		implements ITranslationRule {
	// there is an SQL token without an assigned conjunct-scope
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?token", SymbolicType.SQL_AST),
		literal(false, SymbolicPredicates.conjunctScope, "?token", "?cscope"),
		// this is just to get a reference to the SELECT node
		literal(SQLPredicates.nodeHasType, "?select", "SelectNode")
	);
	
	public ConjunctScopeComputationRule() {
	}

	public ConjunctScopeComputationRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		ext.nextTuple();

		SQLToken selectToken = ext.getToken("?select");
		QueryTreeNode cscope = selectToken.getAstNode();
		processToken(cscope, selectToken);
		
		return true;
	}
	
	protected void processToken(QueryTreeNode cscope, SQLToken token) {
		QueryTreeNode astNode = token.getAstNode();
		token.setConjunctScope(cscope);
		// FIXME others, e.g. NOT?
		if( astNode instanceof OrNode ) {
			for( ISymbolicToken c: token.getChildren() ) {
				SQLToken child = (SQLToken)c;
				processToken(child.getAstNode(), child);
			}
		} else {
			for( ISymbolicToken c: token.getChildren() ) {
				SQLToken child = (SQLToken)c;
				processToken(cscope, child);
			}
		}
	}
	
	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.PREPROCESSING);
	}
}
