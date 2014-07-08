package edu.gatech.sqltutor.rules.datalog.iris;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.builtins.FunctionalBuiltin;
import org.deri.iris.factory.Factory;

import edu.gatech.sqltutor.rules.util.NLUtil;

public class PluralizeTermBuiltin extends FunctionalBuiltin {
	public static final IPredicate PREDICATE = IrisUtil.predicate("PLURALIZE_TERM", 2);
	public PluralizeTermBuiltin(ITerm... terms) {
		super(PREDICATE, terms);
	}
	
	public PluralizeTermBuiltin(String inVar, String outVar) {
		super(PREDICATE, IrisUtil.asTerm(inVar), IrisUtil.asTerm(outVar));
	}

	@Override
	protected ITerm computeResult(ITerm[] terms) throws EvaluationException {
		if( terms[0] instanceof IStringTerm ) {
			String value = terms[0].getValue().toString();
			value = NLUtil.pluralize(value);
			return Factory.TERM.createString(value);
		}
		return null;
	}

}
