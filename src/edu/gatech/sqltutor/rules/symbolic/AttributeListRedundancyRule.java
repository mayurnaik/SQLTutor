package edu.gatech.sqltutor.rules.symbolic;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.literal;

import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.builtins.NotEqualBuiltin;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.gatech.sqltutor.rules.DefaultPrecedence;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.RelationExtractor;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicPredicates;
import edu.gatech.sqltutor.rules.lang.StandardSymbolicRule;
import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;

public class AttributeListRedundancyRule 
		extends StandardSymbolicRule implements ISymbolicTranslationRule {
	private static final Logger _log = LoggerFactory.getLogger(AttributeListRedundancyRule.class);
	
	private static final IQuery QUERY = Factory.BASIC.createQuery(
		// Parent sequence token:
		literal(SymbolicPredicates.type, "?seq1", SymbolicType.SEQUENCE),		
		literal(SymbolicPredicates.parentOf, "?seq1", "?attrList1", 0),
		literal(SymbolicPredicates.type, "?attrList1", SymbolicType.ATTRIBUTE_LIST),

		literal(SymbolicPredicates.type, "?seq2", SymbolicType.SEQUENCE),
		literal(SymbolicPredicates.parentOf, "?seq2", "?attrList2", 0),
		literal(SymbolicPredicates.type, "?attrList2", SymbolicType.ATTRIBUTE_LIST),
		literal(new NotEqualBuiltin(IrisUtil.asTerm("?seq1"), IrisUtil.asTerm("?seq2")))
	);

	public AttributeListRedundancyRule() {
		super(DefaultPrecedence.SIMPLIFYING_SYMBOLIC);
	}

	public AttributeListRedundancyRule(int precedence) {
		super(precedence);
	}

	@Override
	protected boolean handleResult(IRelation relation, RelationExtractor ext) {
		_log.info(Markers.SYMBOLIC, "Got relation: {}", relation);
		
		ITuple result = relation.get(0);
		ext.setCurrentTuple(result);
		ISymbolicToken attrList1 = ext.getToken("?attrList1");
		ISymbolicToken attrList2 = ext.getToken("?attrList2");
		Multiset<ISymbolicToken> attrBag1 = HashMultiset.create();
		Multiset<ISymbolicToken> attrBag2 = HashMultiset.create();
		attrBag1.addAll(attrList1.getChildren());
		attrBag2.addAll(attrList2.getChildren());
		
		if(attrBag1.equals(attrBag2)) {
			ISymbolicToken seq2 = ext.getToken("?seq2");
			seq2.getChildren().remove(attrList2);
			_log.info(Markers.SYMBOLIC, "Deleted {} from {}", attrList2, seq2);
			return true;
		}
		
		return false;
	}

	@Override
	protected IQuery getQuery() {
		return QUERY;
	}
}
