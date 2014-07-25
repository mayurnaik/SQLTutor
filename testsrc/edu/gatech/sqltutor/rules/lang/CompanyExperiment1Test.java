package edu.gatech.sqltutor.rules.lang;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.gatech.sqltutor.TestConst;

@RunWith(Parameterized.class)
public class CompanyExperiment1Test extends SymbolicFragmentTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			{"SELECT ALL salary FROM employee"},
			{"SELECT DISTINCT salary FROM employee"},
			{"SELECT ssn FROM employee"},
			{"SELECT * FROM employee WHERE (salary BETWEEN 30000 AND 40000) AND department_id = 5"},
			{"SELECT first_name, last_name, address FROM employee, department WHERE name = 'Research' AND id = department_id"},
			{"SELECT first_name, last_name FROM employee WHERE manager_ssn IS NULL"},
			{"SELECT birthdate, address FROM employee WHERE first_name = 'John' AND middle_initial = 'B' AND last_name = 'Smith'"},
			{"SELECT E.last_name AS employee_name, S.last_name AS supervisor_name FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn"},
			{"SELECT * FROM employee E, employee M, department D WHERE E.salary > M.salary AND E.department_id = D.id AND D.manager_ssn = M.ssn"},
			{"SELECT project.id, department.id, last_name, address, birthdate FROM project, department, employee WHERE project.department_id = department.id AND department.manager_ssn = ssn AND location = 'Stafford'"},	
		};
		return Arrays.asList(params);
	}

	public CompanyExperiment1Test(String query) {
		super(query);
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
