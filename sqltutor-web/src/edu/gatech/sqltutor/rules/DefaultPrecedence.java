/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package edu.gatech.sqltutor.rules;

public class DefaultPrecedence {
	/** For rules that will destructively update the AST. */
	public static final int DESTRUCTIVE_UPDATE = 1000;
	
	/** For rules that will just reorganize the symbolic sentence structure. */
	public static final int FRAGMENT_REWRITE = 100;
	
	/** For rules that will perform simplification on the symbolic sentence structure. */
	public static final int SIMPLIFYING_SYMBOLIC = 150;
	
	/** For rules that will add information to the symbolic sentence structure. */
	public static final int FRAGMENT_ENHANCEMENT = 200;
	
	/** For rules that partially lower tokens, but leave some non-literal tokens. */
	public static final int PARTIAL_LOWERING = 75;
	
	/** For rules that will lower to literal natural language. */
	public static final int LOWERING = 50;
	
	/** For rules that simplified already-lowered state. */
	public static final int SIMPLIFYING_LOWERED = 40;
	
	public static final int CLEANUP = 0;
}
