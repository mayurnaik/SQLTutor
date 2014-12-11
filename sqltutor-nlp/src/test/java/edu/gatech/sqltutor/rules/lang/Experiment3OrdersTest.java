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
public class Experiment3OrdersTest extends SymbolicFragmentTestBase {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		Object[][] params = {
			//0
			{"SELECT cname FROM customer",
				null},
			//1
			{"SELECT cname, city FROM customer",
				null},
			//2
			{"SELECT * FROM customer",
				null},
			//3
			{"SELECT DISTINCT cname FROM customer",
				null},
			//4
			{"SELECT DISTINCT cname, city FROM customer",
				null},
			//5
			{"SELECT DISTINCT * FROM customer",
				null},
			//6
			{"SELECT cust_num FROM customer WHERE cname = 'John Smith'",
				null},
			//7
			{"SELECT cust_num, city FROM customer WHERE cname = 'John Smith'",
				null},
			//8
			{"SELECT * FROM customer WHERE cname = 'John Smith'",
				null},
			//9
			{"SELECT * FROM \"order\" WHERE ord_amt > '2' AND odate = '11-12-2013'",
				null},
			//10
			{"SELECT DISTINCT ord_amt FROM \"order\" WHERE cust_num = '1122334455'",
				null},
			//11
			{"SELECT DISTINCT ord_amt, odate FROM \"order\" WHERE cust_num = '1122334455'",
				null},
			//12
			{"SELECT DISTINCT * FROM \"order\" WHERE cust_num = '1122334455'",
				null},
			//13
			{"SELECT w.city, s.ship_date FROM shipment s, warehouse w WHERE s.warehouse_num = w.warehouse_num",
				null},
			//14
			{"SELECT w.city, s.ship_date FROM shipment s, warehouse w WHERE s.warehouse_num = w.warehouse_num AND s.order_num = '1122334455'",
				null},
		};
		return Arrays.asList(params);
	}

	public Experiment3OrdersTest(String query, String matches) {
		super(query, matches);
	}

	@Override
	protected String getERDiagramResource() {
		return TestConst.Resources.ORDERS_DIAGRAM;
	}

	@Override
	protected String getERMappingResource() {
		return TestConst.Resources.ORDERS_MAPPING;
	}

}
