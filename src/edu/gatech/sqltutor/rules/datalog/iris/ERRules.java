package edu.gatech.sqltutor.rules.datalog.iris;


public class ERRules extends StaticRules {
	private static final String RESOURCE_PATH = "/errules.dlog";
	
	private static final ERRules instance = new ERRules();
	public static ERRules getInstance() { return instance; }

	private ERRules() {
		super(RESOURCE_PATH);
	}
}
