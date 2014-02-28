package edu.gatech.sqltutor.rules;

import org.deri.iris.api.IKnowledgeBase;

import edu.gatech.sqltutor.rules.symbolic.RootToken;

public class SymbolicState {
	private RootToken rootToken;
	private SQLState sqlState;
	private IKnowledgeBase knowledgeBase;
	
	public SymbolicState() { }
	public SymbolicState(SQLState sqlState) {
		this.sqlState = sqlState;
	}

	public RootToken getRootToken() {
		return rootToken;
	}

	public void setRootToken(RootToken rootToken) {
		this.rootToken = rootToken;
	}

	public SQLState getSqlState() {
		return sqlState;
	}

	public void setSqlState(SQLState sqlState) {
		this.sqlState = sqlState;
	}
	public IKnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}
	public void setKnowledgeBase(IKnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}
}
