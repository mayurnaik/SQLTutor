/*
 *   Copyright (c) 2015 Program Analysis Group, Georgia Tech
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SQLParserFeature;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.SQLParserContext.IdentifierCase;

import edu.gatech.sqltutor.JDBCConstants.ColumnPositions.GetSchemas;
import edu.gatech.sqltutor.rules.util.ColumnReferenceResolver;
import edu.gatech.sqltutor.sql.SchemaInfo;
import edu.gatech.sqltutor.util.ScriptRunner;

public class CompanyParseTest {
	private static final String CONN_URL = TestConst.CONNECTION_URL + ";MODE=PostgreSQL";
	
	private static final Logger _log = LoggerFactory.getLogger(CompanyParseTest.class);
	
	private static Connection conn;
	private static SchemaInfo schemaInfo;
	private static String schemaName = "company";
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		Class.forName(TestConst.DRIVER_CLASS);
		conn = DriverManager.getConnection(CONN_URL);
		
		// load the schema to memory
		try (InputStream is = CompanyParseTest.class.getResourceAsStream("/company.schema.sql");
				InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			ScriptRunner runner = new ScriptRunner(conn, true, true);
			runner.setLogWriter(null);
			runner.runScript(reader);
		}
		
		DatabaseMetaData meta = conn.getMetaData();
		if( meta.storesUpperCaseIdentifiers() )
			schemaName = schemaName.toUpperCase(Locale.US);
		
		try (ResultSet rs = meta.getSchemas()) {
			while( rs.next() ) {
				String schema = rs.getString(GetSchemas.TABLE_SCHEM);
				String catalog = rs.getString(GetSchemas.TABLE_CAT);
				System.out.println("Schema: " + schema + " [cat=" + catalog + "]");
			}
		}
		
		// read the metadata
		schemaInfo = QueryUtils.readSchemaInfo(meta, schemaName);
		_log.info("schemaInfo: {}", schemaInfo);
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		Utils.tryClose(conn);
		conn = null;
		schemaInfo = null;
	}
	
	private SQLParser newParser() throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		final IdentifierCase idCase;
		if( meta.storesLowerCaseIdentifiers() )
			idCase = IdentifierCase.LOWER;
		else if( meta.storesUpperCaseIdentifiers() )
			idCase = IdentifierCase.UPPER;
		else
			idCase = IdentifierCase.PRESERVE;
		
		return new SQLParser() {
			@Override
			public IdentifierCase getIdentifierCase() {
//				if(true)throw new RuntimeException("culprit");
				return idCase;
			}
		};
	}
	
	@Test
	public void testBasicQuery() throws Exception {
		String sql = "SELECT dname FROM department d0";
		
		SQLParser parser = new SQLParser();
		StatementNode statement = parser.parseStatement(sql);
		statement.treePrint();
	}	
	
	@Test
	public void testBasicQuery2() throws Exception {
		String sql = "SELECT d0.dname FROM department d0";
		
		SQLParser parser = new SQLParser();
		StatementNode statement = parser.parseStatement(sql);
		statement.treePrint();
	}
	
	@Test
	public void testBasicQuery3() throws Exception {
		String sql = "SELECT dname FROM \"department\" d0";
		
		SQLParser parser = new SQLParser();
		StatementNode statement = parser.parseStatement(sql);
		statement.treePrint();
	}
	
	@Test
	public void testBasicQuery4() throws Exception {
		String sql = "SELECT \"dname\" FROM department";
		
		SQLParser parser = newParser();
		StatementNode statement = parser.parseStatement(sql);
		statement.treePrint();
	}
	
	@Test
	public void testColumnReferenceResolution() throws Exception {
		String sql = "SELECT dname FROM department d0";
		
		SQLParser parser = newParser();
		StatementNode statement = parser.parseStatement(sql);
		
		final ColumnReferenceResolver resolver = new ColumnReferenceResolver(schemaInfo.getTables());
		final SelectNode select = QueryUtils.extractSelectNode(statement);
		resolver.resolve(select);
		
		System.out.println("Resolved: " + QueryUtils.nodeToString(statement));
		statement.treePrint();
	}
}
