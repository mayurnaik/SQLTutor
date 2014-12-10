/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package edu.gatech.sqltutor.rules.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atteo.evo.inflector.English;

import edu.gatech.sqltutor.rules.symbolic.SymbolicException;

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
	
	public static String negateVerb(String unnegatedVerbPhrase, String unnegatedVerb) {
		final String negatedVerbPhrase;
		switch(unnegatedVerb) {
			case "do":
				negatedVerbPhrase = unnegatedVerbPhrase.replaceFirst("do", "do not");
				break;
			case "does":
				negatedVerbPhrase = unnegatedVerbPhrase.replaceFirst("does", "does not");
				break;
			case "did":
				negatedVerbPhrase = unnegatedVerbPhrase.replaceFirst("did", "did not");
				break;
			case "have":
				negatedVerbPhrase = unnegatedVerbPhrase.replaceFirst("have", "have not");
				break;
			case "has":
				negatedVerbPhrase = unnegatedVerbPhrase.replaceFirst("has", "has not");
				break;
			case "had":
				negatedVerbPhrase = unnegatedVerbPhrase.replaceFirst("had", "had not");
				break;
			case "is":
				negatedVerbPhrase = unnegatedVerbPhrase.replaceFirst("is", "is not");
				break;
			case "were":
				negatedVerbPhrase = unnegatedVerbPhrase.replaceFirst("were", "were not");
				break;
			case "was":
				negatedVerbPhrase = unnegatedVerbPhrase.replaceFirst("was", "was not");
				break;
			default:
				throw new SymbolicException("Unable to find negated verbalization for: " + unnegatedVerbPhrase);
		}
		return negatedVerbPhrase;
	}
	
	public static String nameFormat(String name) {
		return name.replace('_', ' ');
	}
	
	private NLUtil() {}
}
