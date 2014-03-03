package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.SymbolicState;
import edu.gatech.sqltutor.rules.datalog.iris.LearnedPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.AbstractSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;
import edu.gatech.sqltutor.rules.symbolic.tokens.LiteralToken;

public class AttributeLiteralLabelRule 
		extends AbstractSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(AttributeLiteralLabelRule.class);

	private Random random = new Random();
	
	public AttributeLiteralLabelRule() {
	}

	@Override
	public boolean apply(SymbolicState state) {
		this.state = state;
		try {
			IKnowledgeBase kb = state.getKnowledgeBase();
			IQuery attributeNode = Factory.BASIC.createQuery(
				literal(SymbolicPredicates.type, "?token", "ATTRIBUTE"),
				literal(SymbolicPredicates.refsAttribute, "?token", "?entity", "?attribute"),
				literal(LearnedPredicates.attributeLabel, "?entity", "?attribute", "?label", "?source")
			);
			
			List<IVariable> bindings = new ArrayList<IVariable>(5);
			IRelation results = kb.execute(attributeNode, bindings);
			if( results.size() < 1 )
				return false;
			
			
			RelationExtractor ext = new RelationExtractor(bindings);
			ext.setTokenMap(state.getSymbolicFacts().getTokenMap());
			
			// FIXME need way to ensure all choices will be used eventually
			int choices = countChoicesForAttribute(results, ext);
			ITuple result = results.get(random.nextInt(choices));
			
			String label = ((IStringTerm)ext.getTerm("?label", result)).getValue();
			ISymbolicToken token = ext.getToken("?token", result);
			ISymbolicToken parent = state.getSymbolicFacts().getParent(token, kb);
			// FIXME what about multi-word labels like "Research Department"?
			LiteralToken literal = new LiteralToken(label, PartOfSpeech.NOUN_SINGULAR_OR_MASS);
			
			if( !SymbolicUtil.replaceChild(parent, token, literal) )
				throw new SQLTutorException("Replacement failed for token " + token + " from parent " + parent);
			_log.info(Markers.SYMBOLIC, "Replaced token {} with {}", token, literal);
			return true;
		} catch( EvaluationException e ) {
			throw new SymbolicException(e);
		} finally {
			this.state = null;
		}
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
}
