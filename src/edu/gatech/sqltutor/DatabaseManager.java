package edu.gatech.sqltutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
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
import java.util.HashMap;
import java.util.List;
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

import objects.DatabaseTable;
import objects.QueryResult;
import objects.QuestionTuple;
import utilities.PasswordHasher;
import utilities.ScriptRunner;
import edu.gatech.sqltutor.entities.UserQuery;

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
		Connection conn = dataSource.getConnection();

		ArrayList<String> schemas = new ArrayList<String>();
		
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getSchemas();
		while( rs.next() ) {
			String schema = rs.getString(1);
			schemas.add(schema);
		}
		
		Utils.tryClose(rs);
		Utils.tryClose(conn);
		
		return schemas;
	}
	
	public boolean isAdmin(String email) throws SQLException {
		Connection connection = dataSource.getConnection();

		final String query = "SELECT admin FROM \"user\" WHERE email = ?;";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, email);
		ResultSet resultSet = preparedStatement.executeQuery();
		
		boolean isAdmin = false;
		if (resultSet.next()) {
			isAdmin = resultSet.getBoolean(1);
		}
		
		Utils.tryClose(preparedStatement);
		Utils.tryClose(resultSet);
		Utils.tryClose(connection);
		
		return isAdmin;
	}
	
	public List<String> getUserSchemas(boolean dev) throws SQLException {
		Connection conn = dataSource.getConnection();
		
		String query = dev ? "SELECT schema FROM schema_options;" : 
			"SELECT schema FROM schema_options WHERE visible_to_users;";
		
		Statement statement = conn.createStatement();
		statement.execute(query);
		
		ResultSet resultSet = statement.getResultSet();
		

		ArrayList<String> schemas = new ArrayList<String>();
		while(resultSet.next()) {
			schemas.add(resultSet.getString(1));
		}
		
		Utils.tryClose(resultSet);
		Utils.tryClose(statement);
		Utils.tryClose(conn);
		
		return schemas;

	}
	
	public HashMap<String, Boolean> getOptions(String schemaName) throws SQLException {
		Connection conn = dataSource.getConnection();
		
		Statement statement = conn.createStatement();
		statement.execute("SELECT visible_to_users, in_order_questions FROM schema_options WHERE schema = '" + schemaName +"'");
		
		ResultSet rs = statement.getResultSet();
		rs.next();
		
		HashMap<String, Boolean> options = new HashMap<String, Boolean>();
		options.put("visible_to_users", rs.getBoolean(1));
		options.put("in_order_questions", rs.getBoolean(2));

		Utils.tryClose(rs);
		Utils.tryClose(statement);
		Utils.tryClose(conn);
		
		return options;
	}
	
	public void deleteSchema(String schemaName) throws SQLException {
		Connection conn = userDataSource.getConnection();
		
		Statement statement = conn.createStatement();
		statement.execute("DROP SCHEMA " + schemaName +" CASCADE;");
		Utils.tryClose(statement);
		Utils.tryClose(conn);
		
		conn = dataSource.getConnection();
		
		statement = conn.createStatement();
		statement.addBatch("DELETE FROM schema_options WHERE schema = '" + schemaName + "';");
		statement.addBatch("DELETE FROM schema_questions WHERE schema = '" + schemaName + "';");
		statement.executeBatch();
		
		Utils.tryClose(statement);
		Utils.tryClose(conn);
	}
	
	public boolean checkSchemaPermissions(String email, String schemaName) throws SQLException {
		Connection connection = dataSource.getConnection();

		final String query = "SELECT 1 FROM schema_options WHERE schema = ? AND owner = ?;";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, schemaName);
		preparedStatement.setString(2, email);
		ResultSet resultSet = preparedStatement.executeQuery();
		
		boolean schemaPermissions = false;
		if (resultSet.next()) {
			schemaPermissions = true;
		}
		
		Utils.tryClose(preparedStatement);
		Utils.tryClose(resultSet);
		Utils.tryClose(connection);
			
		return schemaPermissions;
	}
	
	public void addQuestion(String schemaName, String question, String answer) throws SQLException {
		Connection conn = dataSource.getConnection();
		
		Statement statement = conn.createStatement();
		statement.execute("SELECT max(\"order\") FROM schema_questions");
		
		ResultSet rs = statement.getResultSet();
		int order = 1;
		if(rs.next())
			order = rs.getInt(1) + 1;
		
		Utils.tryClose(rs);
		Utils.tryClose(statement);
		
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO schema_questions (schema, question, answer, \"order\") "
				+ "VALUES (?, ?, ?, ?);");
		ps.setString(1, schemaName);
		ps.setString(2, question);
		ps.setString(3, answer);
		ps.setInt(4, order);
		ps.executeUpdate();
		Utils.tryClose(ps);
		
		Utils.tryClose(conn);
	}
	
	public List<QuestionTuple> getQuestions(String schemaName) throws SQLException {
		Connection conn = dataSource.getConnection();
		
		Statement statement = conn.createStatement();
		statement.execute("SELECT \"order\", question, answer, id FROM "
				+ "schema_questions WHERE schema = '" + schemaName + 
				"' ORDER BY \"order\";");
		
		ResultSet rs = statement.getResultSet();
		List<QuestionTuple> questions = new ArrayList<QuestionTuple>();
		while(rs.next()) {
			questions.add(new QuestionTuple(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)));
		}

		Utils.tryClose(statement);
		Utils.tryClose(conn);
		Utils.tryClose(rs);
		
		return questions;
	}
	
	public void setOptions(String schemaName, boolean visibleToUsers, boolean inOrderQuestions) throws SQLException {
		Connection conn = dataSource.getConnection();
		
		Statement statement = conn.createStatement();
		statement.execute("UPDATE schema_options SET visible_to_users = " + visibleToUsers + ", in_order_questions = " + inOrderQuestions + " WHERE schema = '" + schemaName + "';");

		Utils.tryClose(statement);
		Utils.tryClose(conn);
	}
	
	public String addSchema(String schemaDump, String email) 
			throws SQLException, IOException, IllegalArgumentException {
		
		if(schemaDump == null || schemaDump.length() == 0) {
			throw new IllegalArgumentException("Schema file is null or empty.");
		}
		
		Connection conn = userDataSource.getConnection();

		ScriptRunner runner = new ScriptRunner(conn, false, true);
		BufferedReader reader = new BufferedReader(new StringReader(schemaDump));
		runner.runScript(reader);
		Utils.tryClose(reader);

		// get schema name
		Pattern p = Pattern.compile("(?<=CREATE SCHEMA\\W{1,2})(\\w+)");
		Matcher m = p.matcher(schemaDump);
		m.find();
		String schemaName = m.group(1);
		
		Statement statement = conn.createStatement();
		statement.addBatch("GRANT SELECT ON ALL TABLES IN SCHEMA " + schemaName + " TO readonly_user;");
		statement.addBatch("GRANT USAGE ON SCHEMA " + schemaName + " TO readonly_user;");
		statement.executeBatch();
		
		Utils.tryClose(statement);
		Utils.tryClose(conn);
		
		conn = dataSource.getConnection();
		statement = conn.createStatement();
		statement.execute("INSERT INTO schema_options (schema, owner) VALUES ('"+schemaName+"', '"+email+"');");
		
		Utils.tryClose(statement);
		Utils.tryClose(conn);

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
		Connection conn = dataSource.getConnection();
		
		Statement statement = conn.createStatement();
		for(int i = 0; i < questions.size(); i++) {
			int id = questions.get(i).getId();
			statement.addBatch("UPDATE schema_questions SET \"order\" = " 
					+ (i+1) + " WHERE id = " + id + ";");
		}
		statement.executeBatch();

		Utils.tryClose(statement);
		Utils.tryClose(conn);
	}
	
	public void deleteQuestions(List<QuestionTuple> questions) throws SQLException {
		Connection conn = dataSource.getConnection();

		Statement statement = conn.createStatement();
		for(int i = 0; i < questions.size(); i++) {
			int id = questions.get(i).getId();
			statement.addBatch("DELETE FROM schema_questions WHERE id = " + id + ";");
		}
		statement.executeBatch();

		Utils.tryClose(statement);
		Utils.tryClose(conn);
	}
	
	
	public void addPasswordChangeRequest(String email, UUID uuid) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		Connection connection = dataSource.getConnection();
		
		// generate the user's encryption salt and password
		byte[] salt = PasswordHasher.generateSalt();
		byte[] encryptedId = PasswordHasher.getEncryptedPassword(uuid.toString(), salt);
		
		final String update = "INSERT INTO \"password_change_requests\" (\"email\", \"id\", \"salt\") VALUES (?, ?, ?)";
		PreparedStatement preparedStatement = connection.prepareStatement(update);
		preparedStatement.setString(1, email);
		preparedStatement.setBytes(2, encryptedId);
		preparedStatement.setBytes(3, salt);
		preparedStatement.executeUpdate();

		Utils.tryClose(preparedStatement);
		Utils.tryClose(connection);
	}
	
	public boolean getPasswordChangeRequest(String email, String id) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		Connection connection = dataSource.getConnection();

		// Get the salt and id of the most recent request
		// that belongs to the user within the last 24 hours
		final String query = "SELECT \"salt\", \"id\" FROM \"password_change_requests\" WHERE "
				+ "\"time\" = (SELECT MAX(\"time\") FROM \"password_change_requests\" WHERE \"email\" = ? AND "
				+ "\"time\" >= (now() - '1 day'::INTERVAL));";
		
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, email);
		ResultSet resultSet = preparedStatement.executeQuery();
		
		boolean autheticated = false;
		if (resultSet.next()) {
			byte[] salt = resultSet.getBytes(1);
			byte[] encryptedId = resultSet.getBytes(2);
			
			// use the password hasher to authenticate
			autheticated = PasswordHasher.authenticate(id, encryptedId, salt);
		} 
		
		Utils.tryClose(preparedStatement);
		Utils.tryClose(connection);
		Utils.tryClose(resultSet);

		return autheticated;
	}

	public void changePassword(String email, String newPassword) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		Connection conn = dataSource.getConnection();

		// generate the user's encryption salt and password
		byte[] salt = PasswordHasher.generateSalt();
		byte[] encryptedPassword = null;
		encryptedPassword = PasswordHasher.getEncryptedPassword(newPassword, salt);
		
		PreparedStatement statement = conn.prepareStatement("UPDATE \"user\" SET \"password\" = ?, \"salt\" = ? WHERE email = ?;");
		statement.setBytes(1, encryptedPassword);
		statement.setBytes(2, salt);
		statement.setString(3, email);
		statement.execute();

		Utils.tryClose(statement);
		Utils.tryClose(conn);
	}

	public boolean emailExists(String email) throws SQLException {
		Connection conn = dataSource.getConnection();
		
		PreparedStatement statement = conn.prepareStatement("SELECT 1 FROM \"user\" WHERE email = ?;");
		statement.setString(1, email);
		statement.execute();
		
		boolean hasResults = false;
		ResultSet rs = statement.getResultSet();
		if(rs.next()) {
			hasResults = true;
		}

		Utils.tryClose(statement);
		Utils.tryClose(conn);
		return hasResults;
	}
	
	public void saveUserQuery(UserQuery query) throws SQLException {
		Connection conn = dataSource.getConnection();
		
		Long id = query.getId();
		PreparedStatement statement;
		if( id == null ) {
			final String INSERT = "INSERT INTO query " + 
				"(email, schema, sql, user_description, source, created) VALUES (?, ?, ?, ?, ?, ?)";
			statement = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
		} else {
			final String UPDATE = 
					"UPDATE query SET email=?, schema=?, sql=?, user_description=?, source=?, created=? WHERE id=?";
			statement = conn.prepareStatement(UPDATE);
			statement.setLong(7, id);
		}
		statement.setString(1, query.getEmail());
		statement.setString(2, query.getSchema());
		statement.setString(3, query.getQuery());
		statement.setString(4, query.getUserDescription());
		statement.setString(5, query.getSource());
		statement.setDate(6, new Date(query.getTime().getTime()));

		statement.executeUpdate();
		
		if( id == null ) {
			// get the generated id for inserts
			ResultSet keySet = statement.getGeneratedKeys();
			if( !keySet.next() )
				throw new IllegalStateException("No id generated for query.");
			query.setId(keySet.getLong(1));
			Utils.tryClose(keySet);
		}

		Utils.tryClose(statement);
		Utils.tryClose(conn);
	}
	
	public HashMap<String, QueryResult> getAllData(String schemaName, List<String> tables) throws SQLException {
		Connection connection = userDataSource.getConnection();
		Statement statement = connection.createStatement();
		statement.execute("set search_path to '" + schemaName + "'");
		
		HashMap<String, QueryResult> allData = new HashMap<String, QueryResult>();
		for(String tableName : tables) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM \"" + tableName + "\";");
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
			Utils.tryClose(resultSet);
		} 
		
		Utils.tryClose(statement);
		Utils.tryClose(connection);
		
		return allData;
	}
	
	public void verifyQuery(String schemaName, String query) throws SQLException {
		Connection connection = userDataSource.getConnection();
		
		Statement statement = connection.createStatement();
		statement.execute("set search_path to '" + schemaName + "'");
		statement.executeQuery(query);
		
		Utils.tryClose(statement);
		Utils.tryClose(connection);
	}
	
	public QueryResult getQueryResult(String schemaName, String query, boolean dev) throws SQLException {
		Connection connection = dev ? userDataSource.getConnection() : readUserDataSource.getConnection();
		
		Statement statement = connection.createStatement();
		statement.execute("set search_path to '" + schemaName + "'");
		
		ResultSet resultSet = statement.executeQuery(query);
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
		Utils.tryClose(resultSet);
		Utils.tryClose(connection);
		Utils.tryClose(statement);
		
		return new QueryResult(columnNames, queryData);
	}
	
	public void registerUser(String email, String password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		Connection connection = dataSource.getConnection();

		final String update = "INSERT INTO \"user\" (\"password\", \"salt\", \"email\") VALUES (?, ?, ?)";
		PreparedStatement preparedStatement = connection.prepareStatement(update);
		
		// generate the user's encryption salt and password
		byte[] salt = PasswordHasher.generateSalt();
		byte[] encryptedPassword = PasswordHasher.getEncryptedPassword(password, salt);
		
		preparedStatement.setBytes(1, encryptedPassword);
		preparedStatement.setBytes(2, salt);
		preparedStatement.setString(3, email);
		preparedStatement.executeUpdate();

		Utils.tryClose(preparedStatement);
		Utils.tryClose(connection);
	}
	
	public boolean isUsernameRegistered(String email) throws SQLException {
		Connection connection = dataSource.getConnection();

		final String query = "SELECT 1 FROM \"user\" WHERE \"email\" = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, email);
		
		ResultSet resultSet = preparedStatement.executeQuery();
		
		boolean hasResults = false;
		if (resultSet.next()) {
			hasResults = true;
		}
		
		Utils.tryClose(preparedStatement);
		Utils.tryClose(connection);
		Utils.tryClose(resultSet);
		return hasResults;
	}
	
	public boolean isPasswordCorrect(String email, String attemptedPassword) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {

		Connection connection = dataSource.getConnection();

		// get the user's encryption salt
		String query = "SELECT \"salt\", \"password\" FROM \"user\" WHERE \"email\" = ?";
	
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, email);
		
		ResultSet resultSet = preparedStatement.executeQuery();
		
		boolean isCorrect = false;
		if (resultSet.next()) {
			byte[] salt = resultSet.getBytes(1);
			byte[] encryptedPassword = resultSet.getBytes(2);
			
			// use the password hasher to authenticate
			isCorrect = PasswordHasher.authenticate(attemptedPassword, encryptedPassword, salt);
		} 

		Utils.tryClose(preparedStatement);
		Utils.tryClose(connection);
		Utils.tryClose(resultSet);
	
		return isCorrect;
	}
	
	public void log(String sessionId, String ipAddress, String email, String schemaName, String question, String correctAnswer, String userQuery, boolean parsed, boolean correct) throws SQLException {
		Connection connection = dataSource.getConnection();

		final String update = "INSERT INTO \"log\" (\"session_id\", \"ip_address\", \"email\", \"schema\", \"question\", \"correct_answer\", \"query\", \"parsed\", \"correct\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement preparedStatement = connection.prepareStatement(update);
		preparedStatement.setString(1, sessionId);
		preparedStatement.setString(2, ipAddress);
		preparedStatement.setString(3, email);
		preparedStatement.setString(4, schemaName);
		preparedStatement.setString(5, question);
		preparedStatement.setString(6, correctAnswer);
		preparedStatement.setString(7, userQuery);
		preparedStatement.setBoolean(8, parsed);
		preparedStatement.setBoolean(9, correct);
		preparedStatement.executeUpdate();

		Utils.tryClose(preparedStatement);
		Utils.tryClose(connection);
	}
	
	public List<UserQuery> getUserQueries(String schemaName) throws SQLException {
		Connection conn = dataSource.getConnection();

		final String SELECT = 
				"SELECT * FROM query WHERE schema='" + schemaName + "'";
		Statement statement = conn.createStatement();
		
		ResultSet result = statement.executeQuery(SELECT);
		
		List<UserQuery> userQueries = new ArrayList<UserQuery>();
		while( result.next() ) {
			UserQuery query = new UserQuery();
			query.setId(result.getLong("id"));
			// FIXME the bean?
			query.setEmail(result.getString("email"));
			query.setSchema(result.getString("schema"));
			query.setQuery(result.getString("sql"));
			query.setUserDescription(result.getString("user_description"));
			query.setTime(result.getDate("created"));
			query.setSource(result.getString("source"));
			
			userQueries.add(query);
		}
		
		Utils.tryClose(result);
		Utils.tryClose(statement);
		Utils.tryClose(conn);
		
		return userQueries;
	}
	
	public List<UserQuery> getUserQueries() throws SQLException {
		Connection conn = dataSource.getConnection();
		Statement statement = null;

		final String SELECT = 
				"SELECT * FROM query";
		statement = conn.createStatement();
		ResultSet result = statement.executeQuery(SELECT);
		
		List<UserQuery> userQueries = new ArrayList<UserQuery>();
		while( result.next() ) {
			UserQuery query = new UserQuery();
			query.setId(result.getLong("id"));
			// FIXME the bean?
			query.setEmail(result.getString("email"));
			query.setSchema(result.getString("schema"));
			query.setQuery(result.getString("sql"));
			query.setUserDescription(result.getString("user_description"));
			query.setTime(result.getDate("created"));
			query.setSource(result.getString("source"));
			
			userQueries.add(query);
		}
		
		Utils.tryClose(result);
		Utils.tryClose(statement);
		Utils.tryClose(conn);
		return userQueries;
	}
	
	public List<DatabaseTable> getTables(String schemaName) throws SQLException {
		Connection connection = userDataSource.getConnection();

		DatabaseMetaData metadata = connection.getMetaData();
		
		ResultSet resultSet = metadata.getTables(null, schemaName, "%", new String[] {"TABLE"});
		ArrayList<DatabaseTable> tables = new ArrayList<DatabaseTable>();
		while(resultSet.next()) {
			// the API tells us the third element is the TABLE_NAME string.
			tables.add(new DatabaseTable(resultSet.getString(3)));
		}
		Utils.tryClose(resultSet);
		
		for(int i=0; i < tables.size(); i++) {
			resultSet = metadata.getColumns(null, schemaName, tables.get(i).getTableName(), null);
			ArrayList<String> columns = new ArrayList<String>();
			while(resultSet.next()) {
				columns.add(resultSet.getString(4));
			}
			tables.get(i).setColumns(columns);
			Utils.tryClose(resultSet);
		}

		Utils.tryClose(connection);
		return tables;
	}
}