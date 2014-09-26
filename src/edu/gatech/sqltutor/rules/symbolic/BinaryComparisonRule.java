package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;
import java.util.List;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.BinaryComparisonToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;

public class BinaryComparisonRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(BinaryComparisonRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?token", SymbolicType.BINARY_COMPARISON),
		literal(SymbolicPredicates.binaryOperator, "?token", "?operator"),
		literal(SymbolicPredicates.parentOf, "?parent", "?token", "?pos")
	);
	
	public BinaryComparisonRule() {
		super(DefaultPrecedence.LOWERING);
	}
	
	public BinaryComparisonRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		ITuple result = relation.get(0);
		
		BinaryComparisonToken token = (BinaryComparisonToken)ext.getToken("?token", result);
		ISymbolicToken parent = ext.getToken("?parent", result);
		
		SequenceToken seq = new SequenceToken(PartOfSpeech.VERB_PHRASE);
		List<ISymbolicToken> children = token.getChildren();
		
		// <left> (is|is not|is greater/less than|is greater/less than or equal to) <right>
		seq.addChild(children.get(0));
		seq.addChild(new LiteralToken("is", PartOfSpeech.VERB_RD_PERSON_SINGULAR_PRESENT));
		String op = token.getOperator();
		if( "!=".equals(op) ) {
			seq.addChild(new LiteralToken("not", PartOfSpeech.ADVERB));
		} else if ( !"=".equals(op) ) {
			char first = op.charAt(0);
			if( first == '<' )
				seq.addChild(new LiteralToken(lessPhrase(token), PartOfSpeech.ADJECTIVE_COMPARATIVE));
			else
				seq.addChild(new LiteralToken(greaterPhrase(token), PartOfSpeech.ADJECTIVE_COMPARATIVE));
			seq.addChild(new LiteralToken("than", PartOfSpeech.PREPOSITION_OR_SUBORDINATING_CONJUNCTION));
			if( op.length() > 1 )
				orEqualTo(seq);
		}
		seq.addChild(children.get(1));
		
		// now replace
		SymbolicUtil.replaceChild(parent, token, seq);
		_log.debug(Markers.SYMBOLIC, "Replaced {} with {}", token, seq);
		return true;
	}
	
	private String lessPhrase(BinaryComparisonToken token) {
		switch( token.getValueType() ) {
		case DATETIME:
			return "earlier";
		default:
			return "less";
		}
	}
	
	private String greaterPhrase(BinaryComparisonToken token) {
		switch( token.getValueType() ) {
		case DATETIME:
			return "later";
		default:
			return "greater";
		}
		
	}
	
	private void orEqualTo(ISymbolicToken token) {
		token.addChild(new LiteralToken("or", PartOfSpeech.COORDINATING_CONJUNCTION));
		token.addChild(new LiteralToken("equal", PartOfSpeech.ADJECTIVE));
		token.addChild(new LiteralToken("to", PartOfSpeech.TO));
	}
	
	@Override
	protected IQuery getQuery() { return QUERY; }
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
}
