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
public class SymbolicFragmentCompanyTest extends SymbolicFragmentTestBase {
	private static final Logger _log = LoggerFactory.getLogger(SymbolicFragmentCompanyTest.class);
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] queries = {
			{"SELECT e.first_name FROM employee e, department d WHERE e.ssn=d.manager_ssn"},
			
			{"SELECT * FROM employee e, works_on wo, project p WHERE e.ssn=wo.employee_ssn AND p.id=wo.project_id"},
			
			{"SELECT e.birthdate, e.address FROM employee e WHERE first_name = 'John' AND middle_initial = 'B' AND last_name = 'Smith'"},

			{"SELECT e.first_name, e.middle_initial, e.last_name, e.address FROM employee e, department d WHERE d.name = 'Research' AND d.id = e.department_id"},

			{"SELECT E.first_name, E.last_name, S.first_name, S.last_name FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn"},

			{"SELECT e.first_name, e.ssn FROM employee e"},

			{"SELECT e.ssn, d.name FROM employee e, department d"},

			{"SELECT ALL e.salary FROM employee e WHERE e.salary > 10000"},

			{"SELECT DISTINCT e.salary FROM employee e"},
			
			{"SELECT p.id, e.first_name, e.middle_initial, e.last_name FROM project p, works_on w, employee e WHERE p.id=w.project_id AND w.employee_ssn=e.ssn"},

			{"SELECT E.first_name, E.last_name, S.salary FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn AND S.salary > 100000 AND S.salary < 1000000"},

			{"SELECT E.first_name, E.last_name, S.salary FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn AND S.salary BETWEEN 100000 AND 1000000"},
			
			// FIXME: what about "SELECT E.*, E.first_name" - should we delete E.first_name? 
			// Or keep it to remove ambiguity (it should be printed twice)?
			{"SELECT E.*, S.* FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn"},
			
			{"SELECT * FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn AND E.salary > 50000 AND S.salary > 100000"},
			
			{"SELECT name FROM department WHERE name = 'Research' OR name = 'Sales'"}
		};
		return Arrays.asList(queries);
	}
	
	public SymbolicFragmentCompanyTest(String query) {
		super(query);
	}
	
	@Override
	protected Logger getLogger() { 
		return _log; 
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
