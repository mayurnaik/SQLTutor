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
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;

public class AttributeLiteralLabelRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(AttributeLiteralLabelRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.parentOf, "?parent", "?token", "?pos"),
		literal(SymbolicPredicates.type, "?token", "ATTRIBUTE"),
		literal(SymbolicPredicates.refsAttribute, "?token", "?entity", "?attribute"),
		literal(LearnedPredicates.attributeLabel, "?entity", "?attribute", "?label", "?source")
	); 

	private Random random = new Random();
	
	public AttributeLiteralLabelRule() {
		super(DefaultPrecedence.LOWERING);
	}
	
	public AttributeLiteralLabelRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		// FIXME need way to ensure all choices will be used eventually
		int choices = countChoicesForAttribute(relation, ext);
		ITuple result = relation.get(random.nextInt(choices));
		
		String label = ((IStringTerm)ext.getTerm("?label", result)).getValue();
		ISymbolicToken token = ext.getToken("?token", result);
		ISymbolicToken parent = ext.getToken("?parent", result);
		// FIXME what about multi-word labels like "Research Department"?
		SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);
		seq.addChild(new LiteralToken("the", PartOfSpeech.DETERMINER));
		LiteralToken literal = new LiteralToken(label, PartOfSpeech.NOUN_SINGULAR_OR_MASS);
		seq.addChild(literal);
		
		SymbolicUtil.replaceChild(parent, token, seq);
		_log.debug(Markers.SYMBOLIC, "Replaced token {} with {}", token, literal);
		return true;
	}

	public int countChoicesForAttribute(IRelation results, RelationExtractor ext) {
		if( results.size() < 1 )
			return 0;
		
		String lastAttr = null;
		int i, ilen;
		for( i = 0, ilen = results.size(); i < ilen; ++i ) {
			ITuple result = results.get(i);
			String attr = ext.getTerm("?entity", result) + "." + ext.getTerm("?attribute", result);
			if( lastAttr == null )
				lastAttr = attr;
			else if( !lastAttr.equals(attr) )
				break;
		}
		
		return i;
	}
	
	@Override
	protected IQuery getQuery() { return QUERY; }
	
	@Override
	public int getPrecedence() {
		return DefaultPrecedence.LOWERING;
	}
}
