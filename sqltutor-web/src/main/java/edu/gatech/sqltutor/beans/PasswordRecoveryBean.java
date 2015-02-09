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

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.mail.MessagingException;

import edu.gatech.sqltutor.util.Emailer;
import edu.gatech.sqltutor.util.SaltHasher;

@ManagedBean
@SessionScoped
public class PasswordRecoveryBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	private static final String NONEXISTANT_EMAIL_ERROR = "The email you entered has not been registered.";
	private static final String SUBJECT = "[SQL-Tutor] Password Recovery - Do Not Reply";
	private static final String EMAIL_CONFIRMATION_MESSAGE = "The password request has been sent to your email.";
	private static final String CONFIRMATION_MESSAGE = "Your password has been changed.";
	private static final String HOMEPAGE_CONTEXT = "/HomePage.jsf";
	
	private String password;
	private String id;
	private String email;
	private String hashedEmail;
	
	public void sendPasswordLink() throws MessagingException {
		try {
			if(!getDatabaseManager().emailExists(getHashedEmail())) {
				BeanUtils.addErrorMessage(null, NONEXISTANT_EMAIL_ERROR);
				return;
			}
			
			UUID uuid = UUID.randomUUID();
			getDatabaseManager().addPasswordChangeRequest(getHashedEmail(), uuid);
			String to = getEmail(); 
			
			final String message = "Someone has requested a password recovery email be sent for your account at SQL Tutor.\n\n"
					+ "Go to this URL within 24 hours to reset your password: https://sqltutor.cc.gatech.edu/PasswordRecoveredPage.jsf?u=" + getEmail() + "&i=" + uuid.toString()
					+ " \n\nDo not reply or send future correspondence to this email address. Please send any concerns to \"sql-tutor@googlegroups.com\".";
			
			Emailer.getInstance().sendMessage(to, SUBJECT, message);
			BeanUtils.addInfoMessage(null, EMAIL_CONFIRMATION_MESSAGE);
		} catch(SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
	}
	
	public void invalidRedirect(ComponentSystemEvent event) throws IOException {
		try {
			if(!getDatabaseManager().getPasswordChangeRequest(getHashedEmail(), getId())) {
				final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
				externalContext.responseSendError(404, "");
			}
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
	}
	
	public void updatePassword() throws IOException {
		try {
			getDatabaseManager().changePassword(getHashedEmail(), getPassword());
			final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect(externalContext.getRequestContextPath() + HOMEPAGE_CONTEXT);

			BeanUtils.addInfoMessage(null, CONFIRMATION_MESSAGE);
		} catch(SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
	}

	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
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
		setHashedEmail(email);
	}

	public void setHashedEmail(String email) {
		String encryptedEmail = null;
		if(email != null) {
			try {
				byte[] hashedEmail = SaltHasher.getEncryptedValue(email.toLowerCase(), UserBean.SALT);
				encryptedEmail = Arrays.toString(hashedEmail);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
		}
		this.hashedEmail = encryptedEmail;
	}
	
	public String getHashedEmail() {
		return hashedEmail;
	}
}
