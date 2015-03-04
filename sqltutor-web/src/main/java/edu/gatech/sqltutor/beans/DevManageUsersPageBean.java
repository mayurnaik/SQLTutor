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
package edu.gatech.sqltutor.beans;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import edu.gatech.sqltutor.tuples.UserTuple;

@ManagedBean
@ViewScoped
public class DevManageUsersPageBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	private static final String EXAMPLES_EMAIL = "[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]";
	private static final String CHOOSE_USER_ERROR = "Please select a user.";
	
	private List<UserTuple> users;
	private UserTuple selectedUser;
	
	@PostConstruct
	public void init() {
		try {
			users = getDatabaseManager().getUserTuples();
			selectedUser = null;
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
	}
	
	public void deleteUser() {
		if(selectedUser == null) {
			BeanUtils.addErrorMessage(null, CHOOSE_USER_ERROR);
			return;
		}
		if(selectedUser.getHashedEmail().equals(EXAMPLES_EMAIL)) {
			final String message = "You are not allowed to alter \"" + selectedUser.getHashedEmail() + "\".";
			BeanUtils.addErrorMessage(null, message);
			return;
		}
		try {
			getDatabaseManager().deleteUser(selectedUser.getHashedEmail(), selectedUser.getAdminCode());
			users.remove(selectedUser);
			final String message = "Successfully deleted \"" + selectedUser.getHashedEmail();
			BeanUtils.addInfoMessage(null, message);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		} 
	}
	
	public void promoteUserToAdmin() {
		if(selectedUser == null) {
			BeanUtils.addErrorMessage(null, CHOOSE_USER_ERROR);
			return;
		}
		try {
			if(!selectedUser.isAdmin()) {
				getDatabaseManager().promoteUserToAdmin(selectedUser.getHashedEmail());
				updateSelectedUser();
				final String message = "Successfully promoted \"" + selectedUser.getHashedEmail() + "\" to admin.";
				BeanUtils.addInfoMessage(null, message);
			} else {
				final String message = "\"" + selectedUser.getHashedEmail() + "\" is already an admin.";
				BeanUtils.addInfoMessage(null, message);
			}
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		} 
	}
	
	public void demoteUserFromAdmin() {
		if(selectedUser == null) {
			BeanUtils.addErrorMessage(null, CHOOSE_USER_ERROR);
			return;
		}
		if(selectedUser.getHashedEmail().equals(EXAMPLES_EMAIL)) {
			final String message = "You are not allowed to alter \"" + selectedUser.getHashedEmail() + "\".";
			BeanUtils.addErrorMessage(null, message);
			return;
		}
		try {
			if(selectedUser.isAdmin()) {
				getDatabaseManager().demoteUserFromAdmin(selectedUser.getHashedEmail(), selectedUser.getAdminCode());
				updateSelectedUser();
				final String message = "Successfully demoted \"" + selectedUser.getHashedEmail() + "\" from admin.";
				BeanUtils.addInfoMessage(null, message);
			} else {
				final String message = "\"" + selectedUser.getHashedEmail() + "\" is not an admin."; 
				BeanUtils.addInfoMessage(null, message);
			}
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
	}
	
	public void promoteUserToDev() {
		if(selectedUser == null) {
			BeanUtils.addErrorMessage(null, CHOOSE_USER_ERROR);
			return;
		}
		try {
			if(!selectedUser.isDev()) {
				if(!selectedUser.isAdmin()) {
					promoteUserToAdmin();
				}
				getDatabaseManager().promoteUserToDev(selectedUser.getHashedEmail());
				updateSelectedUser();
				final String message = "Successfully promoted \"" + selectedUser.getHashedEmail() + "\" to dev.";
				BeanUtils.addInfoMessage(null, message);
			} else {
				final String message = "\"" + selectedUser.getHashedEmail() + "\" is already a dev";
				BeanUtils.addInfoMessage(null, message);
			}
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		} 
	}
	
	public void demoteUserFromDev() {
		if(selectedUser == null) {
			BeanUtils.addErrorMessage(null, CHOOSE_USER_ERROR);
			return;
		}
		if(selectedUser.getHashedEmail().equals(EXAMPLES_EMAIL)) {
			final String message = "You are not allowed to alter \"" + selectedUser.getHashedEmail() + "\".";
			BeanUtils.addErrorMessage(null, message);
			return;
		}
		try {
			if(selectedUser.isDev()) {
				getDatabaseManager().demoteUserFromDev(selectedUser.getHashedEmail());
				updateSelectedUser();
				final String message = "Successfully demoted \"" + selectedUser.getHashedEmail() + "\" from dev.";
				BeanUtils.addInfoMessage(null, message);
			} else {
				final String message = "\"" + selectedUser.getHashedEmail() + "\" is not a dev.";
				BeanUtils.addInfoMessage(null, message);
			}
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		} 
	}
	
	public void updateSelectedUser() throws SQLException {
		UserTuple temp = getDatabaseManager().getUserTuple(selectedUser.getHashedEmail());
		selectedUser.setAdmin(temp.isAdmin());
		selectedUser.setDev(temp.isDev());
		selectedUser.setAdminCode(temp.getAdminCode());
	}

	public List<UserTuple> getUsers() {
		return users;
	}

	public void setUsers(List<UserTuple> users) {
		this.users = users;
	}

	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public UserTuple getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(UserTuple selectedUser) {
		this.selectedUser = selectedUser;
	}
}
