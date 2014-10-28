package edu.gatech.sqltutor.rules.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.gatech.sqltutor.TestConst;

@RunWith(Parameterized.class)
public class IssuesTest extends SymbolicFragmentTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			// 0
			// Select the location of the ProductX project where it's controlling department's number is 5.
			{"SELECT plocation FROM project WHERE pname = 'ProductX' AND dnum = 5;", 
				Pattern.quote("Select the location of the ProductX project where department 5 controls the ProductX project.")},
			
			// 1
			// Retrieve the ssn of each employee and the name of each project where the project is controlled by some department, the employee manages the project's controlling department, and the manager's start date is after 01-01-1990.
			{"SELECT mgr_ssn, pname FROM project, department WHERE dnum = dnumber AND mgr_start_date > '1990-01-01'", 
				Pattern.quote("Select the name of each project and the ssn of each employee where the project's employee's department managed's start date is greater than \"1990-01-01.\"")},
			
			// 2
			// Select the name of each project that some employee has worked on for more than 30 hours.
			{"SELECT pname FROM project, works_on WHERE hours > 30 AND pnumber = pno", 
				Pattern.quote("Select the name of each project where the works on's hours is greater than 30 and the project's number is the works on's pno.")},
			
			// 3
			// For each employee whose first name is John, retrieve that employee's dependents' names.
			{"SELECT dependent_name FROM employee AS E, dependent AS D WHERE E.ssn = D.essn AND e.fname = 'John'", 
				Pattern.quote("Select the name of each dependent where the employee dependents of the dependent and the employee's first name is \"John\".")},
			
			// 4
			{"SELECT mgr_start_date FROM department d WHERE dname = 'Research'", 
				Pattern.quote("Select the start date of the Research department.")},
			
			// 5
			{"SELECT fname, lname FROM employee, department", 
				Pattern.quote("Select the first name and last name of each employee.")},
			
			// 6
			// e works for the Research department, not e2.
			{"SELECT e.super_ssn FROM employee e, department d WHERE e.dno = d.dnumber AND d.dname = 'Research'", 
				Pattern.quote("Select the ssn of each employee _e2_ who supervises the employee _e_ and works for the Research department.")},
			
			// 7
			// First, this should say “supervises (a/some) employee”. Second, in this situation we should perhaps just apply e2 and e’s label from the ER (supervisor and supervisee) - or apply one of the two
			{"Select super_ssn FROM employee", 
				Pattern.quote("Select the ssn of each employee _e2_ who supervises the employee _e_.")},
			
			// 8
			{"SELECT E.lname AS employee_name, S.lname AS supervisor_name FROM employee AS E JOIN employee AS S ON (E.super_ssn = S.ssn)", 
				Pattern.quote("Select the last names of each employee and their supervisor.")},
		};
		return Arrays.asList(params);
	}

	public IssuesTest(String query, String matches) {
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
