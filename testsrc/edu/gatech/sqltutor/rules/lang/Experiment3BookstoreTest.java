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

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.gatech.sqltutor.TestConst;

@RunWith(Parameterized.class)
@Ignore("Bookstore schema not yet encoded")
public class Experiment3BookstoreTest extends SymbolicFragmentTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			//0
			{"SELECT title FROM book",
				null},
			//1
			{"SELECT title, publisher_name FROM book",
				null},
			//2
			{"SELECT * FROM book",
				null},
			//3
			{"SELECT DISTINCT title FROM book",
				null},
			//4
			{"SELECT DISTINCT title, publisher_name FROM book",
				null},
			//5
			{"SELECT DISTINCT * FROM book",
				null},
			//6
			{"SELECT title FROM book WHERE publisher_name = 'Stephen King'",
				null},
			//7
			{"SELECT title, publisher_name FROM book WHERE publisher_name = 'Stephen King'",
				null},
			//8
			{"SELECT * FROM book WHERE publisher_name = 'Stephen King'",
				null},
			//9
			{"SELECT * FROM book_loans WHERE date_out > '11-12-2012' AND publisher_name = 'Stephen King'",
				null},
			//10
			{"SELECT DISTINCT title FROM book WHERE publisher_name = 'Stephen King'",
				null},
			//11
			{"SELECT DISTINCT title, publisher_name FROM book WHERE publisher_name = 'Stephen King'",
				null},
			//12
			{"SELECT DISTINCT * FROM book WHERE publisher_name = 'Stephen King'",
				null},
			//13
			{"SELECT title, no_of_copies FROM book, book_copies WHERE book.book_id = book_copies.book_id",
				null},
			//14
			{"SELECT title, no_of_copies FROM book, book_copies WHERE book.book_id = book_copies.book_id AND publisher_name = 'Stephen King'",
				null},
		};
		return Arrays.asList(params);
	}

	public Experiment3BookstoreTest(String query, String matches) {
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
