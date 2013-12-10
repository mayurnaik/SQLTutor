package edu.gatech.sqltutor.rules.er;

public interface ERNode {
	public static final int 
		TYPE_ENTITY       = 1 << 0,
		TYPE_RELATIONSHIP = 1 << 1,
		TYPE_ATTRIBUTE    = 1 << 2;
	
	public int getNodeType();
}
