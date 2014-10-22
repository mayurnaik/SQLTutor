package edu.gatech.sqltutor.beans;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import edu.gatech.sqltutor.DatabaseManager;
import edu.gatech.sqltutor.UserTuple;
import edu.gatech.sqltutor.util.SaltHasher;

@ManagedBean
@ViewScoped
public class DevManageUsersPageBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	@ManagedProperty(value="#{databaseManager}")
	private DatabaseManager databaseManager;
	
	private List<UserTuple> users;
	private UserTuple selectedUser;
	
	@PostConstruct
	public void init() {
		try {
			users = getDatabaseManager().getUserTuples();
			selectedUser = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteUser() {
		if(selectedUser.getHashedEmail().equals("[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]")) {
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"You are not allowed to alter \"" + selectedUser.getHashedEmail() + "\".", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return;
		}
		try {
			getDatabaseManager().deleteUser(selectedUser.getHashedEmail(), selectedUser.getAdminCode());
			users.remove(selectedUser);
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Successfully deleted \"" + selectedUser.getHashedEmail() + "\".", "");

			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (SQLException e) {
			if(e.getNextException() != null)
				e.getNextException().printStackTrace();
			else
				e.printStackTrace();
		} 
	}
	
	public void promoteUserToAdmin() {
		try {
			FacesMessage msg;
			if(!selectedUser.isAdmin()) {
				getDatabaseManager().promoteUserToAdmin(selectedUser.getHashedEmail());
				updateSelectedUser();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Successfully promoted \"" + selectedUser.getHashedEmail() + "\" to admin.", "");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"\"" + selectedUser.getHashedEmail() + "\" is already an admin.", "");
			}
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (SQLException e) {
			if(e.getNextException() != null)
				e.getNextException().printStackTrace();
			else
				e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void demoteUserFromAdmin() {
		if(selectedUser.getHashedEmail().equals("[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]")) {
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"You are not allowed to alter \"" + selectedUser.getHashedEmail() + "\".", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return;
		}
		try {
			FacesMessage msg;
			if(selectedUser.isAdmin()) {
				getDatabaseManager().demoteUserFromAdmin(selectedUser.getHashedEmail(), selectedUser.getAdminCode());
				updateSelectedUser();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Successfully demoted \"" + selectedUser.getHashedEmail() + "\" from admin.", "");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"\"" + selectedUser.getHashedEmail() + "\" is not an admin.", ""); 
			}
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (SQLException e) {
			if(e.getNextException() != null)
				e.getNextException().printStackTrace();
			else
				e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void promoteUserToDev() {
		try {
			FacesMessage msg;
			if(!selectedUser.isDev()) {
				if(!selectedUser.isAdmin()) {
					promoteUserToAdmin();
				}
				getDatabaseManager().promoteUserToDev(selectedUser.getHashedEmail());
				updateSelectedUser();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Successfully promoted \"" + selectedUser.getHashedEmail() + "\" to dev.", "");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"\"" + selectedUser.getHashedEmail() + "\" is already a dev", "");
			}
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (SQLException e) {
			if(e.getNextException() != null)
				e.getNextException().printStackTrace();
			else
				e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void demoteUserFromDev() {
		if(selectedUser.getHashedEmail().equals("[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]")) {
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"You are not allowed to alter \"" + selectedUser.getHashedEmail() + "\".", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return;
		}
		try {
			FacesMessage msg;
			if(selectedUser.isDev()) {
				getDatabaseManager().demoteUserFromDev(selectedUser.getHashedEmail());
				updateSelectedUser();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Successfully demoted \"" + selectedUser.getHashedEmail() + "\" from dev.", "");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"\"" + selectedUser.getHashedEmail() + "\" is not a dev.", "");
			}
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (SQLException e) {
			if(e.getNextException() != null)
				e.getNextException().printStackTrace();
			else
				e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateSelectedUser() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
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

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public UserTuple getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(UserTuple selectedUser) {
		this.selectedUser = selectedUser;
	}
}
