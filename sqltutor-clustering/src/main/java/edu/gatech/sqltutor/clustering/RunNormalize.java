package edu.gatech.sqltutor.clustering;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Properties;

public class RunNormalize {
	private static void error(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
	
	public static void main(String[] args) {
		try {
			if (args.length < 1) {
				error("Need at least a connection string.");
			}
			
			String connString = args[0];
			String password;
			if( args.length > 1 ) {
				password = args[1];
			} else {
				Console console = System.console();
				password = new String(console.readPassword("Password for %s:", connString));
			}
			run(connString, password);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void run(String connString, String password) throws SQLException {
		Properties props = new Properties();
		props.setProperty("password", password);
		try (
			Connection conn = DriverManager.getConnection(connString, props);
			Statement search = conn.createStatement();
			PreparedStatement normalizeStatement = conn.prepareStatement("UPDATE assignment_log SET normalized_query = ? WHERE timestamp = ?");
			PreparedStatement normalizeError = conn.prepareStatement("UPDATE assignment_log SET normalize_error = ? WHERE timestamp = ?");
			ResultSet rs = search.executeQuery("SELECT * FROM assignment_log")
		) {
			QueryNormalizer normalizer = new QueryNormalizer();
			StringQueryNormalizer fallbackNormalizer = new StringQueryNormalizer();
			while (rs.next()) {
				String query = rs.getString("query");
				Timestamp timestamp = rs.getTimestamp("timestamp");
				try {
					String normalized = normalizer.normalize(query);
					normalizeStatement.setString(1, normalized);
					normalizeStatement.setTimestamp(2, timestamp);
					int rows = normalizeStatement.executeUpdate();
					if (rows != 1) {
						System.err.println("WARN: Updated " + rows + " rows for timestamp: " + timestamp);
					}
				} catch (Throwable e) {
					System.err.println("Fallback normalization for: " + query);
					String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
					normalizeError.setString(1, message);
					normalizeError.setTimestamp(2, timestamp);
					int rows = normalizeError.executeUpdate();
					if (rows != 1) {
						System.err.println("WARN: Updated " + rows + " rows for timestamp: " + timestamp);
					}
					

					String normalized = fallbackNormalizer.normalize(query);
					normalizeStatement.setString(1,  normalized);
					normalizeStatement.setTimestamp(2, timestamp);
					normalizeStatement.executeUpdate();
				}
			}
		}
	}
}
