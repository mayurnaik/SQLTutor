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
				Pattern.quote("Select the distinct values of the salaries of all employees.")},
			
			// 2
			{"SELECT ssn FROM employee", 
				Pattern.quote("Select the ssn of each employee.")},
			
			// 3
			{"SELECT * FROM employee WHERE (salary BETWEEN 30000 AND 40000) AND department_id = 5", 
				// Select all attributes of each employee whose salary is between $30,000 and $40,000 and who works for department 5.
				Pattern.quote("Select all attributes of each employee where their salary is between $30,000 and $40,000 and department id is 5.")},
			
			// 4
			{"SELECT e.first_name, e.last_name, e.address FROM employee e, department d WHERE d.name = 'Research' AND d.id = e.department_id", 
				// Select the first name, last name and address of each employee who works for the Research department.
				Pattern.quote("Select the first name, last name, and address of each employee where they work for the Research department.")},
			
			// 5
			{"SELECT first_name, last_name FROM employee WHERE manager_ssn IS NULL", 
				Pattern.quote("Select the first name and last name of each employee where manager ssn does not exist.")},
			
			// 6
			{"SELECT birthdate, address FROM employee WHERE first_name = 'John' AND middle_initial = 'B' AND last_name = 'Smith'", 
				// Select the birth date and address of each employee whose name is \"John B. Smith\"
				Pattern.quote("Select the birth date and address of each employee where their first name is \"John\", "
						+ "their middle initial is \"B\", and their last name is \"Smith\".")},
			
			// 7
			{"SELECT E.last_name AS employee_name, S.last_name AS supervisor_name FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn", 
				Pattern.quote("Select the last names of each employee and their supervisor.")},
			
			// 8
			{"SELECT * FROM employee E, employee M, department D WHERE E.salary > M.salary AND E.department_id = D.id AND D.manager_ssn = M.ssn", 
				Pattern.quote("Select all attributes of each employee, employee, and department where the employee's salary is greater than the employee's salary, " 
						+ "the employee works for the department, and manager ssn is the employee's ssn.")},
			
			// 9
			{"SELECT p.id, d.id, e.last_name, e.address, e.birthdate FROM project p, department d, employee e WHERE p.department_id = d.id AND d.manager_ssn = e.ssn AND d.location = 'Stafford'", 
				Pattern.quote("Select the id of each project, the number of each project's controlling department, "
						+ "and the last name, address, and birth date of each employee where manager ssn is the employee's ssn and location is \"Stafford\".")},	
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
