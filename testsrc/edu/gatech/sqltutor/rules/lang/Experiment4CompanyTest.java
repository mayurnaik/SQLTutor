package edu.gatech.sqltutor.rules.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.gatech.sqltutor.TestConst;

@RunWith(Parameterized.class)
public class Experiment4CompanyTest extends SymbolicFragmentTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			//0
			{"SELECT * FROM employee AS E, employee AS S WHERE E.super_ssn = S.ssn AND E.salary > 20000 AND E.fname = 'Ahmad'",
				null},
			//1
			{"SELECT E.* FROM employee AS E, employee AS S WHERE E.super_ssn = S.ssn AND E.salary > 20000",
				null},
			//2
			{"SELECT * FROM employee AS E, employee AS S WHERE E.super_ssn = S.ssn AND E.salary > 20000",
				null},
			//3
			{"SELECT E.fname, S.fname FROM employee AS E, employee AS S WHERE E.super_ssn = S.ssn AND E.salary > 20000",
				null},
			//4
			{"SELECT E.fname, S.fname FROM employee AS E, employee AS S WHERE E.super_ssn = S.ssn",
				null},
			//5
			{"SELECT employee.salary, department.dname FROM employee, department WHERE department.dnumber = employee.dno AND employee.fname = 'Ahmad'",
				null},
			//6
			{"SELECT employee.salary, department.dname FROM employee, department WHERE department.dnumber = employee.dno",
				null},
			//7
			{"SELECT DISTINCT * FROM employee WHERE fname = 'Ahmad'",
				null},
			//8
			{"SELECT DISTINCT fname, salary FROM employee WHERE fname = 'Ahmad'",
				null},
			//9
			{"SELECT DISTINCT fname FROM employee WHERE fname = 'Ahmad'",
				null},
			//10
			{"SELECT * FROM employee WHERE salary > 20000 AND fname = 'Ahmad'",
				null},
			//11
			{"SELECT * FROM employee WHERE fname = 'Ahmad'",
				null},
			//12
			{"SELECT ssn, salary FROM employee WHERE fname = 'Ahmad'",
				null},
			//13
			{"SELECT ssn FROM employee WHERE fname = 'Ahmad'",
				null},
			//14
			{"SELECT DISTINCT * FROM employee",
				null},
			//15
			{"SELECT DISTINCT fname, salary FROM employee",
				null},
			//16
			{"SELECT DISTINCT fname FROM employee",
				null},
			//17
			{"SELECT * FROM employee",
				null},
			//18
			{"SELECT ssn, salary FROM employee",
				null},
			//19
			{"SELECT ssn FROM employee",
				null},
		};
		return Arrays.asList(params);
	}

	public Experiment4CompanyTest(String query, String matches) {
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
