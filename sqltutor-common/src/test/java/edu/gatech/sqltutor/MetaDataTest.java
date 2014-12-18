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
package edu.gatech.sqltutor;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetaDataTest {
	private static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String CONN_URL = "jdbc:derby:memory:testDB";
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		Class.forName(DB_DRIVER);
	}
	
	private Connection conn;
	
	@Before
	public void before() throws Exception {
		conn = DriverManager.getConnection(CONN_URL + ";create=true");
		Statement s = conn.createStatement();
		
		s.execute("CREATE TABLE t1 ( c1 INT NOT NULL, c2 VARCHAR(10), c3 VARCHAR(255) NOT NULL WITH DEFAULT 'foo')");
		s.execute("CREATE TABLE t2 ( c4 CHAR(5) NOT NULL PRIMARY KEY )");
	}
	
	@After
	public void after() throws Exception {
		Utils.tryClose(conn);
		conn = null;
	}
	
	@Test
	public void testReadTableInfo() throws Exception {
		List<DatabaseTable> tableInfos = QueryUtils.readTableInfo(conn.getMetaData(), null);
		
		assertEquals("Wrong number of tables.", 2, tableInfos.size());
		DatabaseTable t1 = tableInfos.get(0), t2 = tableInfos.get(1);
		ColumnInfo cinfo;
		
		// in case DB engine changes
		assertTrue("Remaining assertions expect identifiers to be stored uppercase",
				conn.getMetaData().storesUpperCaseIdentifiers());
		
		assertEquals("T1", t1.getTableName());
		List<ColumnInfo> cinfos = t1.getColumns();
		cinfo = cinfos.get(0);
		assertEquals("C1", cinfo.getName());
		assertEquals("t1.c1 should not be nullable", DatabaseMetaData.columnNoNulls, cinfo.getNullable());
		assertEquals(Types.INTEGER, cinfo.getType());
		assertEquals(1, cinfo.getPosition());
		
		cinfo = cinfos.get(1);
		assertEquals("C2", cinfo.getName());
		assertEquals("t1.c2 should be nullable", DatabaseMetaData.columnNullable, cinfo.getNullable());
		assertEquals(Types.VARCHAR, cinfo.getType());
		assertEquals(2, cinfo.getPosition());
		
		cinfo = cinfos.get(2);
		assertEquals("C3", cinfo.getName());
		assertEquals("t1.c3 should not be nullable", DatabaseMetaData.columnNoNulls, cinfo.getNullable());
		assertEquals(Types.VARCHAR, cinfo.getType());
		assertEquals("t1.c3 default should be 'foo'", "'foo'", cinfo.getDefaultValue());
		assertEquals(3, cinfo.getPosition());
		
		assertEquals("T2", t2.getTableName());
		cinfos = t2.getColumns();
		cinfo = cinfos.get(0);
		assertEquals("C4", cinfo.getName());
		assertEquals("t2.c4 should not be nullable", DatabaseMetaData.columnNoNulls, cinfo.getNullable());
		assertEquals(Types.CHAR, cinfo.getType());
		assertEquals(1, cinfo.getPosition());
	}
}
