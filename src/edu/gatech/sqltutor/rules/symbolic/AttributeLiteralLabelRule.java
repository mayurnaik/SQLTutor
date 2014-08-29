package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.TranslationPhase;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.SequenceToken;
import edu.gatech.sqltutor.rules.util.Literals;

public class AttributeLiteralLabelRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(AttributeLiteralLabelRule.class);
	
	private static final StaticRules staticRules = new StaticRules(AttributeLiteralLabelRule.class);
	
	private static final IPredicate PREDICATE = IrisUtil.predicate("ruleAttributeLiteralLabel", 7);
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(PREDICATE, "?parent", "?token", "?pos", "?entity", "?attribute", "?label", "?source")
	);

	private Random random = new Random();
	
	public AttributeLiteralLabelRule() {
	}
	
	public AttributeLiteralLabelRule(int precedence) {
		super(precedence);
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		// FIXME need way to ensure all choices will be used eventually
		int choices = countChoicesForAttribute(relation, ext);
		ITuple result = relation.get(random.nextInt(choices));
		ext.setCurrentTuple(result);
		
		String label = ext.getString("?label");
		ISymbolicToken token = ext.getToken("?token");
		ISymbolicToken parent = ext.getToken("?parent");
		int pos = ext.getInteger("?pos");
		
		ISymbolicToken replacement;
		
		// FIXME what about multi-word labels like "Research Department"?
		LiteralToken literal = new LiteralToken(label, token.getPartOfSpeech());
		
//		ISymbolicToken before = SymbolicUtil.getPotentialDeterminer(token);
		List<ISymbolicToken> siblings = parent.getChildren();
		LiteralToken determiner = null;
		if( needsDeterminer(token) )
			determiner = Literals.the(); // FIXME "a"/"an"?
		
		if( parent.getType() == SymbolicType.ATTRIBUTE_LIST || pos == 0 ) {
			if( determiner != null ) {
				SequenceToken seq = new SequenceToken(PartOfSpeech.NOUN_PHRASE);
				seq.addChild(determiner);
				seq.addChild(literal);
				replacement = seq;
			} else {
				replacement = literal;
			}
		} else {
			replacement = literal;
			
			if( determiner != null ) {
				_log.debug(Markers.SYMBOLIC, "Inserting {} in front of {}", determiner, parent);
				siblings.add(0, determiner);
			}
		}
		
		SymbolicUtil.replaceChild(parent, token, replacement);
		_log.debug(Markers.SYMBOLIC, "Replaced token {} with {}", token, replacement);
		return true;
	}
	
	private boolean needsDeterminer(ISymbolicToken token) {
		ISymbolicToken before = SymbolicUtil.getPotentialDeterminer(token);
		if( before == null )
			return true;
		PartOfSpeech pos = before.getPartOfSpeech();
		if( pos.isPossessive() || pos == PartOfSpeech.DETERMINER )
			return false;
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
	protected int getVariableEstimate() {
		return PREDICATE.getArity();
	}
	
	@Override
	public List<IRule> getDatalogRules() {
		return staticRules.getRules();
	}
	
	@Override
	protected int getDefaultPrecedence() {
		return DefaultPrecedence.PARTIAL_LOWERING;
	}
	
	@Override
	protected EnumSet<TranslationPhase> getDefaultPhases() {
		return EnumSet.of(TranslationPhase.LOWERING);
	}
}
