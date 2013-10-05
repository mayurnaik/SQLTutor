package objects;

import java.util.ArrayList;
import utilities.JDBC_Abstract_Connection;

public class DatabaseSchema {
	private ArrayList<DatabaseTable> databaseTables = new ArrayList<DatabaseTable>();

	public DatabaseSchema(JDBC_Abstract_Connection connection, String selectedDatabase) {
		ArrayList<String> tables = connection.getTables(selectedDatabase); 
		// for every table name retrieved, add a database table object to our schema
		for(int i=0; i < tables.size(); i++) {
			// set the database table's name to what was retrieved from the practice database.
			DatabaseTable table = new DatabaseTable(tables.get(i), connection, selectedDatabase);
			databaseTables.add(table);
		}
	}
	
	public ArrayList<DatabaseTable> getDatabaseTables() {
		return databaseTables;
	}

	public void setDatabaseTables(ArrayList<DatabaseTable> databaseTables) {
		this.databaseTables = databaseTables;
	}
}
