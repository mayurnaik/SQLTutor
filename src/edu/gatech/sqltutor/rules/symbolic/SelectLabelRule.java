package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.List;
import java.util.Random;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;

public class SelectLabelRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(SelectLabelRule.class);
	
	private static final List<String> LITERAL_CHOICES = 
		ImmutableList.of("select", "retrieve", "show", "list", "display");
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		literal(SymbolicPredicates.type, "?token", SymbolicType.SELECT),
		literal(SymbolicPredicates.parentOf, "?parent", "?token", "?pos")
	);
	
	private Random random = new Random();
	
	public SelectLabelRule() {
	}
	
	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		ITuple result = relation.get(0);
		
		ISymbolicToken select = ext.getToken("?token", result);//symbolicFacts.getTokenMap().getMappedObject(result.get(0));
		ISymbolicToken parent = ext.getToken("?parent", result);//symbolicFacts.getParent(select, kb);
		
		// FIXME non-determinism so that all choices are used eventually
		String replacement = LITERAL_CHOICES.get(random.nextInt(LITERAL_CHOICES.size()));
		LiteralToken literal = new LiteralToken(replacement, PartOfSpeech.VERB_BASE_FORM);
		
		SymbolicUtil.replaceChild(parent, select, literal);
		_log.info(Markers.SYMBOLIC, "Replaced token {} with {}", select, literal);
		
		return true;
	}
	
	@Override
	public IQuery getQuery() { return QUERY; }
}
