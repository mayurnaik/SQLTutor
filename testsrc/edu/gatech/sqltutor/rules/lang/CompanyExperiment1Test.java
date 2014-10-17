package edu.gatech.sqltutor.rules.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.gatech.sqltutor.TestConst;

@RunWith(Parameterized.class)
public class CompanyExperiment1Test extends SymbolicFragmentTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			// 0
			{"SELECT ALL salary FROM employee", 
				Pattern.quote("Select the salary of each employee.")},
			
			// 1
			{"SELECT DISTINCT salary FROM employee", 
				Pattern.quote("Select the distinct salaries of all employees.")},
			
			// 2
			{"SELECT ssn FROM employee", 
				Pattern.quote("Select the ssn of each employee.")},
			
			// 3
			{"SELECT * FROM employee WHERE (salary BETWEEN 30000 AND 40000) AND dno = 5", 
				// Select all attributes of each employee whose salary is between $30,000 and $40,000 and who works for department 5.
				Pattern.quote("Select all attributes of each employee whose salary is between $30,000 and $40,000 and dno is 5.")},
			
			// 4
			{"SELECT e.fname, e.lname, e.address FROM employee e, department d WHERE d.dname = 'Research' AND d.dnumber = e.dno", 
				// Select the first name, last name and address of each employee who works for the Research department.
				Pattern.quote("Select the first name, last name, and address of each employee who works for the Research department.")},
			
			// 5
			{"SELECT fname, lname FROM employee WHERE super_ssn IS NULL", 
				Pattern.quote("Select the first name and last name of each employee where their super ssn does not exist.")},
			
			// 6
			{"SELECT bdate, address FROM employee WHERE fname = 'John' AND minit = 'B' AND lname = 'Smith'", 
				// Select the birth date and address of each employee whose name is \"John B. Smith\"
				Pattern.quote("Select the birth date and address of each employee whose first name is \"John\", "
						+ "middle initial is \"B\", and last name is \"Smith.\"")},
			
			// 7
			{"SELECT E.lname AS employee_name, S.lname AS supervisor_name FROM employee AS E, employee AS S WHERE E.super_ssn = S.ssn", 
				Pattern.quote("Select the last names of each employee and their supervisor.")},
			
			// 8
			{"SELECT * FROM employee E, employee M, department D WHERE E.salary > M.salary AND E.dno = D.dnumber AND D.mgr_ssn = M.ssn", 
				Pattern.quote("Select all attributes of each employee _e_, employee _e2_, and department where _e_'s salary is greater than _e2_'s salary, " 
						+ "_e_ works for the department, and _e2_ manages the department.")},
			
			// 9
			{"SELECT p.pnumber, d.dnumber, e.lname, e.address, e.bdate FROM project p, department d, employee e WHERE p.dnum = d.dnumber AND d.mgr_ssn = e.ssn AND p.plocation = 'Stafford'", 
				Pattern.quote("Select the numbers of each project and its controlling department "
						+ "and the last name, address, and birth date of each controlling department's manager where the project's location is \"Stafford.\"")},	
		};
		return Arrays.asList(params);
	}

	public CompanyExperiment1Test(String query, String matches) {
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
