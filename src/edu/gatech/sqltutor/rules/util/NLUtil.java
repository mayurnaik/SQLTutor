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
	
	public static String getVerbForm(String verb) {
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
	
	public static String nameFormat(String name) {
		return name.replace('_', ' ');
	}
	
	private NLUtil() {}
}
