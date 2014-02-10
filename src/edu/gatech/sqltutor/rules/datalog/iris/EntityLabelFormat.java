package edu.gatech.sqltutor.rules.datalog.iris;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.builtins.FunctionalBuiltin;
import org.deri.iris.factory.Factory;

public class EntityLabelFormat extends FunctionalBuiltin {
	public static final IPredicate PREDICATE = IrisUtil.predicate("ENTITY_LABEL_FORMAT", 2);
	
	public EntityLabelFormat(String inVar, String outVar) {
		super(PREDICATE, IrisUtil.asTerm(inVar), IrisUtil.asTerm(outVar));
	}
	public EntityLabelFormat(ITerm... terms) {
		super(PREDICATE, terms);
	}
	
	@Override
	protected ITerm computeResult(ITerm[] terms) throws EvaluationException {
		if( terms[0] instanceof IStringTerm ) {
			String value = terms[0].getValue().toString();
			value = value.toLowerCase().replace('_', ' ');
			return Factory.TERM.createString(value);
		}
		return null;
	}
}
