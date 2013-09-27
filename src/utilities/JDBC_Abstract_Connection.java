package utilities;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public abstract class JDBC_Abstract_Connection {
	protected final String DB_MANAGER_USERNAME = "DB_Manager";
	protected final String DB_READONLY_USERNAME = "readonly_user";
	protected final String DB_PASSWORD = "SQLTutor!!!";
	
	/*
	 * This method gets passed to the child, where it must be implemented with their connection string/driver.
	 */
	protected abstract Connection getConnection(String DB_NAME);
	
	/*
	 * This method gets passed to the child, where it must be implemented with their connection string/driver.
	 */
	protected abstract Connection getConnection(String dbName, String dbUsername);
	
	/*
	 * 
	 */
	public void log(String userQuery) {
		
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		final String DB_NAME =  "log";
		
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
		final String DB_NAME =  "user";
		
		try {
			connection = getConnection(DB_NAME);
			
			// get the user's encryption salt
			String query = "SELECT \"salt\", \"password\" FROM \"user\" WHERE \"username\" = ?";
			
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			log(query);
			
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
		
		final String DB_NAME =  "user";

		try {
			connection = getConnection(DB_NAME);
			final String query = "SELECT * FROM \"user\" WHERE \"username\" = ?";
			
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			resultSet = preparedStatement.executeQuery();
			log(query);
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
		final String DB_NAME =  "user";
		
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
			log(update);
			 
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
	
	public ArrayList<String> getTables(String databaseName) {
		Connection connection = null;
		ResultSet resultSet = null;
		
		try {
			
			connection = getConnection(databaseName, DB_READONLY_USERNAME);
			
			DatabaseMetaData metadata = connection.getMetaData();
			resultSet = metadata.getTables(null, null, "%", new String[] {"TABLE"});
			ArrayList<String> tables = new ArrayList<String>();
			while(resultSet.next()) {
				// the API tells us the third element is the TABLE_NAME string.
				tables.add(resultSet.getString(3));
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
	
	public ArrayList<String> getTableColumns(String databaseName, String table) {
		Connection connection = null;
		ResultSet resultSet = null;
		
		try {
			
			connection = getConnection(databaseName, DB_READONLY_USERNAME);
			
			DatabaseMetaData metadata = connection.getMetaData();
			resultSet = metadata.getColumns(null, null, table, null);
			ArrayList<String> columns = new ArrayList<String>();
			while(resultSet.next()) {
				columns.add(resultSet.getString(4));
			}
			return columns;
			 
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
	
	public ArrayList<String> getQueryColumns(String databaseName, String query) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			
			connection = getConnection(databaseName, DB_READONLY_USERNAME);
			
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			log(query);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			// Put each of the column names into an array list.
			int columnCount = resultSetMetaData.getColumnCount();
			ArrayList<String> columnNames = new ArrayList<String>();

			for(int i = 1; i <=  columnCount; i++) {
				columnNames.add(resultSetMetaData.getColumnName(i));
			}
			
			return columnNames;
			
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
	
	public ArrayList<ArrayList<String>> getQueryData(String databaseName, String query) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {

			connection = getConnection(databaseName, DB_READONLY_USERNAME);

			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			log(query);
			// Put each of the column names into an array list.
			int columnCount = resultSet.getMetaData().getColumnCount();
			ArrayList<ArrayList<String>> queryData = new ArrayList<ArrayList<String>>();
			while(resultSet.next()) {
				ArrayList<String> rowData = new ArrayList<String>();
				for (int i = 1; i <= columnCount; i++) {
					rowData.add(resultSet.getString(i));
				}
				queryData.add(rowData);
			}

			return queryData;
			 
		} catch (Exception e) {
			
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
		return null;
	}
	
	public String getQueryFeedback(String databaseName, String userSQLQuery, String answerSQLQuery) {
		Connection connection = null;
		Statement statement = null;
		ResultSet userResultSet = null;
		ResultSet answerResultSet = null;
		// NOTE: This is used as a check in TutorialBean.java
		// When it pulls feedback, it will only populate the result table if the exception string is not there.
		String exception = "Query malformed.\n";
		
		try {

			connection = getConnection(databaseName, DB_READONLY_USERNAME);
			// forms user result set
			statement = connection.createStatement();
			userResultSet = statement.executeQuery(userSQLQuery);
			//log(userSQLQuery);     User query is already logged when it is displayed.
			ResultSetMetaData userResultSetMetaData = userResultSet.getMetaData();
			int userColumnCount = userResultSetMetaData.getColumnCount();
			// forms answer result set
			statement = connection.createStatement();
			answerResultSet = statement.executeQuery(answerSQLQuery);
			log(answerSQLQuery);
			ResultSetMetaData answerResultSetMetaData = answerResultSet.getMetaData();
			int answerColumnCount = answerResultSetMetaData.getColumnCount();
			//checks to see if user and answer result sets have the same number of columns
			if(userColumnCount != answerColumnCount) {
				return "Your query results were incorrect. The number of columns retrieved by your query (" + userColumnCount + ") did not line up with the answer.";
			}
			while(userResultSet.next()) {
				if(answerResultSet.next()) {
					for(int i = 1; i < userColumnCount + 1; i++) {
						if (!userResultSet.getObject(i).equals(answerResultSet.getObject(i))) {
							return "Your query results were incorrect. Your data didn't match our stored answer!\nHowever, the number of columns returned was correct! Keep trying.";
						}
					}
				} else {	// if the user result set had another row, but the answer did not, they didn't match
					return "Your query results were incorrect. Your result had too many rows of data!\nHowever, the number of columns returned was correct! Keep trying.";
				}
			}
			// if the user result set didn't have another row, but the answer result set did, they didn't match
			if (answerResultSet.next()) {
				return "Your query results were incorrect. Your result did not have enough rows of data!\nHowever, the number of columns returned was correct! Keep trying.";
			}
			return "Correct! Good job!";
			 
		} catch (Exception e) {
			
			System.err.println("Exception: " + e.getMessage());
			exception += "Exception: " + e.getMessage();
			
		} finally {
			try {
				if(connection != null) {
					connection.close();
				}
				if(statement != null) {
					statement.close();
				}
				if(answerResultSet != null) {
					answerResultSet.close();
				}
				if(userResultSet != null) {
					userResultSet.close();
				}
			} catch (SQLException e) {}
		}
		return exception;
	}
}
