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
