package utilities;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import objects.DatabaseTable;
import objects.QueryResult;
import edu.gatech.sqltutor.Utils;
import edu.gatech.sqltutor.entities.UserQuery;


public abstract class JDBC_Abstract_Connection {
	protected final String DB_NAME = "sqltutor";
	protected final String DB_MANAGER_USERNAME = "DB_Manager";
	protected final String DB_READONLY_USERNAME = "readonly_user";
	protected final String DB_PASSWORD = "SQLTutor!!!";
	
	/*
	 * This method gets passed to the child, where it must be implemented with their connection string/driver.
	 */
	protected abstract Connection getConnection(String databaseName);
	
	/*
	 * This method gets passed to the child, where it must be implemented with their connection string/driver.
	 */
	protected abstract Connection getConnection(String databaseName, String databaseUsername);
	
	public void saveUserQuery(UserQuery query) {
		Connection conn = null;
		PreparedStatement statement = null;
		
		try {
			conn = getConnection(DB_NAME);
			
			Long id = query.getId();
			if( id == null ) {
				final String INSERT = 
						"INSERT INTO query (username, schema, sql, user_description) VALUES (?, ?, ?, ?)";
				statement = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
			} else {
				final String UPDATE = 
						"UPDATE query SET username=?, schema=?, sql=?, user_description=? WHERE id=?";
				statement = conn.prepareStatement(UPDATE);
				statement.setLong(5, id);
			}
			statement.setString(1, query.getUsername());
			statement.setString(2, query.getSchema());
			statement.setString(3, query.getQuery());
			statement.setString(4, query.getUserDescription());
			
			statement.executeUpdate();
			
			if( id == null ) {
				// get the generated id for inserts
				ResultSet keySet = statement.getGeneratedKeys();
				if( !keySet.next() )
					throw new IllegalStateException("No id generated for query.");
				query.setId(keySet.getLong(1));
			}
		} catch( Exception e ) {
			if( e instanceof RuntimeException )
				throw (RuntimeException)e;
			throw new RuntimeException(e); // FIXME exception type
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(conn);
		}
	}
	
	public List<UserQuery> getUserQueries() {
		Connection conn = null;
		Statement statement = null;
		
		try {
			conn = getConnection(DB_NAME);
			
			final String SELECT = 
					"SELECT * FROM query";
			statement = conn.createStatement();
			ResultSet result = statement.executeQuery(SELECT);
			
			List<UserQuery> userQueries = new ArrayList<UserQuery>();
			while( result.next() ) {
				UserQuery query = new UserQuery();
				query.setId(result.getLong("id"));
				// FIXME the bean?
				query.setUsername(result.getString("user"));
				query.setSchema(result.getString("schema"));
				query.setQuery(result.getString("sql"));
				query.setUserDescription(result.getString("user_description"));
				
				userQueries.add(query);
			}
			return userQueries;
			
		} catch( Exception e ) {
			throw new RuntimeException(e); // FIXME exception type
		} finally {
			Utils.tryClose(statement);
			Utils.tryClose(conn);
		}
	}
	
