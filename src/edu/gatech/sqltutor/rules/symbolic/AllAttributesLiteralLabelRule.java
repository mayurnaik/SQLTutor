package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.Random;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.LearnedPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.AttributeToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;

public class AllAttributesLiteralLabelRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(AllAttributesLiteralLabelRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.parentOf, "?parent", "?token", "?pos"),
		literal(SymbolicPredicates.type, "?token", "ALL_ATTRIBUTES")//,
	); 
	
	public AllAttributesLiteralLabelRule() {
		super(DefaultPrecedence.LOWERING);
	}
	
	public AllAttributesLiteralLabelRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		ITuple result = relation.get(0);
		ISymbolicToken token = ext.getToken("?token", result);
		ISymbolicToken parent = ext.getToken("?parent", result);
		// FIXME what about multi-word labels like "Research Department"?
		SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);
		seq.addChild(new LiteralToken("all", PartOfSpeech.DETERMINER));
		seq.addChild(new LiteralToken("attributes", PartOfSpeech.NOUN_PLURAL));
		
		SymbolicUtil.replaceChild(parent, token, seq);
		_log.debug(Markers.SYMBOLIC, "Replaced token {} with {}", token, "attributes");
		return true;
	}
	
	@Override
	protected IQuery getQuery() { return QUERY; }
	
	@Override
	public int getPrecedence() {
		return DefaultPrecedence.LOWERING;
	}
}
