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

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.TestConst;

@RunWith(Parameterized.class)
public class BusinessTripTest extends SymbolicFragmentTestBase {
	private static final Logger _log = LoggerFactory.getLogger(BusinessTripTest.class);
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
				
		};
		return Arrays.asList(params);
	}
	
	public BusinessTripTest(String query) {
		super(query);
	}

	@Override
	protected String getERDiagramResource() {
		return TestConst.Resources.BUSINESS_TRIP_DIAGRAM;
	}

	@Override
	protected String getERMappingResource() {
		return TestConst.Resources.BUSINESS_TRIP_MAPPING;
	}
	
	@Override
	protected Logger getLogger() {
		return _log;
	}
}
