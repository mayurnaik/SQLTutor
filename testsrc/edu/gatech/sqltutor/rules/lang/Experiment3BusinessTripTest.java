package edu.gatech.sqltutor.rules.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.gatech.sqltutor.TestConst;

@RunWith(Parameterized.class)
@Ignore("BusinessTrip schema not yet encoded")
public class Experiment3BusinessTripTest extends SymbolicFragmentTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			//0
			{"SELECT name FROM salesperson",
				null},
			//1
			{"SELECT name, dept_no FROM salesperson",
				null},
			//2
			{"SELECT * FROM salesperson",
				null},
			//3
			{"SELECT DISTINCT name FROM salesperson",
				null},
			//4
			{"SELECT DISTINCT name, dept_no FROM salesperson",
				null},
			//5
			{"SELECT DISTINCT * FROM salesperson",
				null},
			//6
			{"SELECT ssn FROM salesperson WHERE name = ‘John Smith’",
				null},
			//7
			{"SELECT ssn, dept_no FROM salesperson WHERE name = ‘John Smith’",
				null},
			//8
			{"SELECT * FROM salesperson WHERE name = ‘John Smith’",
				null},
			//9
			{"SELECT * FROM salesperson WHERE start_year > ‘2012’ AND name = ‘John Smith’",
				null},
			//10
			{"SELECT DISTINCT start_year FROM salesperson WHERE name = ‘John Smith’",
				null},
			//11
			{"SELECT DISTINCT start_year, dept_no FROM salesperson WHERE name = ‘John Smith’",
				null},
			//12
			{"SELECT DISTINCT * FROM salesperson WHERE name = ‘John Smith’",
				null},
			//13
			{"SELECT name, to_city FROM salesperson, trip WHERE salesperson.ssn = trip.ssn",
				null},
			//14
			{"SELECT name, to_city FROM salesperson, trip WHERE salesperson.ssn = trip.ssn  AND from_city = ‘Atlanta’",
				null},
		};
		return Arrays.asList(params);
	}

	public Experiment3BusinessTripTest(String query, String matches) {
		super(query, matches);
	}

	@Override
	protected String getERDiagramResource() {
		return TestConst.Resources.COMPANY_DIAGRAM;
	}

	@Override
	protected String getERMappingResource() {
		return TestConst.Resources.COMPANY_MAPPING;
	}

}
