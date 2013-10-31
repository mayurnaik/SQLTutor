package edu.gatech.sqltutor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ManagedBean(name="databaseManager", eager=true)
@ApplicationScoped
public class DatabaseManager {
	@Resource(name="jdbc/sqltutorDB")
	private DataSource dataSource;
	
	@Resource(name="jdbc/sqltutorUserDB")
	private DataSource userDataSource;

	public DatabaseManager() {
	}
	
	@PostConstruct
	public void lookupDataSource() {
		// FIXME: @Resource injection not working for some reason
		try {
			Context context = new InitialContext();
			if( dataSource == null ) {
				System.err.println("DatabaseManager: Resource injection failed, performing lookup.");
				dataSource = (DataSource)context.lookup("java:comp/env/jdbc/sqltutorDB");
			}
			if( userDataSource == null ) {
				userDataSource = (DataSource)context.lookup("java:comp/env/jdbc/sqltutorUserDB");
			}
		} catch( NamingException e ) {
			e.printStackTrace();
		} catch( ClassCastException e ) {
			e.printStackTrace();
		}
	}

	public List<String> getSchemas() throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			
			ArrayList<String> schemas = new ArrayList<String>();
			
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet rs = meta.getSchemas();
			while( rs.next() ) {
				String schema = rs.getString(1);
				schemas.add(schema);
			}
			return schemas;
		} finally {
			Utils.tryClose(conn);
		}
	}
	
	public List<String> getUserSchemas() throws SQLException {
		Connection conn = null;
		try {
			conn = userDataSource.getConnection();
			
			ArrayList<String> schemas = new ArrayList<String>();
			
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet rs = meta.getSchemas();
			while( rs.next() ) {
				String schema = rs.getString(1);
				if( !("public".equals(schema) || 
						"information_schema".equals(schema) || 
						"pg_catalog".equals(schema)) ) {
					schemas.add(schema);
				}
			}
			return schemas;
		} finally {
			Utils.tryClose(conn);
		}
	}
	
	private static final String SET_SCHEMA_TEMPLATE = 
		"DROP SCHEMA IF EXISTS \"%1$s\" CASCADE;\n" +
		"CREATE SCHEMA \"%1$s\" AUTHORIZATION \"DB_Manager\";\n" + 
		"SET search_path = \"%1$s\";\n";
	
	public void addUserSchema(String schemaName, String schemaDump) 
			throws SQLException {
		if( schemaName == null || (schemaName = schemaName.trim()).length() == 0 )
			throw new IllegalArgumentException("No schema name provided.");
		
		// create the schema and force it to be default context
		schemaDump = schemaDump.replaceFirst("SET search_path = ([^;]+;)", 
			"$1\n " + String.format(SET_SCHEMA_TEMPLATE, schemaName));
		
		// replace references to public schema with the schema name
		schemaDump = schemaDump.replaceAll("TABLE public\\.", "TABLE \"" + schemaName + "\".");
		
		// TODO also need to fix GRANT statements
		
		Connection conn = null;
		try {
			conn = userDataSource.getConnection();
			
			// FIXME this does not handle data import like:
			// COPY works_on (employee_ssn, project_id, hours) FROM stdin;
			// 123456789       1       32.5
			// 123456789       2       7.5
			// \.
			Statement s = conn.createStatement();
			for( String line: schemaDump.split("\r\n|[\r\n]") ) {
				if( line.length() > 0 && !line.startsWith("--") )
					s.addBatch(line);
			}
			
			s.executeBatch();
		} finally {
			Utils.tryClose(conn);
		}
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public DataSource getUserDataSource() {
		return userDataSource;
	}
	
	public void setUserDataSource(DataSource userDataSource) {
		this.userDataSource = userDataSource;
	}
}

