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
package edu.gatech.sqltutor.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.BaseEncoding;

import edu.gatech.sqltutor.DatabaseTable;
import edu.gatech.sqltutor.QueryResult;
import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.tuples.QuestionTuple;
import edu.gatech.sqltutor.tuples.TutorialOptionsTuple;
import edu.gatech.sqltutor.tuples.UserQuery;
import edu.gatech.sqltutor.tuples.UserTuple;
import edu.gatech.sqltutor.util.ScriptRunner;

@ManagedBean(name="databaseManager", eager=true)
@ApplicationScoped
public class DatabaseManager implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

	@Resource(name="jdbc/sqltutorDB")
	private DataSource dataSource;

	@Resource(name="jdbc/sqltutorUserDB")
	private DataSource userDataSource;

	@Resource(name="jdbc/sqltutorUserDBRead")
	private DataSource readUserDataSource;

	public static final int QUERY_TIMEOUT_SECONDS = 30;

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
		boolean isAdmin = false;
		try (final Connection connection = dataSource.getConnection()) {
			
			try(final PreparedStatement preparedStatement = connection.prepareStatement("SELECT admin FROM \"user\" WHERE email = ?;")) {
				preparedStatement.setString(1, email);
				
				try(final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next())
						isAdmin = resultSet.getBoolean(1);
				}
			}
		} 
		return isAdmin;
	}

	public TutorialOptionsTuple getOptions(String tutorialName, String tutorialAdminCode) throws SQLException {
		TutorialOptionsTuple options = null;
		try (final Connection connection = dataSource.getConnection()) {
			
			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT visible_to_users, in_order_questions, "
					+ "link, open_access, close_access, max_question_attempts, admin_code, schema FROM schema_options WHERE schema = ? "
					+ "AND admin_code = ?")) {
				preparedStatement.setString(1, tutorialName);
				preparedStatement.setString(2, tutorialAdminCode);

				try(final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
						options = new TutorialOptionsTuple(resultSet.getBoolean(1), resultSet.getBoolean(2), resultSet.getString(3),
								resultSet.getTimestamp(4, cal), resultSet.getTimestamp(5, cal), resultSet.getInt(6), resultSet.getString(7), resultSet.getString(8));
					}
				}
			}

		} 
		return options;
	}

	public void deleteTutorial(String tutorial, String tutorialName, String tutorialAdminCode) throws SQLException {
		// Drop the schema from the user data source
		try (final Connection connection = userDataSource.getConnection()) {

			try (final Statement statement = connection.createStatement()) {
				statement.executeUpdate("DROP SCHEMA " + tutorialName + " CASCADE;");
			}
		} 
		
		// Delete the schema from the schema_options table and the schema_questions table
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM schema_options WHERE schema = ? AND admin_code = ?")) { 
				preparedStatement.setString(1, tutorialName);
				preparedStatement.setString(2, tutorialAdminCode);
				preparedStatement.executeUpdate();
			}
			
			try (final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM schema_questions WHERE schema = ? AND admin_code = ?;")) {
				preparedStatement.setString(1, tutorialName);
				preparedStatement.setString(2, tutorialAdminCode);
				preparedStatement.executeUpdate();
			}
		} 
	}

	public boolean checkTutorialPermissions(String tutorialName, String tutorialAdminCode) throws SQLException {
		boolean schemaPermissions = false;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM schema_options WHERE schema = ? AND admin_code = ?;")) {
				preparedStatement.setString(1, tutorialName);
				preparedStatement.setString(2, tutorialAdminCode);
				
				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next())
						schemaPermissions = true;
				}
			}
		}       
		return schemaPermissions;
	}

	public void addQuestion(String tutorialName, QuestionTuple question, String tutorialAdminCode) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement(
					"INSERT INTO schema_questions (schema, question, answer, \"order\", concept_tags, performance_leniency_seconds, column_order_matters, row_order_matters, admin_code) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
				preparedStatement.setString(1, tutorialName);
				preparedStatement.setString(2, question.getQuestion());
				preparedStatement.setString(3, question.getAnswer());
				preparedStatement.setInt(4, question.getOrder());
				preparedStatement.setArray(5, question.getConcepts() != null ? connection.createArrayOf("text", question.getConcepts()) : null);
				preparedStatement.setDouble(6, question.getPerformanceLeniencySeconds());
				preparedStatement.setBoolean(7, question.isColumnOrderMatters());
				preparedStatement.setBoolean(8, question.isRowOrderMatters());
				preparedStatement.setString(9, tutorialAdminCode);
				preparedStatement.executeUpdate();
			}
		}
	}

	public List<UserTuple> getUserTuples() throws SQLException {
		List<UserTuple> users = null;
		try (final Connection connection = dataSource.getConnection()) {
			
			try (final Statement statement = connection.createStatement()) {
				
				try (final ResultSet resultSet = statement.executeQuery("SELECT \"email\", \"admin\", \"admin_code\", \"developer\" FROM \"user\"")) {
					if (resultSet.isBeforeFirst())
						users = new LinkedList<UserTuple>();
					while (resultSet.next()) 
						users.add(new UserTuple(resultSet.getString(1), resultSet.getBoolean(2), resultSet.getString(3), resultSet.getBoolean(4)));
				}
			}
		} 
		return users;
	}

	public UserTuple getUserTuple(String email) throws SQLException {
		UserTuple user = null;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT \"admin\", \"admin_code\", \"developer\" FROM \"user\" WHERE email = ?")) {
				preparedStatement.setString(1, email);

				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) 
						user = new UserTuple(email, resultSet.getBoolean(1), resultSet.getString(2), resultSet.getBoolean(3));
				}
			}
		}      
		return user;
	}

	public List<QuestionTuple> getQuestions(String tutorialName, String tutorialAdminCode) throws SQLException {
		List<QuestionTuple> questions = null;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement(
					"SELECT \"order\", question, answer, id, concept_tags, performance_leniency_seconds, column_order_matters, row_order_matters   "
					+ "FROM schema_questions WHERE schema = ? AND admin_code = ? ORDER BY \"order\";")) {
				preparedStatement.setString(1, tutorialName);
				preparedStatement.setString(2, tutorialAdminCode);
			
				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.isBeforeFirst())
						questions = new LinkedList<QuestionTuple>();
					while (resultSet.next()) {
						questions.add(new QuestionTuple(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getInt(4), (resultSet.getArray(5) != null ? (String[])resultSet.getArray(5).getArray() : null), resultSet.getDouble(6), resultSet.getBoolean(7), resultSet.getBoolean(8)));
					}
				}
			}
		} 
		return questions;
	}

	public void setOptions(String tutorialName, TutorialOptionsTuple options, String tutorialAdminCode) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {
			
			try (final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE schema_options SET visible_to_users = ?, in_order_questions = ?, "
					+ "open_access = ?, close_access = ?, link = ?, max_question_attempts = ? WHERE schema = ? AND admin_code = ?;")) {
				preparedStatement.setBoolean(1, options.isInOrderQuestions());
				preparedStatement.setBoolean(2, options.isVisibleToUsers());
				final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
				preparedStatement.setTimestamp(3, options.getOpenAccess(), cal);
				preparedStatement.setTimestamp(4, options.getCloseAccess(), cal);
				preparedStatement.setString(5, options.getLink());
				preparedStatement.setInt(6, options.getMaxQuestionAttempts());
				preparedStatement.setString(7, tutorialName);
				preparedStatement.setString(8, tutorialAdminCode);
				preparedStatement.executeUpdate();
			}
		} 
	}

	/**
	 * 
	 * @param schemaDump	note: finds the name of the schema from this dump using the following regex: "CREATE SCHEMA\\s+(IF\\s+NOT\\s+EXISTS\\s+)(\\w+)"
	 * @param tutorialAdminCode
	 * @return	the name of the schema
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public String addSchema(String schemaDump, String tutorialAdminCode) throws SQLException, IllegalArgumentException, IOException {
		if (schemaDump == null || schemaDump.length() == 0) 
			throw new IllegalArgumentException("Schema file is null or empty.");

		// get schema name, modify, and replace as "<tutorialAdminCode>_<schemaName>"
		String schemaName = null;
		{
			final Pattern p = Pattern.compile("CREATE SCHEMA\\s+(IF\\s+NOT\\s+EXISTS\\s+)(\\w+)", Pattern.CASE_INSENSITIVE);
			final Matcher m = p.matcher(schemaDump);
			m.find();
			schemaName = m.group(2);
			// add tutorialAdminCode to schemaName
			schemaDump.replaceAll(schemaName, tutorialAdminCode + "_" + schemaName);
		}
		
		if (schemaName == null)
			throw new IllegalArgumentException("Unable to find schema name. Regex used: \"CREATE SCHEMA\\s+(IF\\s+NOT\\s+EXISTS\\s+)(\\w+)\"");
		
		try (final Connection connection = userDataSource.getConnection()) {
			
			try (final Reader reader = new BufferedReader(new StringReader(schemaDump))) {
				final ScriptRunner runner = new ScriptRunner(connection, false, true);
				runner.setLogWriter(null);
				runner.runScript(reader);
			}

			try (final Statement statement = connection.createStatement()) {
				statement.addBatch("GRANT SELECT ON ALL TABLES IN SCHEMA \"" + schemaName + "\" TO readonly_user;");
				statement.addBatch("GRANT USAGE ON SCHEMA \"" + schemaName + "\" TO readonly_user;");
				statement.executeBatch();
			}
		} 
		
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO schema_options (schema, adminCode) VALUES (?, ?);")) {
				preparedStatement.setString(1, schemaName);
				preparedStatement.setString(2, tutorialAdminCode);
				preparedStatement.executeUpdate();
			}
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

	public DataSource getReadUserDataSource() {
		return readUserDataSource;
	}

	public void setReadUserDataSource(DataSource readUserDataSource) {
		this.readUserDataSource = readUserDataSource;
	}

	public void reorderQuestions(List<QuestionTuple> questions) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final Statement statement = connection.createStatement()) {
				for(int i = 0; i < questions.size(); i++) {
					final int id = questions.get(i).getId();
					statement.addBatch("UPDATE schema_questions SET \"order\" = "
							+ (i+1) + " WHERE id = " + id + ";");
				}
				statement.executeBatch();
			}
		} 
	}

	public void deleteQuestions(List<QuestionTuple> questions) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final Statement statement = connection.createStatement()) {
				for(int i = 0; i < questions.size(); i++) {
					final int id = questions.get(i).getId();
					statement.addBatch("DELETE FROM schema_questions WHERE id = " + id + ";");
				}
				statement.executeBatch();
			}
		}
	}


	public void addPasswordChangeRequest(String email, UUID uuid) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {
			// generate the user's encryption salt and password
			try {
				final byte[] salt = SaltHasher.generateSalt();
				final byte[] encryptedId = SaltHasher.getEncryptedValue(uuid.toString(), salt);
				
				try (final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO \"password_change_requests\" (\"email\", \"id\", \"salt\") VALUES (?, ?, ?)")) {
					preparedStatement.setString(1, email);
					preparedStatement.setString(2, Arrays.toString(encryptedId));
					preparedStatement.setString(3, Arrays.toString(salt));
					preparedStatement.executeUpdate();
				}
			} catch (NoSuchAlgorithmException e) {
				// TODO: 
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO: 
				e.printStackTrace();
			}
		} 
	}

	public boolean getPasswordChangeRequest(String email, String id) throws SQLException {
		boolean autheticated = false;
		try (final Connection connection = dataSource.getConnection()) {

			// Get the salt and id of the most recent request
			// that belongs to the user within the last 24 hours
			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT \"salt\", \"id\" FROM \"password_change_requests\" WHERE "
					+ "\"time\" = (SELECT MAX(\"time\") FROM \"password_change_requests\" WHERE \"email\" = ? AND "
					+ "\"time\" >= (now() - '1 day'::INTERVAL));")) {
				preparedStatement.setString(1, email);

				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						final byte[] salt = stringByteArrayToByteArray(resultSet.getString(1));
						final byte[] encryptedId = stringByteArrayToByteArray(resultSet.getString(2));
						// use the password hasher to authenticate
						try {
							autheticated = SaltHasher.authenticate(id, encryptedId, salt);
						} catch (NoSuchAlgorithmException e) {
							// TODO:
							e.printStackTrace();
						} catch (InvalidKeySpecException e) {
							// TODO:
							e.printStackTrace();
						}
					}
				}
			}
		} 
		return autheticated;
	}

	public void changePassword(String email, String newPassword) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {
			
			try {
				// generate the user's encryption salt and password
				final byte[] salt = SaltHasher.generateSalt();
				final byte[] encryptedPassword = SaltHasher.getEncryptedValue(newPassword, salt);
	
				try (final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE \"user\" SET \"password\" = ?, \"salt\" = ? WHERE email = ?;")) {
					preparedStatement.setString(1, Arrays.toString(encryptedPassword));
					preparedStatement.setString(2, Arrays.toString(salt));
					preparedStatement.setString(3, email);
					preparedStatement.executeUpdate();
				}
			} catch (NoSuchAlgorithmException e) {
				// TODO:
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO:
				e.printStackTrace();
			}
		}
	}

	public boolean emailExists(String email) throws SQLException {
		boolean emailExists = false;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM \"user\" WHERE email = ?;")) {
				preparedStatement.setString(1, email);

				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next())
						emailExists = true;
				}
			}
		}
		return emailExists;
	}
	
	public HashMap<String, QueryResult> getAllData(String schemaName, List<String> tables) throws SQLException {
		HashMap<String, QueryResult> allData = null;
		try (final Connection connection = userDataSource.getConnection()) {
			
			try (final Statement statement = connection.createStatement()) {
				statement.execute("SET search_path TO '" + schemaName + "'");
	
				if (tables != null && tables.size() > 0)
					allData = new HashMap<String, QueryResult>();
				for (String tableName : tables) {
					List<String> columnNames = null;
					List<List<String>> queryData = null;
					
					try (ResultSet resultSet = statement.executeQuery("SELECT * FROM \"" + tableName + "\";")) {
						// get column names
						columnNames = new LinkedList<String>();
						final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
						final int columnCount = resultSetMetaData.getColumnCount();
						for (int i = 1; i <=  columnCount; i++) {
							columnNames.add(resultSetMetaData.getColumnName(i));
						}
						// get query data
						if (resultSet.isBeforeFirst()) 
							queryData = new LinkedList<List<String>>();
						while (resultSet.next()) {
							final List<String> rowData = new LinkedList<String>();
							for (int i = 1; i <= columnCount; i++) {
								rowData.add(resultSet.getString(i));
							}
							queryData.add(rowData);
						}
					} 
					allData.put(tableName, new QueryResult(columnNames, queryData));
				}
			}
		}
		return allData;
	}
	
	public List<DatabaseTable> getDevSchemaTables() throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {
			return QueryUtils.readTableInfo(connection.getMetaData(), "public");
		}
	}
	
	public List<DatabaseTable> getSchemaTables(String schema) throws SQLException {
		try (Connection connection = userDataSource.getConnection()) {
			return QueryUtils.readTableInfo(connection.getMetaData(), schema);
		}
	}

	public HashMap<String, QueryResult> getAllDevData() throws SQLException {
		HashMap<String, QueryResult> allData = null;
		try (final Connection connection = dataSource.getConnection()) {
			final List<DatabaseTable> devTables = QueryUtils.readTableInfo(connection.getMetaData(), "public");
			List<String> tables = new LinkedList<String>();
			for(DatabaseTable dt : devTables)
				tables.add(dt.getTableName());
			
			try (final Statement statement = connection.createStatement()) {
				if (tables.size() > 0) {
					statement.execute("SET search_path TO 'public'");
					allData = new HashMap<String, QueryResult>();
				}

				for(String tableName : tables) {
					List<String> columnNames = null;
					List<List<String>> queryData = null;
						
					try (ResultSet resultSet = statement.executeQuery("SELECT * FROM \"" + tableName + "\";")) {
						// get column names
						columnNames = new LinkedList<String>();
						final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
						final int columnCount = resultSetMetaData.getColumnCount();
						for(int i = 1; i <=  columnCount; i++) {
							columnNames.add(resultSetMetaData.getColumnName(i));
						}
						// get query data
						if (resultSet.isBeforeFirst()) 
							queryData = new LinkedList<List<String>>();
						while(resultSet.next()) {
							final List<String> rowData = new LinkedList<String>();
							for (int i = 1; i <= columnCount; i++) {
								rowData.add(resultSet.getString(i));
							}
							queryData.add(rowData);
						}
					} 
					allData.put(tableName, new QueryResult(columnNames, queryData));
				}
			}
		} 
		return allData;
	}
	
	public QueryResult getQueryResult(String schema, String query, boolean dev) throws SQLException {
		QueryResult queryResult = null;
		try (final Connection connection = dev ? userDataSource.getConnection() : readUserDataSource.getConnection()) {
			
				try (final Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
					queryResult = getQueryResult(schema, query, connection, statement);
				}
		}
		return queryResult;
	}
	
	public QueryResult getQueryResult(String schema, String query, Connection connection, Statement statement) throws SQLException {
		QueryResult queryResult = null;
		
		connection.setAutoCommit(false);
			
		statement.setFetchSize(1000);
		statement.execute("SET search_path TO '" + schema + "'");
		statement.execute("SET statement_timeout TO " + (QUERY_TIMEOUT_SECONDS * 1000));
			
		final long queryStart = System.currentTimeMillis();
		try (final ResultSet resultSet = statement.executeQuery(query)) {
			final long queryEnd = System.currentTimeMillis();
			queryResult = new QueryResult();
			queryResult.setExecutionTime(queryEnd - queryStart);
			// get column names
			List<String> columnNames = new LinkedList<String>();
			final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			final int columnCount = resultSetMetaData.getColumnCount();
			for(int i = 1; i <=  columnCount; i++) {
				columnNames.add(resultSetMetaData.getColumnName(i));
			}
			queryResult.setColumns(columnNames);
			// get query data
			int rows = 0;
			List<List<String>> queryData = null;
			if (resultSet.isBeforeFirst())
				queryData = new LinkedList<List<String>>();
			while (resultSet.next()) {
				// stop reading rows from the driver at all when the limit is reached
				if (rows >= QueryResult.QUERY_READ_LIMIT) {
					log.warn("Query exceeded max result: {}", query);
					queryResult.setReadLimitExceeded(true);
					break;
				}

				++rows;

				// stop adding to the QueryResult after a certain number of rows
				if (rows <= QueryResult.QUERY_SIZE_LIMIT) {
					List<String> rowData = new LinkedList<String>();
					for (int i = 1; i <= columnCount; i++) {
						rowData.add(resultSet.getString(i));
					}
					queryData.add(rowData);
				}
			}
			queryResult.setData(queryData);
			
			if (rows > QueryResult.QUERY_SIZE_LIMIT)
				queryResult.setTruncated(true);
			
			queryResult.setOriginalSize(rows);
			
			queryResult.setTotalTime(System.currentTimeMillis() - queryStart);
		}
		return queryResult;
	}

	public QueryResult getDevQueryResult(String query, boolean dev) throws SQLException {
		QueryResult queryResult = null;
		try (final Connection connection = dataSource.getConnection()) {
			
			try (final Statement statement = connection.createStatement()) {
				statement.execute("SET search_path TO 'public'");
				statement.execute("SET statement_timeout TO " + (QUERY_TIMEOUT_SECONDS * 1000));
				
				try (final ResultSet resultSet = statement.executeQuery(query)) {
					queryResult = new QueryResult();
					// get columns
					List<String> columnNames = new LinkedList<String>();
					final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
					final int columnCount = resultSetMetaData.getColumnCount();
					for(int i = 1; i <=  columnCount; i++) {
						columnNames.add(resultSetMetaData.getColumnName(i));
					}
					queryResult.setColumns(columnNames);
					// get data
					List<List<String>> queryData = null;
					if (resultSet.isBeforeFirst())
						queryData = new LinkedList<List<String>>();
					while (resultSet.next()) {
						final List<String> rowData = new LinkedList<String>();
						for (int i = 1; i <= columnCount; i++) {
							rowData.add(resultSet.getString(i));
						}
						queryData.add(rowData);
					}
					queryResult.setData(queryData);
				}
			}
		} 
		return queryResult;
	}

	public void registerUser(String hashedEmail, String email, String password) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {
			// Add user to the user table
			try {
				// generate the user's encryption salt and password
				final byte[] salt = SaltHasher.generateSalt();
				final byte[] encryptedPassword = SaltHasher.getEncryptedValue(password, salt);
	
				try (final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO \"user\" (\"password\", \"salt\", \"email\", \"email_plain\") "
						+ "VALUES (?, ?, ?, ?)")) {
					preparedStatement.setString(1, Arrays.toString(encryptedPassword));
					preparedStatement.setString(2, Arrays.toString(salt));
					preparedStatement.setString(3, hashedEmail);
					preparedStatement.setString(4, email);
					preparedStatement.executeUpdate();
				}
			} catch (NoSuchAlgorithmException e) {
				// TODO: 
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO: 
				e.printStackTrace();
			}
			// add user to linked_admin_codes table
			try (final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO linked_admin_codes (\"email\") VALUES (?)")) {
				preparedStatement.setString(1, hashedEmail);
				preparedStatement.executeUpdate();
			}
		} 
	}
	
	public List<String> getLinkedTutorials(String hashedEmail, String adminCode) throws SQLException {
		List<String> linkedTutorials = null;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT schema FROM schema_options WHERE (visible_to_users AND "
					+ "(open_access IS NULL OR open_access <= now()) AND (close_access IS NULL OR close_access >= now()) AND admin_code = ?) "
					+ "OR admin_code = (SELECT admin_code FROM \"user\" WHERE email = ?);")) {
				preparedStatement.setString(1, adminCode);
				preparedStatement.setString(2, hashedEmail);
				
				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.isBeforeFirst())
						linkedTutorials = new LinkedList<String>();
					while (resultSet.next()) 
						linkedTutorials.add(resultSet.getString(1));
				}
			}
		} 
		return linkedTutorials;
	}

	public void deleteUser(String email, String adminCode) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM \"user\" WHERE email = ?")) {
				preparedStatement.setString(1, email);
				preparedStatement.executeUpdate();
			} 
			
			try (final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM linked_admin_codes WHERE linked_admin_code = ?")) {
				preparedStatement.setString(1, adminCode);
				preparedStatement.executeUpdate();
			}
		} 
	}

	/**
	 * Returns whether the given email is registered.
	 *
	 * @param hashedEmail        email to check
	 * @return        whether the email is registered
	 * @throws SQLException
	 */
	public boolean isEmailRegistered(String hashedEmail, String email) throws SQLException {
		boolean isEmailRegistered = false;
		try (final Connection connection = dataSource.getConnection()) {
			
			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM \"user\" WHERE \"email\" = ?")) {
				preparedStatement.setString(1, hashedEmail);
	
				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next())
						isEmailRegistered = true;
				}
			}

			boolean hasPlainTextEmail = false;
			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM \"user\" WHERE \"email\" = ? AND \"email_plain\" != NULL")) {
				preparedStatement.setString(1, hashedEmail);

				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next())
						hasPlainTextEmail = true;
				}
			}

			if(isEmailRegistered && !hasPlainTextEmail) {
				try (final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE \"user\" SET \"email_plain\" = ? WHERE \"email\" = ?")) {
					preparedStatement.setString(1, email);
					preparedStatement.setString(2, hashedEmail);
					preparedStatement.executeUpdate();
				}
			}
		} 
		return isEmailRegistered;
	}

	public boolean isPasswordCorrect(String email, String attemptedPassword) throws SQLException {
		boolean isCorrect = false;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT \"salt\", \"password\" FROM \"user\" WHERE \"email\" = ?")) {
			preparedStatement.setString(1, email);

				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						byte[] salt = stringByteArrayToByteArray(resultSet.getString(1));
						byte[] encryptedPassword = stringByteArrayToByteArray(resultSet.getString(2));
						// use the password hasher to authenticate
						isCorrect = SaltHasher.authenticate(attemptedPassword, encryptedPassword, salt);
					}
				} catch (NoSuchAlgorithmException e) {
					// TODO: 
					e.printStackTrace();
				} catch (InvalidKeySpecException e) {
					// TODO: 
					e.printStackTrace();
				}
			}
		}
		return isCorrect;
	}

	public void log(String sessionId, String email, String schemaName, String question, String correctAnswer, String userQuery, boolean parsed, boolean correct, String nlpFeedback,
			double totalTimeSeconds, double queryTotalTimeSeconds, double answerTotalTimeSeconds, double queryExecutionTimeSeconds, double answerExecutionTimeSeconds, boolean truncated, 
			boolean readLimitExceeded, int originalSize, String schemaAdminCode) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO \"log\" (\"session_id\", "
					+ "\"email\", \"schema\", \"question\", \"correct_answer\", \"query\", "
					+ "\"parsed\", \"correct\", \"nlp_feedback\", \"total_time_seconds\", "
					+ "\"query_total_time_seconds\", \"answer_total_time_seconds\", \"query_execution_time_seconds\", "
					+ "\"answer_execution_time_seconds\", \"truncated\", "
					+ "\"read_limit_exceeded\", \"original_size\", \"schema_admin_code\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				preparedStatement.setString(1, sessionId);
				preparedStatement.setString(2, email);
				preparedStatement.setString(3, schemaName);
				preparedStatement.setString(4, question);
				preparedStatement.setString(5, correctAnswer);
				preparedStatement.setString(6, userQuery);
				preparedStatement.setBoolean(7, parsed);
				preparedStatement.setBoolean(8, correct);
				preparedStatement.setString(9, nlpFeedback);
				preparedStatement.setDouble(10, totalTimeSeconds);
				preparedStatement.setDouble(11, queryTotalTimeSeconds);
				preparedStatement.setDouble(12, answerTotalTimeSeconds);
				preparedStatement.setDouble(13, queryExecutionTimeSeconds);
				preparedStatement.setDouble(14, answerExecutionTimeSeconds);
				preparedStatement.setBoolean(15, truncated);
				preparedStatement.setBoolean(16, readLimitExceeded);
				preparedStatement.setInt(17, originalSize);
				preparedStatement.setString(18, schemaAdminCode);
				preparedStatement.executeUpdate();
			}
		} 
	}

	public void logQuestionPresentation(String sessionId, String email, String schemaName, String question, String schemaAdminCode) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO \"log_question_presentation\" (\"session_id\", "
					+ "\"email\", \"schema\", \"question\", \"schema_admin_code\") VALUES (?, ?, ?, ?, ?)")) {
				preparedStatement.setString(1, sessionId);
				preparedStatement.setString(2, email);
				preparedStatement.setString(3, schemaName);
				preparedStatement.setString(4, question);
				preparedStatement.setString(5, schemaAdminCode);
				preparedStatement.executeUpdate();
			}
		} 
	}

	public void logException(String sessionId, String email, String exception) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO \"log_exceptions\" (\"session_id\", "
					+ "\"email\", \"exception\") VALUES (?, ?, ?)")) {
				preparedStatement.setString(1, sessionId);
				preparedStatement.setString(2, email != null ? email : "not logged in");
				preparedStatement.setString(3, exception);
				preparedStatement.executeUpdate();
			}
		} 
	}

	public String getAdminCode(String email) throws SQLException {
		String adminCode = null;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT \"admin_code\" FROM \"user\" WHERE \"email\" = ?;")) {
				preparedStatement.setString(1, email);
				preparedStatement.execute();

				try (final ResultSet resultSet = preparedStatement.getResultSet()) {
					if (resultSet.next())
						adminCode = resultSet.getString(1);
				}
			}
		} 
		return adminCode;
	}

	public List<String> getLinkedAdminCodes(String email) throws SQLException {
		List<String> linkedAdminCodes = null;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT \"linked_admin_code\" FROM \"linked_admin_codes\" WHERE \"email\" = ?;")) {
				preparedStatement.setString(1, email);
				preparedStatement.execute();
	
				try (final ResultSet resultSet = preparedStatement.getResultSet()) {
					if (resultSet.isBeforeFirst())
						linkedAdminCodes = new LinkedList<String>();
					while (resultSet.next())
						linkedAdminCodes.add(resultSet.getString(1));
				}
			}
		} 
		return linkedAdminCodes;
	}
	
	public boolean adminCodeExists(String adminCode) throws SQLException {
		boolean adminCodeExists = false;
		try (final Connection connection = dataSource.getConnection()) {
			try (final Statement statement = connection.createStatement()) {
				try(final ResultSet resultSet = statement.executeQuery("SELECT 1 FROM \"user\" WHERE admin_code = '" + adminCode + "';")) {
					if (resultSet.next())
						adminCodeExists = true;
				} 
			} 
		}
		return adminCodeExists;
	}
	
	public void promoteUserToAdmin(String email) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {
			String adminCode = null;
			try (final Statement statement = connection.createStatement()) {
				final Random random = new SecureRandom();
				final byte[] buffer = new byte[5];
				boolean adminCodeExists = false;
				do {
					random.nextBytes(buffer);
					adminCode = BaseEncoding.base64Url().omitPadding().encode(buffer);
					
					try(final ResultSet resultSet = statement.executeQuery("SELECT 1 FROM \"user\" WHERE admin_code = '" + adminCode + "';")) {
						if (resultSet.next())
							adminCodeExists = true;
					} 
				} while(adminCodeExists);
			} 

			try (final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE \"user\" SET \"admin\" = ?, \"admin_code\" = ? WHERE email = ?;")) {
				preparedStatement.setBoolean(1, true);
				preparedStatement.setString(2, adminCode);
				preparedStatement.setString(3, email);
				preparedStatement.execute();
			}
			
			try (final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO linked_admin_codes (email, linked_admin_code) VALUES (?, ?)")) {
				preparedStatement.setString(1, email);
				preparedStatement.setString(2, adminCode);
				preparedStatement.executeUpdate();
			}
		}
	}

	public void promoteUserToDev(String email) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE \"user\" SET \"developer\" = ? WHERE email = ?;")) {		
				preparedStatement.setBoolean(1, true);
				preparedStatement.setString(2, email);
				preparedStatement.execute();
			}
		} 
	}

	public void demoteUserFromAdmin(String email, String adminCode) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE \"user\" SET \"admin\" = ?, \"admin_code\" = ? WHERE email = ?;")) {
				preparedStatement.setBoolean(1, false);
				preparedStatement.setString(2, null);
				preparedStatement.setString(3, email);
				preparedStatement.execute();
			}
			
			try (final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM linked_admin_codes WHERE linked_admin_code = ?")) {
				preparedStatement.setString(1, adminCode);
				preparedStatement.execute();
			} 
		} 
	}

	public void demoteUserFromDev(String email) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE \"user\" SET \"developer\" = ? WHERE email = ?;")) {
				preparedStatement.setBoolean(1, false);
				preparedStatement.setString(2, email);
				preparedStatement.execute();
			}
		}
	}

	public boolean isDeveloper(String email) throws SQLException {
		boolean isDeveloper = false;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT developer FROM \"user\" WHERE email = ?;")) {
				preparedStatement.setString(1, email);

				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next())
						isDeveloper = resultSet.getBoolean(1);
				}
			}
		}
		return isDeveloper;
	}

	public void linkAdminCode(String email, String adminCode) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO \"linked_admin_codes\" (\"email\", \"linked_admin_code\") VALUES (?, ?)")) {
				preparedStatement.setString(1, email);
				preparedStatement.setString(2, adminCode);
				preparedStatement.executeUpdate();
			}
		} 
	}

	public void unlinkAdminCode(String email, String adminCode) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM linked_admin_codes WHERE email = ? AND linked_admin_code = ?")) {
				preparedStatement.setString(1, email);
				preparedStatement.setString(2, adminCode);
				preparedStatement.executeUpdate();
			}
		} 
	}

	private byte[] stringByteArrayToByteArray(String stringByteArray) {
		final String[] byteValues = stringByteArray.substring(1, stringByteArray.length() - 1).split(",");
		final byte[] bytes = new byte[byteValues.length];

		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = Byte.valueOf(byteValues[i].trim());    
		}

		return bytes;
	}

	public String getUserSchema(String schemaLink) throws SQLException {
		String schemaName = null;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT admin_code, schema FROM schema_options WHERE link = ?")) {
				preparedStatement.setString(1, schemaLink);
				
				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next())
						schemaName = resultSet.getString(1) + "_" + resultSet.getString(2);
				}
			}
		} 
		return schemaName;
	}
	
	public int getMostRecentlyPresentedQuestion(String hashedEmail, String schema, String schemaAdminCode) throws SQLException {
		int mostRecentlyPresentedQuestion = 0;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT s.order FROM log_question_presentation p, "
					+ "schema_questions s WHERE p.question = s.question AND p.schema = s.schema AND p.email = ? AND p.schema = ? AND schema_admin_code = ?"
					+ " GROUP BY s.order ORDER BY max(timestamp) DESC LIMIT 1")) {
				preparedStatement.setString(1, hashedEmail);
				preparedStatement.setString(2, schema);
				preparedStatement.setString(3, schemaAdminCode);

				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next())
						mostRecentlyPresentedQuestion = resultSet.getInt(1);
				}
			}
		} 
		return mostRecentlyPresentedQuestion;
	}
	
	public int getNumberOfAttempts(String hashedEmail, String schema, String question, boolean parsed, boolean correct, String schemaAdminCode) throws SQLException {
		int numberOfParsedIncorrectAttempts = 0;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT count(*) FROM log "
					+ "WHERE email = ? AND question = ? AND schema = ? AND correct = ? AND parsed = ? AND schema_admin_code = ?")) {
				preparedStatement.setString(1, hashedEmail);
				preparedStatement.setString(2, question);
				preparedStatement.setString(3, schema);
				preparedStatement.setBoolean(4, correct);
				preparedStatement.setBoolean(5, parsed);
				preparedStatement.setString(6, schemaAdminCode);

				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next())
						numberOfParsedIncorrectAttempts = resultSet.getInt(1);
				}
			}
		}
		return numberOfParsedIncorrectAttempts;
	}
	
	public List<String> getQuestionComments(String schema, int order, String schemaAdminCode) throws SQLException {
		List<String> comments = null;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT comment FROM schema_questions_comment "
					+ "WHERE schema = ? AND \"order\" = ? AND admin_code = ?")) {
				preparedStatement.setString(1, schema);
				preparedStatement.setInt(2, order);
				preparedStatement.setString(3, schemaAdminCode);
	
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.isBeforeFirst())
						comments = new LinkedList<String>();
					while (resultSet.next())
						comments.add(resultSet.getString(1));
				} 
			}
		}
		return comments;
	}
	
	public List<UserQuery> getUserQueriesMarkedForReview(String schema, String schemaAdminCode) throws SQLException {
		List<UserQuery> userQueries = null;
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT email, query, schema, question, "
					+ "correct_answer, \"order\" \"timestamp\", parsed, correct, read_limit_exceeded FROM log WHERE schema = ? AND "
					+ "marked_for_review AND schema_admin_code = ?")) {
				preparedStatement.setString(1, schema);
				preparedStatement.setString(2, schemaAdminCode);
			
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.isBeforeFirst())
						userQueries = new LinkedList<UserQuery>();
					final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
					if (resultSet.next()) {
						final UserQuery userQuery = new UserQuery(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5), resultSet.getInt(6), resultSet.getTimestamp(7, cal), resultSet.getBoolean(8), resultSet.getBoolean(9), resultSet.getBoolean(10));
						userQueries.add(userQuery);
					}
				}
			}
		}
		return userQueries;
	}
	
	public void addComment(String schemaName, int order, String comment, String schemaAdminCode) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO schema_questions_comment "
					+ "(schema, \"order\", comment, admin_code) VALUES (?, ?, ?, ?);")) {
				preparedStatement.setString(1, schemaName);
				preparedStatement.setInt(2, order);
				preparedStatement.setString(3, comment);
				preparedStatement.setString(4, schemaAdminCode);
				preparedStatement.executeUpdate();
			}
		}
	}
	
	public void markForReview(String email) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {

			try (final PreparedStatement preparedStatement = connection.prepareStatement(
					"UPDATE log SET marked_for_review = true WHERE email = ? AND \"timestamp\" = (SELECT MAX(\"timestamp\") FROM log WHERE email = ?);")) {
				preparedStatement.setString(1, email);
				preparedStatement.setString(2, email);
				preparedStatement.executeUpdate();
			}
		}
	}
}
