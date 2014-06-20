package edu.gatech.sqltutor.beans;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import beans.UserBean;
import edu.gatech.sqltutor.DatabaseManager;
import utilities.Emailer;
import utilities.JDBC_PostgreSQL_Connection;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;

@ManagedBean
@SessionScoped
public class PasswordRecoveryBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	@ManagedProperty(value="#{databaseManager}")
	private DatabaseManager databaseManager;
	
	private String password;
	private String id;
	private String email;
	
	public void sendPasswordLink() {
		try {
			if(!databaseManager.emailExists(getEmail())) {
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"The email you entered has not been registered.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			UUID uuid = UUID.randomUUID();
			databaseManager.addPasswordChangeRequest(getEmail(), uuid);
			String to = getEmail(); 
			String subject = "[SQL-Tutor] Password Recovery";
			String message = "Someone has requested a password recovery email be sent for your account at SQL Tutor.\n\n"
					+ "Go to this URL to reset your password: https://sqltutor.cc.gatech.edu/PasswordRecoveredPage.jsf?u=" + getEmail() + "&i=" + uuid.toString();
			
			Emailer.getInstance().sendMessage(to, subject, message);
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
				"The password request has been sent to your email.", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void invalidRedirect() {
		if(!databaseManager.getPasswordChangeRequest(getEmail(), id)) {
			final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
	        try {
				externalContext.redirect(externalContext.getRequestContextPath() + "/HomePage.jsf");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updatePassword() {
		try {
			databaseManager.changePassword(getEmail(), getPassword());
			final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
	        try {
				externalContext.redirect(externalContext.getRequestContextPath() + "/HomePage.jsf");
			} catch (IOException e) {
				e.printStackTrace();
			}
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Your password has been changed.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch(SQLException e) {
			e.printStackTrace();
		}
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
