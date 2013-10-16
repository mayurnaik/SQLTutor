package utilities;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;


public class JDBC_PostgreSQL_Connection extends JDBC_Abstract_Connection {
	protected static final String DB_CONNECTION_STRING = "jdbc:postgresql://localhost/";
	protected static final String DB_DRIVER = "org.postgresql.Driver";
	
	/*
	 * 
	 */
	public void testConnection() {
		
		Connection connection = null;
		
		System.out.println("------------ PostgreSQL "
				+ "JDBC Connection Testing ------------");
 
		// Try to find PostgreSQL driver.
		try {
 
			Class.forName("org.postgresql.Driver");
 
		} catch (ClassNotFoundException e) {
 
			System.out.println("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!");
			e.printStackTrace();
			return;
 
		}
 
		System.out.println("PostgreSQL JDBC Driver Registered!");

		// Try to set up a connection
		try {
 
			connection = DriverManager.getConnection(DB_CONNECTION_STRING + "SQLTutor_Users", DB_MANAGER_USERNAME, DB_PASSWORD);
			
			if (connection != null) {
				System.out.println("Connection successful.");
			}
 
		} catch (SQLException e) {
 
			System.out.println("Connection Failed! Check output console.");
			e.printStackTrace();
			return;
 
		} finally {	
			// close the connection when we're finished
			try {
				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	/*
	 * 
	 */
	protected Connection getConnection(String databaseName) {
		
		Connection connection = null;
		 
		try {
 
			Class.forName(DB_DRIVER);
 
		} catch (ClassNotFoundException e) {
 
			System.out.println(e.getMessage());
 
		}
 
		try {
 
			connection = DriverManager.getConnection(
                            DB_CONNECTION_STRING + databaseName, DB_MANAGER_USERNAME ,DB_PASSWORD);
 
		} catch (SQLException e) {
 
			System.out.println(e.getMessage());
 
		}
 
		return connection;
	}
	
	/*
	 * 
	 */
	protected Connection getConnection(String databaseName, String databaseUsername) {
		
		Connection connection = null;
		 
		try {
 
			Class.forName(DB_DRIVER);
 
		} catch (ClassNotFoundException e) {
 
			System.out.println(e.getMessage());
 
		}
 
		try {
 
			connection = DriverManager.getConnection(
                            DB_CONNECTION_STRING + databaseName, databaseUsername, DB_PASSWORD);
 
		} catch (SQLException e) {
 
			System.out.println(e.getMessage());
 
		}
 
		return connection;
	}
} 
