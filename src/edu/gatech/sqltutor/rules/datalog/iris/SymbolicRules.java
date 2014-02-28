package edu.gatech.sqltutor.rules.datalog.iris;

public class SymbolicRules extends StaticRules {
	private static final SymbolicRules instance = new SymbolicRules();
	public static SymbolicRules getInstance() { return instance; }

	public SymbolicRules() {
		super("/symbolicrules.dlog");
	}
}
