package edu.gatech.sqltutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.google.common.io.BaseEncoding;

import edu.gatech.sqltutor.entities.UserQuery;
import edu.gatech.sqltutor.util.SaltHasher;
import edu.gatech.sqltutor.util.ScriptRunner;

@ManagedBean(name="databaseManager", eager=true)
@ApplicationScoped
public class DatabaseManager implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Resource(name="jdbc/sqltutorDB")
	private DataSource dataSource;
	
	@Resource(name="jdbc/sqltutorUserDB")
	private DataSource userDataSource;

	@Resource(name="jdbc/sqltutorUserDBRead")
	private DataSource readUserDataSource;
	
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
			if ( readUserDataSource == null ) { 
				readUserDataSource = (DataSource)context.lookup("java:comp/env/jdbc/sqltutorUserDBRead");
			}
		} catch( NamingException e ) {
			e.printStackTrace();
		} catch( ClassCastException e ) {
			e.printStackTrace();
		}
	}

	public List<String> getSchemas() throws SQLException {
		Connection connection = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
	
			ArrayList<String> schemas = new ArrayList<String>();
			
			DatabaseMetaData meta = connection.getMetaData();
			resultSet = meta.getSchemas();
			while( resultSet.next() ) {
				String schema = resultSet.getString(1);
				schemas.add(schema);
			}
			
			return schemas;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(connection);
		}
	}
	
	public boolean isAdmin(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = dataSource.getConnection();
			final String query = "SELECT admin FROM \"user\" WHERE email = ?;";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			resultSet = preparedStatement.executeQuery();
			
			boolean isAdmin = false;
			if (resultSet.next()) {
				isAdmin = resultSet.getBoolean(1);
			}
			return isAdmin;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public List<String> getDevSchemas(boolean dev) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			ArrayList<String> schemas = new ArrayList<String>();
			if(dev) {
				connection = dataSource.getConnection();
		
				String query = "SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE';";
				
				statement = connection.createStatement();
				statement.execute(query);
				
				resultSet = statement.getResultSet();
				
				while(resultSet.next()) {
					schemas.add(resultSet.getString(1));
				}
			} 
			return schemas;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public List<String> getUserSchemas(boolean admin) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
	
		try { 
			connection = dataSource.getConnection();
			
			String query = admin ? "SELECT schema FROM schema_options;" : 
				"SELECT schema FROM schema_options WHERE visible_to_users;";
			
			statement = connection.createStatement();
			statement.execute(query);
			
			resultSet = statement.getResultSet();
			
	
			ArrayList<String> schemas = new ArrayList<String>();
			while(resultSet.next()) {
				schemas.add(resultSet.getString(1));
			}
			return schemas;
		} finally {	
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public HashMap<String, Boolean> getOptions(String schemaName) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = dataSource.getConnection();
			
			statement = connection.createStatement();
			statement.execute("SELECT visible_to_users, in_order_questions FROM schema_options WHERE schema = '" + schemaName +"'");
			
			resultSet = statement.getResultSet();
			resultSet.next();
			
			HashMap<String, Boolean> options = new HashMap<String, Boolean>();
			options.put("visible_to_users", resultSet.getBoolean(1));
			options.put("in_order_questions", resultSet.getBoolean(2));
			return options;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public void deleteSchema(String schemaName) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		
		try {
			connection = userDataSource.getConnection();
			
			statement = connection.createStatement();
			statement.execute("DROP SCHEMA " + schemaName +" CASCADE;");
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
		
		try {
			connection = dataSource.getConnection();
			
			statement = connection.createStatement();
			statement.addBatch("DELETE FROM schema_options WHERE schema = '" + schemaName + "';");
			statement.addBatch("DELETE FROM schema_questions WHERE schema = '" + schemaName + "';");
			statement.executeBatch();
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(connection);
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
			
			boolean schemaPermissions = false;
			if (resultSet.next()) {
				schemaPermissions = true;
			}
			return schemaPermissions;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}	
	}
	
	public void addQuestion(String schemaName, String question, String answer) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		PreparedStatement preparedStatement = null;
		
		int order = 1;
		
		try {
			connection = dataSource.getConnection();
		
			statement = connection.createStatement();
			statement.execute("SELECT max(\"order\") FROM schema_questions");
		
			resultSet = statement.getResultSet();
			if(resultSet.next())
				order = resultSet.getInt(1) + 1;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}

		try {
			connection = dataSource.getConnection(); 
			preparedStatement = connection.prepareStatement(
				"INSERT INTO schema_questions (schema, question, answer, \"order\") "
				+ "VALUES (?, ?, ?, ?);");
			preparedStatement.setString(1, schemaName);
			preparedStatement.setString(2, question);
			preparedStatement.setString(3, answer);
			preparedStatement.setInt(4, order);
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public List<UserTuple> getUserTuples() throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try { 
			connection = dataSource.getConnection();
			
			statement = connection.createStatement();
			statement.execute("SELECT \"email\", \"admin\", \"admin_code\", \"developer\" FROM \"user\"");
			
			resultSet = statement.getResultSet();
			List<UserTuple> users = new ArrayList<UserTuple>();
			while(resultSet.next()) {
				users.add(new UserTuple(resultSet.getString(1), resultSet.getBoolean(2), resultSet.getString(3), resultSet.getBoolean(4)));
			}
			return users;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public UserTuple getUserTuple(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try { 
			connection = dataSource.getConnection();
		
			preparedStatement = connection.prepareStatement("SELECT \"admin\", \"admin_code\", \"developer\" FROM \"user\" WHERE email = ?");
			preparedStatement.setString(1, email);
			preparedStatement.execute();
			
			resultSet = preparedStatement.getResultSet();
			UserTuple user = new UserTuple();
			while(resultSet.next()) { 
				user = new UserTuple(email, resultSet.getBoolean(1), resultSet.getString(2), resultSet.getBoolean(3));
			}
			return user;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}	
	}
	
	public List<QuestionTuple> getQuestions(String schemaName) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
		
			statement = connection.createStatement();
			statement.execute("SELECT \"order\", question, answer, id FROM "
				+ "schema_questions WHERE schema = '" + schemaName + 
				"' ORDER BY \"order\";");
		
			resultSet = statement.getResultSet();
			List<QuestionTuple> questions = new ArrayList<QuestionTuple>();
			while(resultSet.next()) {
				questions.add(new QuestionTuple(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getInt(4)));
			}
			return questions;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public void setOptions(String schemaName, boolean visibleToUsers, boolean inOrderQuestions) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		
		try {
			connection = dataSource.getConnection();
		
			statement = connection.createStatement();
			statement.execute("UPDATE schema_options SET visible_to_users = " + visibleToUsers + ", in_order_questions = " + inOrderQuestions + " WHERE schema = '" + schemaName + "';");
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public String addSchema(String schemaDump, String email) 
			throws SQLException, IllegalArgumentException, IOException {
		
		if(schemaDump == null || schemaDump.length() == 0) {
			throw new IllegalArgumentException("Schema file is null or empty.");
		}
		
		Connection connection = null;
		BufferedReader reader = null;
		try { 
			connection = userDataSource.getConnection();
			
			ScriptRunner runner = new ScriptRunner(connection, false, true);
			reader = new BufferedReader(new StringReader(schemaDump));
			runner.runScript(reader);
		} catch(SQLException e) {
			throw e;
		} finally {
			Utils.tryClose(reader);
			Utils.tryClose(connection);
		}

		// get schema name
		Pattern p = Pattern.compile("(?<=CREATE SCHEMA\\W{1,2})(\\w+)");
		Matcher m = p.matcher(schemaDump);
		m.find();
		String schemaName = m.group(1);
		
		Statement statement = null;
		
		try {
			connection = userDataSource.getConnection();
			statement = connection.createStatement();
			statement.addBatch("GRANT SELECT ON ALL TABLES IN SCHEMA " + schemaName + " TO readonly_user;");
			statement.addBatch("GRANT USAGE ON SCHEMA " + schemaName + " TO readonly_user;");
			statement.executeBatch();
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
		
		try { 
			connection = dataSource.getConnection();
			statement = connection.createStatement();
			statement.execute("INSERT INTO schema_options (schema, owner) VALUES ('"+schemaName+"', '"+email+"');");
			
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}

		return schemaName;
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
		Connection connection = null;
		Statement statement = null;
		
		try { 
			connection = dataSource.getConnection();
		
			statement = connection.createStatement();
			for(int i = 0; i < questions.size(); i++) {
				int id = questions.get(i).getId();
				statement.addBatch("UPDATE schema_questions SET \"order\" = " 
						+ (i+1) + " WHERE id = " + id + ";");
			}
			statement.executeBatch();
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public void deleteQuestions(List<QuestionTuple> questions) throws SQLException {
		Connection connection = null;
		Statement statement = null;

		try {
			connection = dataSource.getConnection();

			statement = connection.createStatement();
			for(int i = 0; i < questions.size(); i++) {
				int id = questions.get(i).getId();
				statement.addBatch("DELETE FROM schema_questions WHERE id = " + id + ";");
			}
			statement.executeBatch();
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	
	public void addPasswordChangeRequest(String email, UUID uuid) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			connection = dataSource.getConnection();
			
			// generate the user's encryption salt and password
			byte[] salt = SaltHasher.generateSalt();
			byte[] encryptedId = SaltHasher.getEncryptedValue(uuid.toString(), salt);
			
			final String update = "INSERT INTO \"password_change_requests\" (\"email\", \"id\", \"salt\") VALUES (?, ?, ?)";
			preparedStatement = connection.prepareStatement(update);
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, Arrays.toString(encryptedId));
			preparedStatement.setString(3, Arrays.toString(salt));
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public boolean getPasswordChangeRequest(String email, String id) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try { 
			connection = dataSource.getConnection();
	
			// Get the salt and id of the most recent request
			// that belongs to the user within the last 24 hours
			final String query = "SELECT \"salt\", \"id\" FROM \"password_change_requests\" WHERE "
					+ "\"time\" = (SELECT MAX(\"time\") FROM \"password_change_requests\" WHERE \"email\" = ? AND "
					+ "\"time\" >= (now() - '1 day'::INTERVAL));";
			
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			resultSet = preparedStatement.executeQuery();
		
			boolean autheticated = false;
			while (resultSet.next()) {
				byte[] salt = stringByteArrayToByteArray(resultSet.getString(1));
				byte[] encryptedId = stringByteArrayToByteArray(resultSet.getString(2));
				
				// use the password hasher to authenticate
				autheticated = SaltHasher.authenticate(id, encryptedId, salt);
			} 
			return autheticated;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}

	public void changePassword(String email, String newPassword) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			connection = dataSource.getConnection();

			// generate the user's encryption salt and password
			byte[] salt = SaltHasher.generateSalt();
			byte[] encryptedPassword = SaltHasher.getEncryptedValue(newPassword, salt);
			
			preparedStatement = connection.prepareStatement("UPDATE \"user\" SET \"password\" = ?, \"salt\" = ? WHERE email = ?;");
			preparedStatement.setString(1, Arrays.toString(encryptedPassword));
			preparedStatement.setString(2, Arrays.toString(salt));
			preparedStatement.setString(3, email);
			preparedStatement.execute();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}

	public boolean emailExists(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
		
			preparedStatement = connection.prepareStatement("SELECT 1 FROM \"user\" WHERE email = ?;");
			preparedStatement.setString(1, email);
			preparedStatement.execute();
			
			boolean hasResults = false;
			resultSet = preparedStatement.getResultSet();
			if(resultSet.next()) {
				hasResults = true;
			}
			return hasResults;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public void saveUserQuery(UserQuery query) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
			
			Long id = query.getId();
			if( id == null ) {
				final String INSERT = "INSERT INTO query " + 
					"(email, schema, sql, user_description, source, created) VALUES (?, ?, ?, ?, ?, ?)";
				preparedStatement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
			} else {
				final String UPDATE = 
						"UPDATE query SET email=?, schema=?, sql=?, user_description=?, source=?, created=? WHERE id=?";
				preparedStatement = connection.prepareStatement(UPDATE);
				preparedStatement.setLong(7, id);
			}
			preparedStatement.setString(1, query.getEmail());
			preparedStatement.setString(2, query.getSchema());
			preparedStatement.setString(3, query.getQuery());
			preparedStatement.setString(4, query.getUserDescription());
			preparedStatement.setString(5, query.getSource());
			preparedStatement.setDate(6, new Date(query.getTime().getTime()));
	
			preparedStatement.executeUpdate();
			
			if( id == null ) {
				// get the generated id for inserts
				resultSet = preparedStatement.getGeneratedKeys();
				if( !resultSet.next() )
					throw new IllegalStateException("No id generated for query.");
				query.setId(resultSet.getLong(1));
			}
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public HashMap<String, QueryResult> getAllData(String schemaName, List<String> tables) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = userDataSource.getConnection();
			statement = connection.createStatement();
			statement.execute("set search_path to '" + schemaName + "'");
			
			HashMap<String, QueryResult> allData = new HashMap<String, QueryResult>();
			for(String tableName : tables) {
				resultSet = statement.executeQuery("SELECT * FROM \"" + tableName + "\";");
				ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
				int columnCount = resultSetMetaData.getColumnCount();
				ArrayList<List<String>> queryData = new ArrayList<List<String>>();
				ArrayList<String> columnNames = new ArrayList<String>();
				for(int i = 1; i <=  columnCount; i++) {
					columnNames.add(resultSetMetaData.getColumnName(i));
				}
				while(resultSet.next()) {
					ArrayList<String> rowData = new ArrayList<String>();
					for (int i = 1; i <= columnCount; i++) {
						rowData.add(resultSet.getString(i));
					}
					queryData.add(rowData);
				}
				// return the query result object
				allData.put(tableName, new QueryResult(columnNames, queryData));
			} 
			return allData;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
		
	}
	
	public HashMap<String, QueryResult> getAllDevData(List<String> tables) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
			statement = connection.createStatement();
			statement.execute("set search_path to public");
			
			HashMap<String, QueryResult> allData = new HashMap<String, QueryResult>();
			for(String tableName : tables) {
				resultSet = statement.executeQuery("SELECT * FROM \"" + tableName + "\";");
				ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
				int columnCount = resultSetMetaData.getColumnCount();
				ArrayList<List<String>> queryData = new ArrayList<List<String>>();
				ArrayList<String> columnNames = new ArrayList<String>();
				for(int i = 1; i <=  columnCount; i++) {
					columnNames.add(resultSetMetaData.getColumnName(i));
				}
				while(resultSet.next()) {
					ArrayList<String> rowData = new ArrayList<String>();
					for (int i = 1; i <= columnCount; i++) {
						rowData.add(resultSet.getString(i));
					}
					queryData.add(rowData);
				}
				// return the query result object
				allData.put(tableName, new QueryResult(columnNames, queryData));
			} 
			return allData;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public void verifyQuery(String schemaName, String query) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		
		try {
			connection = userDataSource.getConnection();
			
			statement = connection.createStatement();
			statement.execute("set search_path to '" + schemaName + "'");
			statement.executeQuery(query);
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public QueryResult getQueryResult(String schemaName, String query, boolean dev) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dev ? userDataSource.getConnection() : readUserDataSource.getConnection();
			
			statement = connection.createStatement();
			statement.execute("set search_path to '" + schemaName + "'");
			
			resultSet = statement.executeQuery(query);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		
			int columnCount = resultSetMetaData.getColumnCount();
			ArrayList<List<String>> queryData = new ArrayList<List<String>>();
			ArrayList<String> columnNames = new ArrayList<String>();
			for(int i = 1; i <=  columnCount; i++) {
				columnNames.add(resultSetMetaData.getColumnName(i));
			}
			while(resultSet.next()) {
				ArrayList<String> rowData = new ArrayList<String>();
				for (int i = 1; i <= columnCount; i++) {
					rowData.add(resultSet.getString(i));
				}
				queryData.add(rowData);
			}
			
			return new QueryResult(columnNames, queryData);
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(connection);
			Utils.tryClose(statement);
		}
	}
	
	public QueryResult getDevQueryResult(String query, boolean dev) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
		
			statement = connection.createStatement();
			statement.execute("set search_path to public");
		
			resultSet = statement.executeQuery(query);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		
			int columnCount = resultSetMetaData.getColumnCount();
			ArrayList<List<String>> queryData = new ArrayList<List<String>>();
			ArrayList<String> columnNames = new ArrayList<String>();
			for(int i = 1; i <=  columnCount; i++) {
				columnNames.add(resultSetMetaData.getColumnName(i));
			}
			while(resultSet.next()) {
				ArrayList<String> rowData = new ArrayList<String>();
				for (int i = 1; i <= columnCount; i++) {
					rowData.add(resultSet.getString(i));
				}
				queryData.add(rowData);
			}
		
			return new QueryResult(columnNames, queryData);
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(connection);
			Utils.tryClose(statement);
		}
	}
	
	public void registerUser(String email, String password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			connection = dataSource.getConnection();
	
			final String updateUserEntry = "INSERT INTO \"user\" (\"password\", \"salt\", \"email\") VALUES (?, ?, ?)";
			preparedStatement = connection.prepareStatement(updateUserEntry);
			
			// generate the user's encryption salt and password
			byte[] salt = SaltHasher.generateSalt();
			byte[] encryptedPassword = SaltHasher.getEncryptedValue(password, salt);
	
			preparedStatement.setString(1, Arrays.toString(encryptedPassword));
			preparedStatement.setString(2, Arrays.toString(salt));
			preparedStatement.setString(3, email);
			preparedStatement.executeUpdate();
			
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		
		try {
			connection = dataSource.getConnection();
			
			final String updateAdminCodeLinkEntry = "INSERT INTO linked_admin_codes (\"email\") VALUES (?)";
			preparedStatement = connection.prepareStatement(updateAdminCodeLinkEntry);
	
			preparedStatement.setString(1, email);
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public void deleteUser(String email, String adminCode) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			connection = dataSource.getConnection();
	
			final String delete = "DELETE FROM \"user\" WHERE email = ?";
			preparedStatement = connection.prepareStatement(delete);
			preparedStatement.setString(1, email);
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		
		try {
			connection = dataSource.getConnection();
			
			preparedStatement = connection.prepareStatement("DELETE FROM linked_admin_codes WHERE linked_admin_code = ?");
			preparedStatement.setString(1, adminCode);
			preparedStatement.execute();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public boolean isEmailRegistered(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
	
			final String query = "SELECT 1 FROM \"user\" WHERE \"email\" = ?";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			
			resultSet = preparedStatement.executeQuery();
			
			boolean hasResults = false;
			if (resultSet.next()) {
				hasResults = true;
			}
			return hasResults;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public boolean isPasswordCorrect(String email, String attemptedPassword) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();

			// get the user's encryption salt
			String query = "SELECT \"salt\", \"password\" FROM \"user\" WHERE \"email\" = ?";
		
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			
			resultSet = preparedStatement.executeQuery();
			
			boolean isCorrect = false;
			if (resultSet.next()) {	
				byte[] salt = stringByteArrayToByteArray(resultSet.getString(1));
				byte[] encryptedPassword = stringByteArrayToByteArray(resultSet.getString(2));
				
				// use the password hasher to authenticate
				isCorrect = SaltHasher.authenticate(attemptedPassword, encryptedPassword, salt);
			} 
			return isCorrect;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public void log(String sessionId, String email, String schemaName, String question, String correctAnswer, String userQuery, boolean parsed, boolean correct) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try { 
			connection = dataSource.getConnection();
			
			final String update = "INSERT INTO \"log\" (\"session_id\", \"email\", \"schema\", \"question\", \"correct_answer\", \"query\", \"parsed\", \"correct\") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			preparedStatement = connection.prepareStatement(update);
			preparedStatement.setString(1, sessionId);
			preparedStatement.setString(2, email);
			preparedStatement.setString(3, schemaName);
			preparedStatement.setString(4, question);
			preparedStatement.setString(5, correctAnswer);
			preparedStatement.setString(6, userQuery);
			preparedStatement.setBoolean(7, parsed);
			preparedStatement.setBoolean(8, correct);
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public List<UserQuery> getUserQueries(String schemaName) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
	
			final String SELECT = 
					"SELECT * FROM query WHERE schema='" + schemaName + "'";
			statement = connection.createStatement();
			
			resultSet = statement.executeQuery(SELECT);
			
			List<UserQuery> userQueries = new ArrayList<UserQuery>();
			while( resultSet.next() ) {
				UserQuery query = new UserQuery();
				query.setId(resultSet.getLong("id"));
				// FIXME the bean?
				query.setEmail(resultSet.getString("email"));
				query.setSchema(resultSet.getString("schema"));
				query.setQuery(resultSet.getString("sql"));
				query.setUserDescription(resultSet.getString("user_description"));
				query.setTime(resultSet.getDate("created"));
				query.setSource(resultSet.getString("source"));
				
				userQueries.add(query);
			}
			return userQueries;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public List<UserQuery> getUserQueries() throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
	
			final String SELECT = 
					"SELECT * FROM query";
			statement = connection.createStatement();
			resultSet = statement.executeQuery(SELECT);
			
			List<UserQuery> userQueries = new ArrayList<UserQuery>();
			while( resultSet.next() ) {
				UserQuery query = new UserQuery();
				query.setId(resultSet.getLong("id"));
				// FIXME the bean?
				query.setEmail(resultSet.getString("email"));
				query.setSchema(resultSet.getString("schema"));
				query.setQuery(resultSet.getString("sql"));
				query.setUserDescription(resultSet.getString("user_description"));
				query.setTime(resultSet.getDate("created"));
				query.setSource(resultSet.getString("source"));
				
				userQueries.add(query);
			}
			return userQueries;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public List<DatabaseTable> getTables(String schemaName) throws SQLException {
		Connection connection = null;
		ResultSet resultSet = null;
		
		DatabaseMetaData metadata = null;
		ArrayList<DatabaseTable> tables = new ArrayList<DatabaseTable>();
		try {
			connection = userDataSource.getConnection();

			metadata = connection.getMetaData();
		
			resultSet = metadata.getTables(null, schemaName, "%", new String[] {"TABLE"});
			while(resultSet.next()) {
				// the API tells us the third element is the TABLE_NAME string.
				tables.add(new DatabaseTable(resultSet.getString(3)));
			}
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(connection);
		}
		
		for(int i=0; i < tables.size(); i++) {
			try {
				connection = userDataSource.getConnection();
				resultSet = metadata.getColumns(null, schemaName, tables.get(i).getTableName(), null);
				ArrayList<String> columns = new ArrayList<String>();
				while(resultSet.next()) {
					columns.add(resultSet.getString(4));
				}
				tables.get(i).setColumns(columns);
			} finally {
				Utils.tryClose(resultSet);
				Utils.tryClose(connection);
			}
		}
		return tables;

	}
	
	public List<DatabaseTable> getDevTables() throws SQLException {
		Connection connection = null;
		ResultSet resultSet = null;
		
		DatabaseMetaData metadata = null;
		ArrayList<DatabaseTable> tables = new ArrayList<DatabaseTable>();
		try { 
			connection = dataSource.getConnection();
	
			metadata = connection.getMetaData();
			
			resultSet = metadata.getTables(null, "public", "%", new String[] {"TABLE"});
			while(resultSet.next()) {
				// the API tells us the third element is the TABLE_NAME string.
				tables.add(new DatabaseTable(resultSet.getString(3)));
			}
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(connection);
		}
		
		Statement statement = null;
		
		try {
			connection = dataSource.getConnection();
			statement = connection.createStatement();
			statement.execute("set search_path to public");
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
		
		for(int i = 0; i < tables.size(); i++) {
			try {
				connection = dataSource.getConnection();
				resultSet = metadata.getColumns(null, "public", tables.get(i).getTableName(), null); 
				ArrayList<String> columns = new ArrayList<String>();
				while(resultSet.next()) {
					columns.add(resultSet.getString(4));
				}
				tables.get(i).setColumns(columns);
			} finally {
				Utils.tryClose(resultSet);
				Utils.tryClose(connection);
			}
		}

		return tables;
	}

	public String getAdminCode(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = dataSource.getConnection();
			
			String query = "SELECT \"admin_code\" FROM \"user\" WHERE \"email\" = ?;";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			preparedStatement.execute();
			
			resultSet = preparedStatement.getResultSet();
			String adminCode = null;
			while(resultSet.next()) {
				adminCode = resultSet.getString(1);
			}
			return adminCode;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}

	public List<String> getLinkedAdminCodes(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try { 
			connection = dataSource.getConnection();
		
			String query = "SELECT \"linked_admin_code\" FROM \"linked_admin_codes\" WHERE \"email\" = ?;";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			preparedStatement.execute();
			
			resultSet = preparedStatement.getResultSet();
			List<String> linkedAdminCodes = new ArrayList<String>();
			while(resultSet.next()) {
				linkedAdminCodes.add(resultSet.getString(1));
			}
			return linkedAdminCodes;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	private String generateAdminCode() throws SQLException {
		final Random random = new SecureRandom();
		byte[] buffer = new byte[5];
		String adminCode = null;
		do {
			random.nextBytes(buffer);
			adminCode = BaseEncoding.base64Url().omitPadding().encode(buffer);
		} while(adminCodeExists(adminCode));
		
		return adminCode;
	}
	
	public boolean adminCodeExists(String adminCode) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try { 
			connection = dataSource.getConnection();

			final String query = "SELECT 1 FROM \"user\" WHERE admin_code = ?;";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, adminCode);
			resultSet = preparedStatement.executeQuery();
			
			boolean adminCodeExists = false;
			if (resultSet.next()) {
				adminCodeExists = true;
			}
			return adminCodeExists;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public void promoteUserToAdmin(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		String adminCode = generateAdminCode();
		try { 
			connection = dataSource.getConnection();
			preparedStatement = connection.prepareStatement("UPDATE \"user\" SET \"admin\" = ?, \"admin_code\" = ? WHERE email = ?;");
			preparedStatement.setBoolean(1, true);
			preparedStatement.setString(2, adminCode);
			preparedStatement.setString(3, email);
			preparedStatement.execute();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		
		try { 
			connection = dataSource.getConnection();
			preparedStatement = connection.prepareStatement("INSERT INTO linked_admin_codes (email, linked_admin_code) VALUES (?, ?)");
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, adminCode);
			preparedStatement.execute();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public void promoteUserToDev(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			connection = dataSource.getConnection();
	
			preparedStatement = connection.prepareStatement("UPDATE \"user\" SET \"developer\" = ? WHERE email = ?;");
			preparedStatement.setBoolean(1, true);
			preparedStatement.setString(2, email);
			preparedStatement.execute();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public void demoteUserFromAdmin(String email, String adminCode) throws SQLException {
		Connection connection = null; 
		PreparedStatement preparedStatement = null;
		
		try { 
			connection = dataSource.getConnection();

			preparedStatement = connection.prepareStatement("UPDATE \"user\" SET \"admin\" = ?, \"admin_code\" = ? WHERE email = ?;");
			preparedStatement.setBoolean(1, false);
			preparedStatement.setString(2, null);
			preparedStatement.setString(3, email);
			preparedStatement.execute();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		
		try { 
			connection = dataSource.getConnection();
			
			preparedStatement = connection.prepareStatement("DELETE FROM linked_admin_codes WHERE linked_admin_code = ?");
			preparedStatement.setString(1, adminCode);
			preparedStatement.execute();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public void demoteUserFromDev(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			connection = dataSource.getConnection();
	
			preparedStatement = connection.prepareStatement("UPDATE \"user\" SET \"developer\" = ? WHERE email = ?;");
			preparedStatement.setBoolean(1, false);
			preparedStatement.setString(2, email);
			preparedStatement.execute();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}

	public boolean isDeveloper(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try { 
			connection = dataSource.getConnection();
	
			final String query = "SELECT developer FROM \"user\" WHERE email = ?;";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			resultSet = preparedStatement.executeQuery();
			
			boolean isDeveloper = false;
			if (resultSet.next()) {
				isDeveloper = resultSet.getBoolean(1);
			}
			return isDeveloper;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}

	public void linkCode(String email, String code) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try { 
			connection = dataSource.getConnection();
			
			final String update = "INSERT INTO \"linked_admin_codes\" (\"email\", \"linked_admin_code\") VALUES (?, ?)";
			preparedStatement = connection.prepareStatement(update);
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, code);
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}

	public void unlinkCode(String email, String code) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			connection = dataSource.getConnection();
		
			final String update = "DELETE FROM linked_admin_codes WHERE email = ? AND linked_admin_code = ?";
			preparedStatement = connection.prepareStatement(update);
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, code);
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	private byte[] stringByteArrayToByteArray(String stringByteArray) {
		String[] byteValues = stringByteArray.substring(1, stringByteArray.length() - 1).split(",");
		byte[] bytes = new byte[byteValues.length];
		
		for (int i = 0; i < bytes.length; i++) {
		   bytes[i] = Byte.valueOf(byteValues[i].trim());     
		}

		return bytes;
	}
}