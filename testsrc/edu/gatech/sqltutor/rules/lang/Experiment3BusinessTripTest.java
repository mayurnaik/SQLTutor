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
public class Experiment3BusinessTripTest extends SymbolicFragmentTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			//0
			{"SELECT name FROM salesperson",
				Pattern.quote("Select the name of each salesperson.")},
			//1
			{"SELECT name, dept_no FROM salesperson",
				Pattern.quote("Select the name and department number of each salesperson.")},
			//2
			{"SELECT * FROM salesperson",
				Pattern.quote("Select all attributes of each salesperson.")},
			//3
			{"SELECT DISTINCT name FROM salesperson",
				Pattern.quote("Select the distinct names of all salespersons.")},
			//4
			{"SELECT DISTINCT name, dept_no FROM salesperson",
				Pattern.quote("Select the distinct values of the names and department numbers of all salespersons.")},
			//5
			{"SELECT DISTINCT * FROM salesperson",
				Pattern.quote("Select the distinct values of all attributes of all salespersons.")},
			//6
			{"SELECT ssn FROM salesperson WHERE name = 'John Smith'",
				Pattern.quote("Select the ssn of each salesperson whose name is \"John Smith.\"")},
			//7
			{"SELECT ssn, dept_no FROM salesperson WHERE name = 'John Smith'",
				Pattern.quote("Select the ssn and department number of each salesperson whose name is \"John Smith.\"")},
			//8
			{"SELECT * FROM salesperson WHERE name = 'John Smith'",
				Pattern.quote("Select all attributes of each salesperson whose name is \"John Smith.\"")},
			//9
			{"SELECT * FROM salesperson WHERE start_year > '2012' AND name = 'John Smith'",
				Pattern.quote("Select all attributes of each salesperson whose start year is later than 2012 and name is \"John Smith.\"")},
			//10
			{"SELECT DISTINCT start_year FROM salesperson WHERE name = 'John Smith'",
				Pattern.quote("Select the distinct start years of all salespersons whose names are \"John Smith.\"")},
			//11
			{"SELECT DISTINCT start_year, dept_no FROM salesperson WHERE name = 'John Smith'",
				Pattern.quote("Select the distinct values of the start years and department numbers of all salespersons whose names are \"John Smith.\"")},
			//12
			{"SELECT DISTINCT * FROM salesperson WHERE name = 'John Smith'",
				Pattern.quote("Select the distinct values of all attributes of all salespersons whose names are \"John Smith.\"")},
			//13
			{"SELECT s.name, t.to_city FROM salesperson s, trip t WHERE s.ssn = t.ssn",
				Pattern.quote("Select the name of each salesperson and the destination of each trip where the salesperson takes the trip.")},
			//14
			{"SELECT s.name, t.to_city FROM salesperson s, trip t WHERE s.ssn = t.ssn AND t.from_city = 'Atlanta' ",
				Pattern.quote("Select the name of each salesperson and the destination of each trip where the salesperson takes the trip " +
					"and the trip's origin is \"Atlanta.\"")},
		};
		return Arrays.asList(params);
	}

	public Experiment3BusinessTripTest(String query, String matches) {
		super(query, matches);
	}

	@Override
	protected String getERDiagramResource() {
		return TestConst.Resources.BUSINESS_TRIP_DIAGRAM;
	}

	@Override
	protected String getERMappingResource() {
		return TestConst.Resources.BUSINESS_TRIP_MAPPING;
	}

}
