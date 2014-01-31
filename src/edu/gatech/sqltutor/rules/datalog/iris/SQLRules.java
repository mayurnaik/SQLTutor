package edu.gatech.sqltutor.rules.datalog.iris;

/** Static rules related to the SQL query structure. */
public class SQLRules extends StaticRules {
	private static final String RESOURCE_PATH = "/sqlrules.dlog";
	
	private static final SQLRules instance = new SQLRules();
	public static SQLRules getInstance() { return instance; }

	private SQLRules() {
		super(RESOURCE_PATH);
	}
}
