package edu.gatech.sqltutor.beans;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;

import edu.gatech.sqltutor.DatabaseManager;
import edu.gatech.sqltutor.util.Emailer;
import edu.gatech.sqltutor.util.SaltHasher;

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
			if(!databaseManager.emailExists(getHashedEmail())) {
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"The email you entered has not been registered.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			UUID uuid = UUID.randomUUID();
			databaseManager.addPasswordChangeRequest(getHashedEmail(), uuid);
			String to = getEmail(); 
			String subject = "[SQL-Tutor] Password Recovery - Do Not Reply";
			String message = "Someone has requested a password recovery email be sent for your account at SQL Tutor.\n\n"
					+ "Go to this URL within 24 hours to reset your password: https://sqltutor.cc.gatech.edu/PasswordRecoveredPage.jsf?u=" + getEmail() + "&i=" + uuid.toString()
					+ " \n\nDo not reply or send future correspondence to this email address. Please send any concerns to \"sql-tutor@googlegroups.com\".";
			
			Emailer.getInstance().sendMessage(to, subject, message);
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"The password request has been sent to your email.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(InvalidKeySpecException e) {
			e.printStackTrace();
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public void invalidRedirect() {
		try {
			if(!databaseManager.getPasswordChangeRequest(getHashedEmail(), getId())) {
				final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			    try {
					externalContext.redirect(externalContext.getRequestContextPath() + "/HomePage.jsf");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updatePassword() {
		try {
			databaseManager.changePassword(getHashedEmail(), getPassword());
			final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect(externalContext.getRequestContextPath() + "/HomePage.jsf");

			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Your password has been changed.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch(SQLException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
	
	public String getHashedEmail() throws NoSuchAlgorithmException, InvalidKeySpecException {
		return Arrays.toString(SaltHasher.getEncryptedValue(getEmail().toLowerCase(), UserBean.SALT));
	}
}
