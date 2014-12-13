/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
			{"SELECT e.fname FROM employee e, department d WHERE e.ssn=d.mgr_ssn"},
			
			{"SELECT * FROM employee e, works_on wo, project p WHERE e.ssn=wo.essn AND p.pnumber=wo.pno"},
			
			{"SELECT e.bdate, e.address FROM employee e WHERE fname = 'John' AND minit = 'B' AND lname = 'Smith'"},

			{"SELECT e.fname, e.minit, e.lname, e.address FROM employee e, department d WHERE d.dname = 'Research' AND d.dnumber = e.dno"},

			{"SELECT E.fname, E.lname, S.fname, S.lname FROM employee AS E, employee AS S WHERE E.mgr_ssn = S.ssn"},

			{"SELECT e.fname, e.ssn FROM employee e"},

			{"SELECT e.ssn, d.dname FROM employee e, department d"},

			{"SELECT ALL e.salary FROM employee e WHERE e.salary > 10000"},

			{"SELECT DISTINCT e.salary FROM employee e"},
			
			{"SELECT p.pnumber, e.fname, e.minit, e.lname FROM project p, works_on w, employee e WHERE p.pnumber=w.pno AND w.essn=e.ssn"},

			{"SELECT E.fname, E.lname, S.salary FROM employee AS E, employee AS S WHERE E.mgr_ssn = S.ssn AND S.salary > 100000 AND S.salary < 1000000"},

			{"SELECT E.fname, E.lname, S.salary FROM employee AS E, employee AS S WHERE E.mgr_ssn = S.ssn AND S.salary BETWEEN 100000 AND 1000000"},
			
			// FIXME: what about "SELECT E.*, E.first_name" - should we delete E.first_name? 
			// Or keep it to remove ambiguity (it should be printed twice)?
			{"SELECT E.*, S.* FROM employee AS E, employee AS S WHERE E.mgr_ssn = S.ssn"},
			
			{"SELECT * FROM employee AS E, employee AS S WHERE E.mgr_ssn = S.ssn AND E.salary > 50000 AND S.salary > 100000"},
			
			{"SELECT dname FROM department WHERE dname = 'Research' OR dname = 'Sales'"}
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
