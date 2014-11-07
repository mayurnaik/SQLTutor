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
			{"SELECT d.mgr_ssn, p.pname FROM project p, department d WHERE p.dnum = d.dnumber AND d.mgr_start_date > '1990-01-01'", 
				Pattern.quote("Select the name of each project and the ssn of each employee where the project's employee's department managed's start date is later than 1990-01-01.")},
			
			// 2
			// Select the name of each project that some employee has worked on for more than 30 hours.
			{"SELECT p.pname FROM project p, works_on w WHERE w.hours > 30 AND p.pnumber = w.pno", 
				Pattern.quote("Select the name of each project where the works on's hours is greater than 30 and the project's number is the works on's pno.")},
			
			// 3
			// For each employee whose first name is John, retrieve that employee's dependents' names.
			{"SELECT D.dependent_name FROM employee AS E, dependent AS D WHERE E.ssn = D.essn AND e.fname = 'John'", 
				Pattern.quote("Select the name of each dependent where the employee dependents of the dependent and the employee's first name is \"John.\"")},
			
			// 4
			{"SELECT d.mgr_start_date FROM department d WHERE d.dname = 'Research'", 
				Pattern.quote("Select the start date of the Research department.")},
			
			// 5
			{"SELECT e.fname, e.lname FROM employee e, department d", 
				Pattern.quote("Select the first name and last name of each employee.")},
			
			// 6
			// e works for the Research department, not e2.
			{"SELECT e.super_ssn FROM employee e, department d WHERE e.dno = d.dnumber AND d.dname = 'Research'", 
				Pattern.quote("Select the ssn of each employee _e2_ who supervises the employee _e_ and works for the Research department.")},
			
			// 7
			// First, this should say "supervises (a/some) employee". Second, in this situation we should perhaps just apply e2 and e's label from the ER (supervisor and supervisee) - or apply one of the two
			{"Select e.super_ssn FROM employee e", 
				Pattern.quote("Select the ssn of each employee _e2_ who supervises the employee _e_.")},
			
			// 8
			{"SELECT E.lname AS employee_name, S.lname AS supervisor_name FROM employee AS E JOIN employee AS S ON (E.super_ssn = S.ssn)", 
				Pattern.quote("Select the last names of each employee and their supervisor.")},
				
			// 9
			{"SELECT d.dependent_name, d.bdate FROM department d2, dependent d, dept_locations l WHERE l.dnumber = d2.dnumber AND d.essn = d2.mgr_ssn AND l.dlocation = 'Houston'", 
				Pattern.quote("List the name and birthdate of the dependents of each employee who manages a department located in \"Houston.\"")},
		
			// 10
			{"SELECT E.fname, M.fname, D.fname FROM employee E, employee M, employee D WHERE E.salary > M.salary AND E.dno = D.dno AND D.mgr_ssn = M.ssn", 
				Pattern.quote("")},
				
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
