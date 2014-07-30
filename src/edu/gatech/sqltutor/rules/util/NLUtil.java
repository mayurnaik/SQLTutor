package edu.gatech.sqltutor.rules.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atteo.evo.inflector.English;

import edu.gatech.sqltutor.rules.er.ERRelationship;

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
	
	public static String getVerbForm(ERRelationship rel) {
		String verb = rel.getVerbForm();
		if( verb == null )
			verb = rel.getName().toLowerCase().replace('_', ' ');
		System.out.println("verb: " + verb);
		Matcher m = splitPlural.matcher(verb);
		if( m.matches() ) {
			String first = m.group(1).trim();
			if( !first.endsWith("s") ) {
				System.out.println("pluralizing: " + first);
				first = English.plural(first);
				System.out.println("result: " + first);
				verb = first + " " + m.group(2);
			}
		}
		return verb;
	}
	
	private NLUtil() {}
}
