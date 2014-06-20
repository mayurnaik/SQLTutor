package edu.gatech.sqltutor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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

import utilities.PasswordHasher;
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
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			
			ArrayList<String> schemas = new ArrayList<String>();
			
			DatabaseMetaData meta = conn.getMetaData();
			rs = meta.getSchemas();
			while( rs.next() ) {
				String schema = rs.getString(1);
				schemas.add(schema);
			}
			return schemas;
		} finally {
			Utils.tryClose(rs);
			Utils.tryClose(conn);
		}
	}
	
	public List<String> getUserSchemas(boolean dev) throws SQLException {
		Connection conn = null;
		Connection optionsConn = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			ArrayList<String> schemas = new ArrayList<String>();
			String query = dev ? "SELECT schema FROM schema_options;" : 
				"SELECT schema FROM schema_options WHERE visible_to_users;";
			
			optionsConn = dataSource.getConnection();
			statement = optionsConn.createStatement();
			statement.execute(query);
			resultSet = statement.getResultSet();
			
			while(resultSet.next()) {
				schemas.add(resultSet.getString(1));
			}

			return schemas;
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(resultSet);
			Utils.tryClose(conn);
		}
	}
	
	public HashMap<String, Boolean> getOptions(String schemaName) throws SQLException {
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			
			statement = conn.createStatement();
			boolean hasResults = statement.execute("SELECT visible_to_users, in_order_questions FROM schema_options WHERE schema = '" + schemaName +"'");
			rs = statement.getResultSet();
			
			if(!hasResults)
				return null;

			HashMap<String, Boolean> options = new HashMap<String, Boolean>();
			rs.next();
			
			options.put("visible_to_users", rs.getBoolean(1));
			options.put("in_order_questions", rs.getBoolean(2));

			return options;
		} finally {
			Utils.tryClose(rs);
			Utils.tryClose(statement);
			Utils.tryClose(conn);
		}
	}
	
	public void deleteSchema(String schemaName) throws SQLException {
		Connection schemaConnection = null;
		Connection optionsConnection = null;
		Statement statement = null;
		try {
			schemaConnection = userDataSource.getConnection();
			statement = schemaConnection.createStatement();
			statement.execute("DROP SCHEMA " + schemaName +" CASCADE;");
			Utils.tryClose(statement);
			
			optionsConnection = dataSource.getConnection();
			statement = optionsConnection.createStatement();
			statement.addBatch("DELETE FROM schema_options WHERE schema = '" + schemaName + "';");
			statement.addBatch("DELETE FROM schema_questions WHERE schema = '" + schemaName + "';");
			statement.executeBatch();

		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(schemaConnection);
			Utils.tryClose(optionsConnection);
		}
	}
	
	public boolean checkSchemaPermissions(String email, String schemaName) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = dataSource.getConnection();
			final String query = "SELECT 1 FROM schema_options WHERE schema = ? AND owner = ?;";
			
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, schemaName);
			preparedStatement.setString(2, email);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				return true;
			}
			
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(resultSet);
			Utils.tryClose(connection);
		}
		
		return false;
	}
	
	public void addQuestion(String schemaName, String question, String answer) throws SQLException {
		Connection conn = null;
		Statement statement = null;
		PreparedStatement ps = null;
		try {
			conn = dataSource.getConnection();
			
			statement = conn.createStatement();
			statement.execute("SELECT max(\"order\") FROM schema_questions");
			ResultSet rs = statement.getResultSet();
			int order = 1;
			if(rs.next())
				order = rs.getInt(1) + 1;

			ps = conn.prepareStatement(
					"INSERT INTO schema_questions (schema, question, answer, \"order\") "
					+ "VALUES (?, ?, ?, ?);");
			ps.setString(1, schemaName);
			ps.setString(2, question);
			ps.setString(3, answer);
			ps.setInt(4, order);
			ps.executeUpdate();
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(ps);
			Utils.tryClose(conn);
		}
	}
	
	public List<QuestionTuple> getQuestions(String schemaName) throws SQLException {
		Connection conn = null;
		Statement statement = null;
		try {
			conn = dataSource.getConnection();
			
			statement = conn.createStatement();
			statement.execute("SELECT \"order\", question, answer, id FROM "
					+ "schema_questions WHERE schema = '" + schemaName + 
					"' ORDER BY \"order\";");
			ResultSet rs = statement.getResultSet();
			List<QuestionTuple> questions = new ArrayList<QuestionTuple>();
			while(rs.next()) {
				questions.add(new QuestionTuple(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)));
			}

			return questions;
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(conn);
		}
	}
	
	public void setOptions(String schemaName, boolean visibleToUsers, boolean inOrderQuestions) throws SQLException {
		Connection conn = null;
		Statement statement = null;
		try {
			conn = dataSource.getConnection();
			
			statement = conn.createStatement();
			statement.execute("UPDATE schema_options SET visible_to_users = " + visibleToUsers + ", in_order_questions = " + inOrderQuestions + " WHERE schema = '" + schemaName + "';");

		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(conn);
		}
	}
	
	public String addSchema(String schemaDump, String email) 
			throws SQLException, IOException, IllegalArgumentException {
		
		if(schemaDump == null || schemaDump.length() == 0) {
			throw new IllegalArgumentException("Schema file is null or empty.");
		}
		
		Connection schemaConnection = null;
		Connection optionsConnection = null;
		PreparedStatement preparedStatement = null;
		try {
			schemaConnection = userDataSource.getConnection();
			ScriptRunner runner = new ScriptRunner(schemaConnection, false, true);
			BufferedReader reader = new BufferedReader(new StringReader(schemaDump));
			runner.runScript(reader);
			reader.close();

			// get schema name
			Pattern p = Pattern.compile("(?<=CREATE SCHEMA\\W{1,2})(\\w+)");
			Matcher m = p.matcher(schemaDump);
			m.find();
			String schemaName = m.group(1);
			
			preparedStatement = schemaConnection.prepareStatement("GRANT SELECT ON ALL TABLES IN SCHEMA " + schemaName + " TO readonly_user;");
			preparedStatement.execute();
			preparedStatement.close();
			
			preparedStatement = schemaConnection.prepareStatement("GRANT USAGE ON SCHEMA " + schemaName + " TO readonly_user;");
			preparedStatement.execute();
			preparedStatement.close();
			
			optionsConnection = dataSource.getConnection();
			preparedStatement = optionsConnection.prepareStatement("INSERT INTO schema_options (schema, owner) VALUES ('"+schemaName+"', '"+email+"');");
			preparedStatement.execute();
			preparedStatement.close();
			
			return schemaName;
		} finally {
			Utils.tryClose(preparedStatement);
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
		Statement statement = null;
		try {
			conn = dataSource.getConnection();
			
			statement = conn.createStatement();

			for(int i = 0; i < questions.size(); i++) {
				int id = questions.get(i).getId();
				statement.addBatch("UPDATE schema_questions SET \"order\" = " 
						+ (i+1) + " WHERE id = " + id + ";");
			}
			statement.executeBatch();

		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(conn);
		}
	}
	
	public void deleteQuestions(List<QuestionTuple> questions) throws SQLException {
		
		Connection conn = null;
		Statement statement = null;
		try {
			conn = dataSource.getConnection();
			
			statement = conn.createStatement();

			for(int i = 0; i < questions.size(); i++) {
				int id = questions.get(i).getId();
				statement.addBatch("DELETE FROM schema_questions WHERE id = " + id + ";");
			}
			statement.executeBatch();

		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(conn);
		}
	}
	
	
	public void addPasswordChangeRequest(String email, UUID uuid) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = dataSource.getConnection();
			final String update = "INSERT INTO \"password_change_requests\" (\"email\", \"id\", \"salt\") VALUES (?, ?, ?)";
			
			preparedStatement = connection.prepareStatement(update);
			
			// generate the user's encryption salt and password
			byte[] salt = PasswordHasher.generateSalt();
			byte[] encryptedId = PasswordHasher.getEncryptedPassword(uuid.toString(), salt);
			
			preparedStatement.setString(1, email);
			preparedStatement.setBytes(2, encryptedId);
			preparedStatement.setBytes(3, salt);
			
			preparedStatement.executeUpdate();

		} catch (Exception e) {
			
			System.err.println("Exception: " + e.getMessage());
			
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public boolean getPasswordChangeRequest(String email, String id) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
		
			// Get the salt and id of the most recent request
			// that belongs to the user within the last 24 hours
			String query = "SELECT \"salt\", \"id\" FROM \"password_change_requests\" WHERE "
					+ "\"time\" = (SELECT MAX(\"time\") FROM \"password_change_requests\" WHERE \"email\" = ? AND "
					+ "\"time\" >= (now() - '1 day'::INTERVAL));";
			
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next()) {
				byte[] salt = resultSet.getBytes(1);
				byte[] encryptedId = resultSet.getBytes(2);
				
				// use the password hasher to authenticate
				return PasswordHasher.authenticate(id, encryptedId, salt);
			} 
			 
		} catch (Exception e) {
			
			System.err.println("Exception: " + e.getMessage());
			
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
			Utils.tryClose(resultSet);
		}
		return false;
	}

	public void changePassword(String email, String newPassword) throws SQLException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = dataSource.getConnection();
			
			// generate the user's encryption salt and password
			byte[] salt = PasswordHasher.generateSalt();
			byte[] encryptedPassword = null;
			try {
				encryptedPassword = PasswordHasher.getEncryptedPassword(newPassword, salt);
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
			
			statement = conn.prepareStatement("UPDATE \"user\" SET \"password\" = ?, \"salt\" = ? WHERE email = ?;");
			statement.setBytes(1, encryptedPassword);
			statement.setBytes(2, salt);
			statement.setString(3, email);
			statement.execute();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(conn);
		}
	}

	public boolean emailExists(String email) throws SQLException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			
			statement = conn.prepareStatement("SELECT 1 FROM \"user\" WHERE email = ?;");
			statement.setString(1, email);
			statement.execute();
			rs = statement.getResultSet();

			if(rs.next()) {
				return true;
			}
			return false;
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(rs);
			Utils.tryClose(conn);
		}
	}
}

