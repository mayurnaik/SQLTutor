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
package edu.gatech.sqltutor.rules.symbolic;

/** Exception indicating a symbolic token was encountered that could not be handled. */
public class UnhandledSymbolicTypeException extends SymbolicException {
	private static final long serialVersionUID = 1L;
	
	protected final SymbolicType type;

	public UnhandledSymbolicTypeException(SymbolicType type) {
		this.type = type;
	}

	public UnhandledSymbolicTypeException(SymbolicType type, String message) {
		super(message);
		this.type = type;
	}

	public UnhandledSymbolicTypeException(SymbolicType type, Throwable cause) {
		super(cause);
		this.type = type;
	}

	public UnhandledSymbolicTypeException(SymbolicType type, String message, Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	public UnhandledSymbolicTypeException(String message) {
		this(null, message);
	}

	public UnhandledSymbolicTypeException(Throwable cause) {
		this((SymbolicType)null, cause);
	}

	public UnhandledSymbolicTypeException(String message, Throwable cause) {
		this(null, message, cause);
	}
	
	public SymbolicType getSymbolicType() {
		return type;
	}
}
