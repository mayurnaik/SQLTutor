package edu.gatech.sqltutor.rules.symbolic;

import java.util.List;
import java.util.Random;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.SymbolicState;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicFacts;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.AbstractSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;

public class SelectLabelRule 
		extends AbstractSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(SelectLabelRule.class);
	
	private static final List<String> LITERAL_CHOICES = 
		ImmutableList.of("select", "retrieve", "show", "list", "display");
	
	private Random random = new Random();
	
	public SelectLabelRule() {
	}

	@Override
	public boolean apply(SymbolicState state) {
		this.state = state;
		try {
			IQuery query = Factory.BASIC.createQuery(
				IrisUtil.literal(SymbolicPredicates.type, "?token", SymbolicType.SELECT)
			);
			IKnowledgeBase kb = state.getKnowledgeBase();
			IRelation relation = kb.execute(query);
			if( relation.size() < 1 )
				return false;
			
			ITuple result = relation.get(0);
			SymbolicFacts symbolicFacts = state.getSymbolicFacts();
			ISymbolicToken select = symbolicFacts.getTokenMap().getMappedObject(result.get(0));
			ISymbolicToken parent = symbolicFacts.getParent(select, kb);
			
			// FIXME non-determinism so that all choices are used eventually
			String replacement = LITERAL_CHOICES.get(random.nextInt(LITERAL_CHOICES.size()));
			LiteralToken literal = new LiteralToken(replacement, PartOfSpeech.VERB_BASE_FORM);
			
			if( !SymbolicUtil.replaceChild(parent, select, literal) )
				throw new SymbolicException("Failed to replace " + select + " with " + literal + " in parent " + parent);
			_log.info(Markers.SYMBOLIC, "Replaced token {} with {}", select, literal);
			
			return true;
			
		} catch( EvaluationException e ) {
			throw new SymbolicException(e);
		} finally {
			this.state = null;
		}
	}

}
