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
public class Experiment3CompanyTest extends SymbolicFragmentTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			//0
			{"SELECT ssn FROM employee",
				null},
			//1
			{"SELECT ssn, salary FROM employee",
				null},
			//2
			{"SELECT * FROM employee",
				null},
			//3
			{"SELECT DISTINCT fname FROM employee",
				null},
			//4
			{"SELECT DISTINCT fname, salary FROM employee",
				null},
			//5
			{"SELECT DISTINCT * FROM employee",
				null},
			//6
			{"SELECT ssn FROM employee WHERE fname = 'Ahmad'",
				null},
			//7
			{"SELECT ssn, salary FROM employee WHERE fname = 'Ahmad'",
				null},
			//8
			{"SELECT * FROM employee WHERE fname = 'Ahmad'",
				null},
			//9
			{"SELECT * FROM employee WHERE salary > 20000 AND fname = 'Ahmad'",
				null},
			//10
			{"SELECT DISTINCT fname FROM employee WHERE fname = 'Ahmad'",
				null},
			//11
			{"SELECT DISTINCT fname, salary FROM employee WHERE fname = 'Ahmad'",
				null},
			//12
			{"SELECT DISTINCT * FROM employee WHERE fname = 'Ahmad'",
				null},
			//13
			{"SELECT e.salary, d.dname FROM employee e, department d WHERE d.dnumber = e.dno",
				null},
			//14
			{"SELECT e.salary, d.dname FROM employee e, department d WHERE d.dnumber = e.dno AND e.fname = 'Ahmad'",
				null},	
		};
		return Arrays.asList(params);
	}

	public Experiment3CompanyTest(String query, String matches) {
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
