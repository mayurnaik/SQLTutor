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
package edu.gatech.sqltutor.rules.lang;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.ERSerializer;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;

public abstract class SymbolicFragmentTestBase {
	private static final Logger _log = LoggerFactory.getLogger(SymbolicFragmentTestBase.class);
	
	protected String query;
	protected String matches;
	protected ERDiagram erDiagram;
	protected ERMapping erMapping;
	protected SymbolicFragmentTranslator translator;
	
	public SymbolicFragmentTestBase(String query) { this(query, null); }
	public SymbolicFragmentTestBase(String query, String matches) {
		this.query = query;
		this.matches = matches;
	}
	
	@Before
	public void setUp() throws Exception {
		Class<?> c = this.getClass();
		ERSerializer serializer = new ERSerializer();
		erDiagram = (ERDiagram)serializer.deserialize(
			c.getResourceAsStream(getERDiagramResource()));
		erMapping = (ERMapping)serializer.deserialize(
			c.getResourceAsStream(getERMappingResource()));
		
		translator = new SymbolicFragmentTranslator();
		translator.setQuery(query);
		translator.setERDiagram(erDiagram);
		translator.setERMapping(erMapping);
	}
	
	@After
	public void tearDown() throws Exception {
		erDiagram = null;
		erMapping = null;
		translator = null;
	}
	
	@Test
	public void testTranslation() throws Exception {
		Logger log = getLogger();
		log.info("Query: " + query);
		try {
			String result = translator.getTranslation();
			log.info("Result: " + result);
			
			if( matches != null ) {
				Assert.assertThat("Translation did not match expected result.", result, 
					new BaseMatcher<String>() {
						@Override
						public boolean matches(Object item) {
							return item.toString().matches(matches);
						}
						
						@Override
						public void describeTo(Description description) {
							description.appendText("must match regex: ").appendText(matches);
						}
					
					});
			}
		} catch( Exception e ) {
			log.error("Failed to translate.");
			throw e;
		}
	}
	
	protected Logger getLogger() { return _log; }
	
	/** Return the resource path for the serialized ER diagram. */
	protected abstract String getERDiagramResource();
	/** Return the resource path for the serialized ER-relational mapping. */
	protected abstract String getERMappingResource();

}
