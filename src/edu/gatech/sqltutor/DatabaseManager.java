package edu.gatech.sqltutor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import objects.QuestionTuple;

import java.sql.PreparedStatement;

import utilities.ScriptRunner;

@ManagedBean(name="databaseManager", eager=true)
@ApplicationScoped
public class DatabaseManager implements Serializable {
	private static final long serialVersionUID = 1L;
	
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
	
	public List<String> getUserSchemas(boolean dev) throws SQLException {
		Connection conn = null;
		Connection optionsConn = null;
		try {
			optionsConn = dataSource.getConnection();
			Statement statement = optionsConn.createStatement();
			statement.execute("SELECT schema, visible_to_users FROM schema_options");
			ResultSet resultSet = statement.getResultSet();
			HashMap<String, Boolean> visibleSchemaOption = new HashMap<String, Boolean>();
			while(resultSet.next()) {
				visibleSchemaOption.put(resultSet.getString(1), resultSet.getBoolean(2));
			}
			statement.close();
			
			conn = userDataSource.getConnection();
			
			ArrayList<String> schemas = new ArrayList<String>();
			
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet rs = meta.getSchemas();
			while( rs.next() ) {
				String schema = rs.getString(1);
				if( !("public".equals(schema) || 
						"information_schema".equals(schema) || 
						"pg_catalog".equals(schema)) ) {
					if(dev || visibleSchemaOption.get(schema)) {
						schemas.add(schema);
					}
				}
			}
			return schemas;
		} finally {
			Utils.tryClose(conn);
		}
	}
	
	public HashMap<String, Boolean> getOptions(String schemaName) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			
			Statement statement = conn.createStatement();
			boolean hasResults = statement.execute("SELECT visible_to_users, in_order_questions FROM schema_options WHERE schema = '" + schemaName +"'");
			ResultSet rs = null;
			rs = statement.getResultSet();
			
			if(!hasResults)
				return null;

			HashMap<String, Boolean> options = new HashMap<String, Boolean>();
			rs.next();
			
			options.put("visible_to_users", rs.getBoolean(1));
			options.put("in_order_questions", rs.getBoolean(2));
			
			statement.close();
			return options;
		} finally {
			Utils.tryClose(conn);
		}
	}
	
	public void deleteSchema(String schemaName) throws SQLException {
		Connection schemaConnection = null;
		Connection optionsConnection = null;
		try {
			schemaConnection = userDataSource.getConnection();
			Statement statement = schemaConnection.createStatement();
			statement.execute("DROP SCHEMA " + schemaName +" CASCADE;");
			statement.close();
			
			optionsConnection = dataSource.getConnection();
			statement = optionsConnection.createStatement();
			statement.addBatch("DELETE FROM schema_options WHERE schema = '" + schemaName + "';");
			statement.addBatch("DELETE FROM schema_questions WHERE schema = '" + schemaName + "';");
			statement.executeBatch();
			statement.close();
		} finally {
			Utils.tryClose(schemaConnection);
			Utils.tryClose(optionsConnection);
		}
	}
	
	public void addQuestion(String schemaName, String question, String answer) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			
			Statement statement = conn.createStatement();
			statement.execute("SELECT max(\"order\") FROM schema_questions");
			ResultSet rs = statement.getResultSet();
			int order = 1;
			if(rs.next())
				order = rs.getInt(1) + 1;
			statement.close();
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO schema_questions (schema, question, answer, \"order\") "
					+ "VALUES (?, ?, ?, ?);");
			ps.setString(1, schemaName);
			ps.setString(2, question);
			ps.setString(3, answer);
			ps.setInt(4, order);
			ps.executeUpdate();
			ps.close();
		} finally {
			Utils.tryClose(conn);
		}
	}
	
	public List<QuestionTuple> getQuestions(String schemaName) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			
			Statement statement = conn.createStatement();
			statement.execute("SELECT \"order\", question, answer, id FROM "
					+ "schema_questions WHERE schema = '" + schemaName + 
					"' ORDER BY \"order\";");
			ResultSet rs = statement.getResultSet();
			List<QuestionTuple> questions = new ArrayList<QuestionTuple>();
			while(rs.next()) {
				questions.add(new QuestionTuple(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)));
			}
			statement.close();
			return questions;
		} finally {
			Utils.tryClose(conn);
		}
	}
	
	public void setOptions(String schemaName, boolean visibleToUsers, boolean inOrderQuestions) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			
			Statement statement = conn.createStatement();
			statement.execute("UPDATE schema_options SET visible_to_users = " + visibleToUsers + ", in_order_questions = " + inOrderQuestions + " WHERE schema = '" + schemaName + "';");
			statement.close();
		} finally {
			Utils.tryClose(conn);
		}
	}
	
	public String addSchema(String schemaDump) 
			throws SQLException, IOException, IllegalArgumentException {
		
		if(schemaDump == null || schemaDump.length() == 0) {
			throw new IllegalArgumentException("Schema file is null or empty.");
		}
		
		Connection schemaConnection = null;
		Connection optionsConnection = null;
		try {
			schemaConnection = userDataSource.getConnection();
			ScriptRunner runner = new ScriptRunner(schemaConnection, false, true);
			BufferedReader reader = new BufferedReader(new StringReader(schemaDump));
			runner.runScript(reader);
			reader.close();
			Statement statement = schemaConnection.createStatement();

			Pattern p = Pattern.compile("(?<=CREATE SCHEMA\\W{1,2})(\\w+)");
			Matcher m = p.matcher(schemaDump);
			m.find();
			String schemaName = m.group(1);
			
			statement.addBatch("GRANT SELECT ON ALL TABLES IN SCHEMA " + schemaName + " TO readonly_user;");
			statement.addBatch("GRANT USAGE ON SCHEMA " + schemaName + " TO readonly_user;");
			statement.executeBatch();
			statement.close();
			
			optionsConnection = dataSource.getConnection();
			statement = optionsConnection.createStatement();
			statement.execute("INSERT INTO schema_options (schema) VALUES ('" + schemaName + "')");
			statement.close();
			
			return schemaName;
		} finally {
			Utils.tryClose(schemaConnection);
			Utils.tryClose(optionsConnection);
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

	public void reorderQuestions(List<QuestionTuple> questions) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			
			Statement statement = conn.createStatement();

			for(int i = 0; i < questions.size(); i++) {
				int id = questions.get(i).getId();
				statement.addBatch("UPDATE schema_questions SET \"order\" = " 
						+ (i+1) + " WHERE id = " + id + ";");
			}
			statement.executeBatch();
			
			statement.close();
		} finally {
			Utils.tryClose(conn);
		}
	}
	
	public void deleteQuestions(List<QuestionTuple> questions) throws SQLException {
		
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			
			Statement statement = conn.createStatement();

			for(int i = 0; i < questions.size(); i++) {
				int id = questions.get(i).getId();
				statement.addBatch("DELETE FROM schema_questions WHERE id = " + id + ";");
			}
			statement.executeBatch();
			
			statement.close();
		} finally {
			Utils.tryClose(conn);
		}
	}
}

