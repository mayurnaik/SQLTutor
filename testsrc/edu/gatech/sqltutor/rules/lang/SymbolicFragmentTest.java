package edu.gatech.sqltutor.rules.lang;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.TestConst;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.ERSerializer;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;

@RunWith(Parameterized.class)
public class SymbolicFragmentTest {
	private static final Logger _log = LoggerFactory.getLogger(SymbolicFragmentTest.class);
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] queries = {
			{"SELECT * FROM employee e, works_on wo, project p WHERE e.ssn=wo.employee_ssn AND p.id=wo.project_id"},
			
			{"SELECT e.birthdate, e.address FROM employee e WHERE first_name = 'John' AND middle_initial = 'B' AND last_name = 'Smith'"},

			{"SELECT e.first_name, e.last_name, e.address FROM employee e, department d WHERE d.name = 'Research' AND d.id = e.department_id"},

			{"SELECT E.first_name, E.last_name, S.first_name, S.last_name FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn"},

			{"SELECT e.first_name, e.ssn FROM employee e"},

			{"SELECT e.ssn, d.name FROM employee e, department d"},

			{"SELECT ALL e.salary FROM employee e"},

			{"SELECT DISTINCT e.salary FROM employee e"},
			
			{"SELECT p.id, e.first_name, e.middle_initial, e.last_name FROM project p, works_on w, employee e WHERE p.id=w.project_id AND w.employee_ssn=e.ssn"},

			{"SELECT E.first_name, E.last_name, S.salary FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn AND S.salary > 100000 AND S.salary < 1000000"},

			{"SELECT E.first_name, E.last_name, S.salary FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn AND 100000 <= S.salary AND 1000000 >= S.salary"},
			
			// FIXME: what about "SELECT E.*, E.first_name" - should we delete E.first_name? 
			// Or keep it to remove ambiguity (it should be printed twice)?
			{"SELECT E.*, S.* FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn"},
			
			{"SELECT * FROM employee AS E, employee AS S WHERE E.manager_ssn = S.ssn"},
		};
		return Arrays.asList(queries);
	}
	
	private String query;
	private ERDiagram companyDiagram;
	private ERMapping companyMapping;
	private SymbolicFragmentTranslator translator;
	
	public SymbolicFragmentTest(String query) {
		this.query = query;
	}

	@Before
	public void setUp() throws Exception {
		Class<?> c = SymbolicFragmentTest.class;
		ERSerializer serializer = new ERSerializer();
		companyDiagram = (ERDiagram)serializer.deserialize(
			c.getResourceAsStream(TestConst.Resources.COMPANY_DIAGRAM));
		companyMapping = (ERMapping)serializer.deserialize(
			c.getResourceAsStream(TestConst.Resources.COMPANY_MAPPING));
		
		translator = new SymbolicFragmentTranslator();
		translator.setQuery(query);
		translator.setERDiagram(companyDiagram);
		translator.setERMapping(companyMapping);
	}

	@After
	public void tearDown() throws Exception {
		companyDiagram = null;
		companyMapping = null;
		translator = null;
	}

	@Test
	public void testTranslation() {
		_log.info("Query: " + query);
		
		String result = translator.getTranslation();
		_log.info("Result: " + result);
	}
}