	/*
	 * 
	 */
	public void log(String userQuery) {
		
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			
			connection = getConnection(DB_NAME);
			final String update = "INSERT INTO \"log\" (\"query\") VALUES (?)";
			preparedStatement = connection.prepareStatement(update);
			preparedStatement.setString(1, userQuery);
			preparedStatement.executeUpdate();
			 
		} catch (Exception e) {
			
			System.err.println("Exception: " + e.getMessage());
			
		} finally {
			try {
				if(connection != null) {
					connection.close();
				}
				if(preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {}
		}
	}
	
	/*
	 * 
	 */
	public boolean isPasswordCorrect(String username, String attemptedPassword) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			connection = getConnection(DB_NAME);
			
			// get the user's encryption salt
			String query = "SELECT \"salt\", \"password\" FROM \"user\" WHERE \"username\" = ?";
			
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next()) {
				byte[] salt = resultSet.getBytes(1);
				byte[] encryptedPassword = resultSet.getBytes(2);
				
				// use the password hasher to authenticate
				return PasswordHasher.authenticate(attemptedPassword, encryptedPassword, salt);

			} else {
				System.out.println("Result set for query: "+query+" returned null");
			}
			 
		} catch (Exception e) {
			
			System.err.println("Exception: " + e.getMessage());
			
		} finally {
			
			try {
				if(resultSet != null) {
					resultSet.close();
				}
				if(preparedStatement != null) {
					preparedStatement.close();
				}
				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {}
		}
		
		return false;
	}
	
	/*
	 * 
	 */
	public boolean isUsernameRegistered(String username) {
		
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = getConnection(DB_NAME);
			final String query = "SELECT * FROM \"user\" WHERE \"username\" = ?";
			
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				return true;
			}
			
		} catch (Exception e) {
			
			System.err.println("Exception: " + e.getMessage());
			
		} finally {
			
			try {
				if(resultSet != null) {
					resultSet.close();
				}
				if(preparedStatement != null) {
					preparedStatement.close();
				}
				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {}
		}
		return false;
	}
	
	/*
	 * 
	 */
	public void registerUser(String username, String password) {
		
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		
		try {
			
			connection = getConnection(DB_NAME);
			
			final String update = "INSERT INTO \"user\" (\"username\", \"password\", \"salt\") VALUES (?, ?, ?)";
			
			preparedStatement = connection.prepareStatement(update);
			
			// generate the user's encryption salt and password
			byte[] salt = PasswordHasher.generateSalt();
			byte[] encryptedPassword = PasswordHasher.getEncryptedPassword(password, salt);
			
			preparedStatement.setString(1, username);
			preparedStatement.setBytes(2, encryptedPassword);
			preparedStatement.setBytes(3, salt);
			
			preparedStatement.executeUpdate();

			 
		} catch (Exception e) {
			
			System.err.println("Exception: " + e.getMessage());
			
		} finally {
			try {
				if(connection != null) {
					connection.close();
				}
				if(preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {}
		}
	}
	
	public List<DatabaseTable> getTables(String databaseName) {
		return getTables(databaseName, null);
	}
	
	public List<DatabaseTable> getTables(String databaseName, String schemaName) {
		Connection connection = null;
		ResultSet resultSet = null;
		
		try {
			
			connection = getConnection(databaseName, DB_READONLY_USERNAME);
			
			DatabaseMetaData metadata = connection.getMetaData();
			resultSet = metadata.getTables(null, null, "%", new String[] {"TABLE"});
			ArrayList<DatabaseTable> tables = new ArrayList<DatabaseTable>();
			while(resultSet.next()) {
				// the API tells us the third element is the TABLE_NAME string.
				tables.add(new DatabaseTable(resultSet.getString(3)));
			}
			for(int i=0; i < tables.size(); i++) {
				resultSet = metadata.getColumns(null, null, tables.get(i).getTableName(), null);
				ArrayList<String> columns = new ArrayList<String>();
				while(resultSet.next()) {
					columns.add(resultSet.getString(4));
				}
				tables.get(i).setColumnNames(columns);
			}
			
			return tables;

			 
		} catch (Exception e) {
			
			System.err.println("Exception: " + e.getMessage());
			
		} finally {
			try {
				if(connection != null) {
					connection.close();
				}
				if(resultSet != null) {
					resultSet.close();
				}
			} catch (SQLException e) {}
		}
		return null;
	}
	
	public QueryResult getQueryResult(String databaseName, String query) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		QueryResult queryResult;
		try {

			connection = getConnection(databaseName, DB_READONLY_USERNAME);
			statement = connection.createStatement();
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
			// return the query result object
			queryResult = new QueryResult(databaseName, 
					columnNames, 
					queryData);
			return queryResult;
			 
		} catch (Exception e) {
			queryResult = new QueryResult();
			queryResult.setMalformed(true);
			queryResult.setExceptionMessage(e.getMessage());
			System.err.println("Exception: " + e.getMessage());
		} finally {
			try {
				if(connection != null) {
					connection.close();
				}
				if(statement != null) {
					statement.close();
				}
				if(resultSet != null) {
					resultSet.close();
				}
			} catch (SQLException e) {}
		}
		return queryResult;
	}
	

}
