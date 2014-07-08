package edu.gatech.sqltutor.rules.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atteo.evo.inflector.English;

public final class NLUtil {
	private static final Pattern splitPlural = Pattern.compile("^(.+ )(\\w+)$");
	public static String pluralize(String value) {
		Matcher m = splitPlural.matcher(value);
		if( !m.matches() ) {
			value = English.plural(value);
		} else {
			value = m.group(1) + English.plural(m.group(2));
		}
		return value;
	}
	
	private NLUtil() {}
}
