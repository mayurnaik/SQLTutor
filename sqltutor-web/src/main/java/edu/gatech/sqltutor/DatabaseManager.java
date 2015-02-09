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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
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

import edu.gatech.sqltutor.entities.SchemaOptionsTuple;
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
	
	public boolean isAdmin(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		boolean isAdmin = false;
		try {
			connection = dataSource.getConnection();
			
			preparedStatement = connection.prepareStatement("SELECT admin FROM \"user\" WHERE email = ?;");
			preparedStatement.setString(1, email);
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next()) 
				isAdmin = resultSet.getBoolean(1);
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		return isAdmin;
	}
	
	public List<String> getUserSchemas(boolean admin) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
	
		ArrayList<String> schemas = new ArrayList<String>();
		try { 
			connection = dataSource.getConnection();
			
			String query = admin ? "SELECT schema FROM schema_options;" : 
				"SELECT schema FROM schema_options WHERE visible_to_users AND (open_access IS NULL OR open_access <= now()) AND (close_access IS NULL OR close_access >= now());";
			
			statement = connection.createStatement();
			statement.execute(query);
			
			resultSet = statement.getResultSet();
			
			while(resultSet.next()) 
				schemas.add(resultSet.getString(1));
		} finally {	
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
		return schemas;
	}
	
	public SchemaOptionsTuple getOptions(String schemaName) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		SchemaOptionsTuple options = null;
		try {
			connection = dataSource.getConnection();
			
			statement = connection.createStatement();
			statement.execute("SELECT visible_to_users, in_order_questions, link, open_access, close_access FROM schema_options WHERE schema = '" + schemaName +"'");
			
			resultSet = statement.getResultSet();
			resultSet.next();
			
			final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST")); 
			options = new SchemaOptionsTuple(resultSet.getBoolean(1), resultSet.getBoolean(2), resultSet.getString(3),
					resultSet.getTimestamp(4, cal), resultSet.getTimestamp(5, cal));
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
		return options;
	}
	
	private void dropSchema(String schemaName) throws SQLException {
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
	}
	
	private void dropSchemaOptionsAndQuestions(String schemaName) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		
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
	
	public void deleteSchema(String schemaName) throws SQLException {
		dropSchema(schemaName);
		dropSchemaOptionsAndQuestions(schemaName);
	}
	
	public boolean checkSchemaPermissions(String email, String schemaName) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		boolean schemaPermissions = false;
		try { 
			connection = dataSource.getConnection();
	
			final String query = "SELECT 1 FROM schema_options WHERE schema = ? AND owner = ?;";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, schemaName);
			preparedStatement.setString(2, email);
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next()) 
				schemaPermissions = true;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}	
		return schemaPermissions;
	}
	
	private int getHighestQuestionOrder() throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
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
		return order;
	}
	
	public void addQuestion(String schemaName, String question, String answer) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		int order = getHighestQuestionOrder();
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
		
		List<UserTuple> users = new ArrayList<UserTuple>();
		try { 
			connection = dataSource.getConnection();
			
			statement = connection.createStatement();
			statement.execute("SELECT \"email\", \"admin\", \"admin_code\", \"developer\" FROM \"user\"");
			
			resultSet = statement.getResultSet();
			
			while(resultSet.next())
				users.add(new UserTuple(resultSet.getString(1), resultSet.getBoolean(2), resultSet.getString(3), resultSet.getBoolean(4)));
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
		return users;
	}
	
	public UserTuple getUserTuple(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		UserTuple user = new UserTuple();
		try { 
			connection = dataSource.getConnection();
		
			preparedStatement = connection.prepareStatement("SELECT \"admin\", \"admin_code\", \"developer\" FROM \"user\" WHERE email = ?");
			preparedStatement.setString(1, email);
			preparedStatement.execute();
			
			resultSet = preparedStatement.getResultSet();
			while(resultSet.next())
				user = new UserTuple(email, resultSet.getBoolean(1), resultSet.getString(2), resultSet.getBoolean(3));
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}	
		return user;
	}
	
	public List<QuestionTuple> getQuestions(String schemaName) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		List<QuestionTuple> questions = new ArrayList<QuestionTuple>();
		try {
			connection = dataSource.getConnection();
		
			statement = connection.createStatement();
			statement.execute("SELECT \"order\", question, answer, id FROM "
				+ "schema_questions WHERE schema = '" + schemaName + 
				"' ORDER BY \"order\";");
		
			resultSet = statement.getResultSet();
			while(resultSet.next()) 
				questions.add(new QuestionTuple(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getInt(4)));
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
		return questions;
	}
	
	public void setOptions(String schemaName, SchemaOptionsTuple options) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			connection = dataSource.getConnection();
			final String update = "UPDATE schema_options SET visible_to_users = ?, in_order_questions = ?, open_access = ?, close_access = ?, link = ? WHERE schema = ?;";
			preparedStatement = connection.prepareStatement(update);
			preparedStatement.setBoolean(1, options.isInOrderQuestions());
			preparedStatement.setBoolean(2, options.isVisibleToUsers());
			final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
			preparedStatement.setTimestamp(3, options.getOpenAccess(), cal);
			preparedStatement.setTimestamp(4, options.getCloseAccess(), cal);
			preparedStatement.setString(5, options.getLink());
			preparedStatement.setString(6, schemaName);
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	private void runSchemaScript(String schemaDump) throws SQLException, IOException {
		Connection connection = null;
		BufferedReader reader = null;
		
		try { 
			connection = userDataSource.getConnection();
			
			ScriptRunner runner = new ScriptRunner(connection, false, true);
			reader = new BufferedReader(new StringReader(schemaDump));
			runner.runScript(reader);
		} finally {
			Utils.tryClose(reader);
			Utils.tryClose(connection);
		}
	}
	
	private void grantAccessToReadonly(String schemaName) throws SQLException {
		Connection connection = null;
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
	}
	
	private void addSchemaToOptions(String schemaName, String email) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		
		try { 
			connection = dataSource.getConnection();
			
			statement = connection.createStatement();
			statement.execute("INSERT INTO schema_options (schema, owner) VALUES ('"+schemaName+"', '"+email+"');");
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
		
		runSchemaScript(schemaDump);
		
		// get schema name
		Pattern p = Pattern.compile("(?<=CREATE SCHEMA\\W{1,2})(\\w+)");
		Matcher m = p.matcher(schemaDump);
		m.find();
		String schemaName = m.group(1);
		
		grantAccessToReadonly(schemaName);
		
		addSchemaToOptions(schemaName, email);

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
	
	
	public void addPasswordChangeRequest(String email, UUID uuid) throws SQLException {
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
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public boolean getPasswordChangeRequest(String email, String id) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		boolean autheticated = false;
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
		
			while (resultSet.next()) {
				byte[] salt = stringByteArrayToByteArray(resultSet.getString(1));
				byte[] encryptedId = stringByteArrayToByteArray(resultSet.getString(2));
				
				// use the password hasher to authenticate
				autheticated = SaltHasher.authenticate(id, encryptedId, salt);
			} 
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		return autheticated;
	}

	public void changePassword(String email, String newPassword) throws SQLException {
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
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}

	public boolean emailExists(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		boolean emailExists = false;
		try {
			connection = dataSource.getConnection();
		
			preparedStatement = connection.prepareStatement("SELECT 1 FROM \"user\" WHERE email = ?;");
			preparedStatement.setString(1, email);
			preparedStatement.execute();
			
			resultSet = preparedStatement.getResultSet();
			if(resultSet.next()) 
				emailExists = true;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		return emailExists;
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
		} finally {
			Utils.tryClose(connection);
			Utils.tryClose(statement);
		}
			
		HashMap<String, QueryResult> allData = new HashMap<String, QueryResult>();
		for(String tableName : tables) {
			ArrayList<List<String>> queryData = new ArrayList<List<String>>();
			ArrayList<String> columnNames = new ArrayList<String>();
			try {
				connection = userDataSource.getConnection();
				
				statement = connection.createStatement();
				
				resultSet = statement.executeQuery("SELECT * FROM \"" + tableName + "\";");
				
				ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
				int columnCount = resultSetMetaData.getColumnCount();
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
			} finally {
				Utils.tryClose(resultSet);
				Utils.tryClose(statement);
				Utils.tryClose(connection);
			}
			allData.put(tableName, new QueryResult(columnNames, queryData));
		}
		return allData;
	}
	
	public HashMap<String, QueryResult> getAllDevData() throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		List<DatabaseTable> devTables = getDevTables();
		List<String> tables = new ArrayList<String>();
		for(DatabaseTable dt : devTables)
			tables.add(dt.getTableName());
		
		try {
			connection = dataSource.getConnection();
			
			statement = connection.createStatement();
			statement.execute("set search_path to public");
		} finally {
			Utils.tryClose(connection);
			Utils.tryClose(statement);
		}
		
		HashMap<String, QueryResult> allData = new HashMap<String, QueryResult>();
		for(String tableName : tables) {
			ArrayList<List<String>> queryData = new ArrayList<List<String>>();
			ArrayList<String> columnNames = new ArrayList<String>();
			try {
				connection = dataSource.getConnection();
				
				statement = connection.createStatement();
				
				resultSet = statement.executeQuery("SELECT * FROM \"" + tableName + "\";");
				
				ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
				int columnCount = resultSetMetaData.getColumnCount();
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
			} finally {
				Utils.tryClose(resultSet);
				Utils.tryClose(statement);
				Utils.tryClose(connection);
			}
			allData.put(tableName, new QueryResult(columnNames, queryData));
		}
		return allData;
	}
	
	public void verifyQuery(String schemaName, String query) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		
		try {
			connection = readUserDataSource.getConnection();
			
			statement = connection.createStatement();
			statement.addBatch("set search_path to '" + schemaName + "'");
			statement.addBatch(query);
			statement.executeBatch();
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
	}
	
	public QueryResult getQueryResult(String schemaName, String query, boolean dev) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		ArrayList<List<String>> queryData = new ArrayList<List<String>>();
		ArrayList<String> columnNames = new ArrayList<String>();
		try {
			connection = dev ? userDataSource.getConnection() : readUserDataSource.getConnection();
			
			statement = connection.createStatement();
			statement.execute("set search_path to '" + schemaName + "'");
			
			resultSet = statement.executeQuery(query);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		
			int columnCount = resultSetMetaData.getColumnCount();
			for(int i = 1; i <=  columnCount; i++) {
				columnNames.add(resultSetMetaData.getColumnName(i));
			}
			
			while(resultSet.next()) {
				List<String> rowData = new ArrayList<String>();
				for (int i = 1; i <= columnCount; i++) {
					rowData.add(resultSet.getString(i));
				}
				queryData.add(rowData);
			}
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(connection);
			Utils.tryClose(statement);
		}
		return new QueryResult(columnNames, queryData);
	}
	
	public QueryResult getDevQueryResult(String query, boolean dev) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		ArrayList<List<String>> queryData = new ArrayList<List<String>>();
		ArrayList<String> columnNames = new ArrayList<String>();
		try {
			connection = dataSource.getConnection();
		
			statement = connection.createStatement();
			statement.execute("set search_path to public");
		
			resultSet = statement.executeQuery(query);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		
			int columnCount = resultSetMetaData.getColumnCount();
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
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(connection);
			Utils.tryClose(statement);
		}
		return new QueryResult(columnNames, queryData);
	}
	
	private void addUser(String hashedEmail, String email, String password) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			connection = dataSource.getConnection();
	
			// generate the user's encryption salt and password
			byte[] salt = SaltHasher.generateSalt();
			byte[] encryptedPassword = SaltHasher.getEncryptedValue(password, salt);
			
			final String query = "INSERT INTO \"user\" (\"password\", \"salt\", \"email\", \"email_plain\") VALUES (?, ?, ?, ?)";
			
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, Arrays.toString(encryptedPassword));
			preparedStatement.setString(2, Arrays.toString(salt));
			preparedStatement.setString(3, hashedEmail);
			preparedStatement.setString(4, email);
			preparedStatement.executeUpdate();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	private void addUserToLinkedAdminCodes(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			connection = dataSource.getConnection();
			
			preparedStatement = connection.prepareStatement("INSERT INTO linked_admin_codes (\"email\") VALUES (?)");
			preparedStatement.setString(1, email);
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	private void addUserToLinkedAdminCodes(String email, String adminCode) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
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
	
	public void registerUser(String hashedEmail, String email, String password) throws SQLException {
		addUser(hashedEmail, email, password);
		addUserToLinkedAdminCodes(hashedEmail);
	}
	
	private void deleteUserEmail(String email) throws SQLException {
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
	}
	
	private void deleteUserAdminLinks(String adminCode) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
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
	
	public void deleteUser(String email, String adminCode) throws SQLException {
		deleteUserEmail(email);
		deleteUserAdminLinks(adminCode);
	}
	
	/**
	 * Returns whether the given email is registered.
	 * 
	 * @param hashedEmail	email to check
	 * @return	whether the email is registered
	 * @throws SQLException
	 */
	public boolean isEmailRegistered(String hashedEmail, String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		boolean isEmailRegistered = false;
		try {
			connection = dataSource.getConnection();
	
			final String query = "SELECT 1 FROM \"user\" WHERE \"email\" = ?";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, hashedEmail);
			
			resultSet = preparedStatement.executeQuery();
			
			if ( resultSet.next() )
				isEmailRegistered = true;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		
		// if the email is registered, make sure their "email_plain" isn't null in the DB
		if( isEmailRegistered && !hasPlainTextEmail(hashedEmail) ) {
			updatePlainTextEmail(hashedEmail, email);
		}

		return isEmailRegistered;
	}
	
	public boolean hasPlainTextEmail(String hashedEmail) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		boolean hasPlainTextEmail = false;
		try {
			connection = dataSource.getConnection();
	
			final String query = "SELECT 1 FROM \"user\" WHERE \"email\" = ? AND \"email_plain\" != NULL";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, hashedEmail);
			
			resultSet = preparedStatement.executeQuery();
			
			if ( resultSet.next() )
				hasPlainTextEmail = true;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		
		return hasPlainTextEmail;
	}
	
	public void updatePlainTextEmail(String hashedEmail, String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = dataSource.getConnection();
	
			final String query = "UPDATE \"user\" SET \"email_plain\" = ? WHERE \"email\" = ?";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, hashedEmail);
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public boolean isPasswordCorrect(String email, String attemptedPassword) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		boolean isCorrect = false;
		try {
			connection = dataSource.getConnection();

			preparedStatement = connection.prepareStatement("SELECT \"salt\", \"password\" FROM \"user\" WHERE \"email\" = ?");
			preparedStatement.setString(1, email);
			
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next()) {	
				byte[] salt = stringByteArrayToByteArray(resultSet.getString(1));
				byte[] encryptedPassword = stringByteArrayToByteArray(resultSet.getString(2));
				
				// use the password hasher to authenticate
				isCorrect = SaltHasher.authenticate(attemptedPassword, encryptedPassword, salt);
			} 
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		return isCorrect;
	}
	
	public void log(String sessionId, String email, String schemaName, String question, String correctAnswer, String userQuery, boolean parsed, boolean correct, String nlpFeedback) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try { 
			connection = dataSource.getConnection();
			
			preparedStatement = connection.prepareStatement("INSERT INTO \"log\" (\"session_id\", "
					+ "\"email\", \"schema\", \"question\", \"correct_answer\", \"query\", "
					+ "\"parsed\", \"correct\", \"nlp_feedback\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			preparedStatement.setString(1, sessionId);
			preparedStatement.setString(2, email);
			preparedStatement.setString(3, schemaName);
			preparedStatement.setString(4, question);
			preparedStatement.setString(5, correctAnswer);
			preparedStatement.setString(6, userQuery);
			preparedStatement.setBoolean(7, parsed);
			preparedStatement.setBoolean(8, correct);
			preparedStatement.setString(9, nlpFeedback);
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public void logQuestionPresentation(String sessionId, String email, String schemaName, String question) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try { 
			connection = dataSource.getConnection();
			
			preparedStatement = connection.prepareStatement("INSERT INTO \"log_question_presentation\" (\"session_id\", "
					+ "\"email\", \"schema\", \"question\") VALUES (?, ?, ?, ?)");
			preparedStatement.setString(1, sessionId);
			preparedStatement.setString(2, email);
			preparedStatement.setString(3, schemaName);
			preparedStatement.setString(4, question);
			preparedStatement.executeUpdate();
		} finally {
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
	}
	
	public void logException(String sessionId, String email, String exception) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try { 
			connection = dataSource.getConnection();
			
			preparedStatement = connection.prepareStatement("INSERT INTO \"log_exceptions\" (\"session_id\", "
					+ "\"email\", \"exception\") VALUES (?, ?, ?)");
			preparedStatement.setString(1, sessionId);
			preparedStatement.setString(2, email != null ? email : "not logged in");
			preparedStatement.setString(3, exception);
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
		
		List<UserQuery> userQueries = new ArrayList<UserQuery>();
		try {
			connection = dataSource.getConnection();

			statement = connection.createStatement();
			
			resultSet = statement.executeQuery("SELECT * FROM query WHERE schema='" + schemaName + "'");
		
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
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
		return userQueries;
	}
	
	public List<UserQuery> getUserQueries() throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		List<UserQuery> userQueries = new ArrayList<UserQuery>();
		try {
			connection = dataSource.getConnection();

			statement = connection.createStatement();
			
			resultSet = statement.executeQuery("SELECT * FROM query");
			
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
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(statement);
			Utils.tryClose(connection);
		}
		return userQueries;
	}
	
	public List<DatabaseTable> getTables(String schemaName) throws SQLException {
		try (Connection conn = userDataSource.getConnection()) {
			DatabaseMetaData metadata = conn.getMetaData();
			return QueryUtils.readTableInfo(metadata, schemaName);
		}
	}
	
	public List<DatabaseTable> getDevTables() throws SQLException {
		try (Connection conn = dataSource.getConnection()) {
			return QueryUtils.readTableInfo(conn.getMetaData(), "public");
		}
	}

	public String getAdminCode(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		String adminCode = null;
		try {
			connection = dataSource.getConnection();
			
			preparedStatement = connection.prepareStatement("SELECT \"admin_code\" FROM \"user\" WHERE \"email\" = ?;");
			preparedStatement.setString(1, email);
			preparedStatement.execute();
			
			resultSet = preparedStatement.getResultSet();
			
			while(resultSet.next()) 
				adminCode = resultSet.getString(1);
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		return adminCode;
	}

	public List<String> getLinkedAdminCodes(String email) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		List<String> linkedAdminCodes = new ArrayList<String>();
		try { 
			connection = dataSource.getConnection();
	
			preparedStatement = connection.prepareStatement("SELECT \"linked_admin_code\" FROM \"linked_admin_codes\" WHERE \"email\" = ?;");
			preparedStatement.setString(1, email);
			preparedStatement.execute();
			
			resultSet = preparedStatement.getResultSet();
			while(resultSet.next()) 
				linkedAdminCodes.add(resultSet.getString(1));
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		return linkedAdminCodes;
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
		
		boolean adminCodeExists = false;
		try { 
			connection = dataSource.getConnection();

			preparedStatement = connection.prepareStatement("SELECT 1 FROM \"user\" WHERE admin_code = ?;");
			preparedStatement.setString(1, adminCode);
			
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next())
				adminCodeExists = true;
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		return adminCodeExists;
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
		
		addUserToLinkedAdminCodes(email, adminCode);
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
		
		deleteUserFromLinkedAdminCodes(adminCode);
	}
	
	private void deleteUserFromLinkedAdminCodes(String adminCode) throws SQLException {
		Connection connection = null; 
		PreparedStatement preparedStatement = null;
		
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
		
		boolean isDeveloper = false;
		try { 
			connection = dataSource.getConnection();
	
			preparedStatement = connection.prepareStatement("SELECT developer FROM \"user\" WHERE email = ?;");
			preparedStatement.setString(1, email);
			
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next())
				isDeveloper = resultSet.getBoolean(1);
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		return isDeveloper;
	}

	public void linkCode(String email, String code) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try { 
			connection = dataSource.getConnection();
			
			preparedStatement = connection.prepareStatement("INSERT INTO \"linked_admin_codes\" (\"email\", \"linked_admin_code\") VALUES (?, ?)");
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

			preparedStatement = connection.prepareStatement("DELETE FROM linked_admin_codes WHERE email = ? AND linked_admin_code = ?");
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

	public String getUserSchema(String link) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		String schema = null;
		try {
			connection = dataSource.getConnection();

			preparedStatement = connection.prepareStatement("SELECT schema FROM schema_options WHERE link = ?");
			preparedStatement.setString(1, link);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next())
				schema = resultSet.getString(1);
			
		} finally {
			Utils.tryClose(resultSet);
			Utils.tryClose(preparedStatement);
			Utils.tryClose(connection);
		}
		return schema;
	}
}