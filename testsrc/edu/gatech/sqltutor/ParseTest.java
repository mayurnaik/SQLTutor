package edu.gatech.sqltutor;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;

@RunWith(Parameterized.class)
public class ParseTest {
	
	@Parameters()
	public static Collection<Object[]> getParameters() {
		return Arrays.asList(new Object[][] {			
			// p98
			{"SELECT birthdate, address FROM employee WHERE first_name = 'John' AND middle_initial = 'B' AND last_name = 'Smith'"},
			
			// p100
			{"SELECT first_name, last_name, address FROM employee, department WHERE name = 'Research' AND id = department_id"},
			
			// p100
			{"SELECT project.id, department.id, last_name, address, birthdate FROM project, department, employee " + 
				"WHERE project.department_id = department.id AND department.manager_ssn = ssn AND location = 'Stafford'"},
				
			// p101
			{"SELECT E.first_name, E.last_name, S.first_name, S.last_name FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn;"},
			
			// p102
			{"SELECT ssn FROM employee;"},
			
			// p102
			{"SELECT employee.ssn, department.name FROM employee, department"},
			
			// p103
			{"SELECT ALL salary FROM employee"},
			
			// p103
			{"SELECT DISTINCT salary FROM employee"},
			
			// p104
			{"(SELECT DISTINCT project.id FROM project, department, employee " + 
				"WHERE department.id = project.department_id AND department.manager_ssn = employee.ssn " +
				"AND employee.last_name = 'Smith') UNION " +
				"(SELECT DISTINCT project.id FROM project, works_on, employee " +
				"WHERE project.id = works_on.project_id AND works_on.employee_ssn = employee.ssn AND employee.last_name = 'Smith')"
			},
			
			// p105
			{"SELECT first_name, last_name FROM employee WHERE address LIKE '%Houston, TX%'"},
			
			// p105
			{"SELECT first_name, last_name FROM employee WHERE birthdate LIKE '__5_______'"},
			
			// p106			
			{"SELECT E.first_name, E.last_name, 1.1 * employee.salary AS Increased_sal FROM employee AS E, works_on AS W, project AS P " +
				"WHERE E.ssn  = W.employee_ssn AND W.project_id = P.id AND P.name = 'ProductX'"},
				
			// p106			
			{"SELECT * FROM employee WHERE (salary BETWEEN 30000 AND 40000) AND department_id = 5"},
			
			// p106
			{"SELECT D.name, E.last_name, E.first_name, P.name FROM department D, employee E, works_on W, project P " +
				"WHERE D.id = E.department_id AND E.SSN = W.employee_SSN AND W.project_id = P.id ORDER BY D.name, E.last_name, E.first_name"},
			
			// p116
			{"SELECT first_name, last_name FROM employee WHERE manager_ssn IS NULL;"},
			
			// p118
			{"SELECT E.first_name, E.last_name FROM employee AS E WHERE E.SSN IN ( SELECT employee_ssn FROM dependent AS D " +
				"WHERE E.first_name = D.name AND E.sex = D.sex)"},
			
			// p120
			{"SELECT first_name, last_name FROM employee WHERE NOT EXISTS ( SELECT * FROM dependent WHERE SSN = employee_SSN )"},
			
			// p121
			{"SELECT first_name, last_name FROM employee WHERE EXISTS ( SELECT * FROM dependent WHERE ssn = employee_ssn ) " +
				"AND EXISTS ( SELECT * FROM department WHERE ssn = manager_ssn )"},
			
			// p121
			{"SELECT first_name, last_name FROM employee WHERE NOT EXISTS ( ( SELECT id FROM project WHERE department_id = 5 ) " +
				"EXCEPT ( SELECT project_id FROM works_on WHERE SSN = employee_ssn ) )"},
				
			// p122
			{"SELECT last_name, first_name FROM employee WHERE NOT EXISTS " + 
				"( SELECT * FROM works_on B WHERE ( B.project_id IN ( SELECT id FROM project WHERE department_id = 5 ) " +
				"AND NOT EXISTS ( SELECT * FROM works_on C WHERE C.employee_ssn = ssn AND C.project_id = B.project_id ) ) )"},
				
			// p122
			{"SELECT DISTINCT employee_ssn FROM works_on WHERE project_id IN (1, 2, 3)"},
			
			// p122
			{"SELECT employee.last_name AS employee_name, S.last_name AS supervisor_name FROM employee AS E, employee AS S WHERE E.manager_SSN = S.SSN"},
			
		});
	}
	
	private String query;
	
	public ParseTest(String query) {
		this.query = QueryUtils.sanitize(query);
	}
	
	@Test
	public void test() throws StandardException {
		SQLParser parser = new SQLParser();
		System.out.println("Parsing: " + query);
		try {
			StatementNode statement = parser.parseStatement(query);
			statement.treePrint();
			System.out.println();
		} catch( StandardException e ) {
			System.err.println("Parse failed: " + e.getMessage());
			throw e;
		}
	}
}
