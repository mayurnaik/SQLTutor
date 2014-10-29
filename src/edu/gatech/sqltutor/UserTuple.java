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
package edu.gatech.sqltutor;

import java.io.Serializable;

public class UserTuple implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String email;
	private boolean admin;
	private String adminCode;
	private boolean dev;
	
	public UserTuple(final String email, final boolean admin, final String adminCode, final boolean dev) {
		this.email = email;
		this.admin = admin;
		this.adminCode = adminCode;
		this.dev = dev;
	}

	public UserTuple() {
		email = "";
		admin = false;
		adminCode = "";
		dev = false;
	}
	
	public String getAdminStatus() {
		return admin + " (" + adminCode + ")";
	}

	public String getHashedEmail() {
		return email;
	}

	public void setHashedEmail(String email) {
		this.email = email;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getAdminCode() {
		return adminCode;
	}

	public void setAdminCode(String adminCode) {
		this.adminCode = adminCode;
	}

	public boolean isDev() {
		return dev;
	}

	public void setDev(boolean dev) {
		this.dev = dev;
	}
}
