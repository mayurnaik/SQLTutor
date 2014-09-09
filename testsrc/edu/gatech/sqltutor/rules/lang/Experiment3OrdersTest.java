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
@Ignore("Orders schema not yet encoded")
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
			{"SELECT cust# FROM customer WHERE cname = ‘John Smith’",
				null},
			//7
			{"SELECT cust#, city FROM customer WHERE cname = ‘John Smith’",
				null},
			//8
			{"SELECT * FROM customer WHERE cname = ‘John Smith’",
				null},
			//9
			{"SELECT * FROM order WHERE ord_amt > ‘2’ AND odate = ‘11-12-2013’",
				null},
			//10
			{"SELECT DISTINCT ord_amt FROM order WHERE cust# = ‘1122334455’",
				null},
			//11
			{"SELECT DISTINCT ord_amt, odate FROM order WHERE cust# = ‘1122334455’",
				null},
			//12
			{"SELECT DISTINCT * FROM order WHERE cust# = ‘1122334455’",
				null},
			//13
			{"SELECT city, ship_date FROM shipment, warehouse WHERE shipment.warehouse# = warehouse.warehouse#",
				null},
			//14
			{"SELECT city, ship_date FROM shipment, warehouse WHERE shipment.warehouse# = warehouse.warehouse# AND order# = ‘1122334455’",
				null},
		};
		return Arrays.asList(params);
	}

	public Experiment3OrdersTest(String query, String matches) {
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
