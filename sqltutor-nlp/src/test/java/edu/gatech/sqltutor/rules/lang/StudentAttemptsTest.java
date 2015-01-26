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
public class StudentAttemptsTest extends SymbolicFragmentTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			// 0
			// Select the first name, middle initial, and last name of each employee whose salary is greater than 10000 and less than 30000.
			{"select fname, minit, lname from employee where salary > 10000 and salary < 30000", 
				Pattern.quote("Select the name of each employee whose salary is greater than $10,000 and less than $30,000.")},
			
			// 1
			// Select the SSN of each supervisor.
			{"select distinct super_ssn from employee", 
				Pattern.quote("Select the SSN of each supervisor.")},
			
			// 3
			// Select the relationship of each dependent.
			{"select relationship from dependent", 
				Pattern.quote("Select the relationship of each dependent.")},
			
			// 4
			{"select mgr_ssn from department", 
				Pattern.quote("Select the ssn of each employee who manages a department.")},
			
			// 5
			{"select mgr_ssn from department where dname = 'research'", 
				Pattern.quote("Select the ssn of each employee who manages the \"research\" department.")},
				
			// 6
			{"select fname, lname from employee where salary > 10000 and salary < 30000", 
				Pattern.quote("Retrieve the first name and last name of each employee who earns greater than $10,000 and less than $30,000.")},
				
			// 7 
			{"select e.fname, s.fname from employee as e, employee as s where e.salary > 20000", 
				Pattern.quote("Select the first names of each employee _e_ and employee _e2_ where _e_'s salary is greater than $20,000.")},
				
			// 8
			{"select fname, minit, lname, salary from employee where salary > 10000 and salary <= 30000",
						Pattern.quote("Retrieve the first name and last name of each employee who earns greater than $10,000 and less than or equal to $30,000.")}
				
		};
		return Arrays.asList(params);
	}

	public StudentAttemptsTest(String query, String matches) {
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
