package edu.gatech.sqltutor.rules.datalog.iris;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atteo.evo.inflector.English;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.builtins.FunctionalBuiltin;
import org.deri.iris.factory.Factory;

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
			
			Pattern p = Pattern.compile("^(.+ )(\\w+)$");
			Matcher m = p.matcher(value);
			if( !m.matches() ) {
				value = English.plural(value);
			} else {
				value = m.group(1) + English.plural(m.group(2));
			}
			
			return Factory.TERM.createString(value);
		}
		return null;
	}

}
