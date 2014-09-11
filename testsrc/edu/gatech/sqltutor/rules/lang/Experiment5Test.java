package edu.gatech.sqltutor.rules.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.gatech.sqltutor.TestConst;

@RunWith(Parameterized.class)
public class Experiment5Test extends SymbolicFragmentTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			// 0 (1-1)
			{"SELECT ssn FROM employee",
				null},
			// 1 (1-2)
			{"SELECT super_ssn FROM employee",
				null},
			// 2 (2-1)
			{"SELECT ssn FROM employee WHERE fname = 'Ahmad'",
				null},
			// 3 (2-2)
			{"SELECT e.super_ssn FROM employee e, department d WHERE d.dname = 'Research'",
				null},
			// 4 (3-1)
			{"SELECT E.fname, S.fname FROM employee AS E, employee AS S WHERE E.super_ssn = S.ssn AND E.salary > 20000",
				null},
			// 5 (3-2)
			{"SELECT S.fname FROM employee AS E, employee AS S WHERE E.super_ssn = S.ssn AND E.salary > 20000",
				null}
		};
		return Arrays.asList(params);
	}

	public Experiment5Test(String query, String matches) {
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
