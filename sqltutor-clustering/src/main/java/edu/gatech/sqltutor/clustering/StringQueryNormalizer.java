/*
 *   Copyright (c) 2015 Program Analysis Group, Georgia Tech
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
package edu.gatech.sqltutor.clustering;

import edu.gatech.sqltutor.QueryUtils;

/**
 * A query normalizer that operates at the superficial string level.  
 * This can be used for queries that are malformed and can't be parsed.
 */
public class StringQueryNormalizer implements IQueryNormalizer {

	public StringQueryNormalizer() {
	}

	@Override
	public String normalize(String query) {
		query = QueryUtils.sanitize(query);
		query = query.replaceAll("([!<>]?=|<>|[+\\-*/<>])", " $1 ");
		query = query.replaceAll("([^\\.])\\*", "$1 * ");
		query = query.replaceAll("\\s*,\\s*", ", ");
		query = query.replaceAll("\\s\\s+", " ");
		query = query.replaceAll("^\\s+|\\s+$", "");
		return query;
	}
}
