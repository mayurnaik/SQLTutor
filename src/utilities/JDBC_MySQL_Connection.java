package utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBC_MySQL_Connection extends JDBC_Abstract_Connection {

	protected static final String DB_CONNECTION_STRING = "jdbc:mysql://localhost/";
	protected static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	
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

	@Override
	protected Connection getConnection(String databaseName,
			String databaseUsername, String schemaName) {
		// TODO Auto-generated method stub
		return null;
	}
} 
