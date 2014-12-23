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
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.servlet.http.HttpServletRequest;

import edu.gatech.sqltutor.util.SaltHasher;

/**
 * UserBean is a class attended to handle user login status, registration, 
 * and Schema selection.
 * @author		William J. Holton
 * @version		0.0
 */
@ManagedBean
@SessionScoped
public class UserBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final byte[] SALT = "-JllYJaGhP+-0xfJ2+_-K~6YIJkF1ip8hg8qKTDis1TmCjQu*B|Mm TB-szu".getBytes();
	
	/** LOGIN_ERROR will be displayed above the user name and password input boxes whenever verification fails. */
	private static final String LOGIN_ERROR = "The email or password you entered is incorrect.";
	private static final String REGISTRATION_ERROR = "The email you entered is already tied to an account.";
	private static final String HOMEPAGE_CONTEXT = "/HomePage.jsf";
	private static final String REGISTRATION_PAGE_CONTEXT = "/RegistrationPage.jsf";
	private static final String REGISTRATION_MESSAGES_ID = ":registrationForm:registrationMessages";
	private static final String LOGIN_MESSAGES_ID = ":loginForm:loginMessages";
	// FIXME: probably don't want to have a default schema here
	private static final String DEFAULT_SCHEMA_NAME = "company";
	
	private String password;
	private String email;
	private String hashedEmail;
	private boolean loggedIn = false;
	private boolean admin = false;
	private boolean developer = false;
	private String adminCode = null;
	private List<String> linkedAdminCodes = null;
	/** The string value of the SelectItem chosen by the user. Formatting should follow: "{Database Type} {Schema Name}". */
	private String selectedSchema;

	/** 
	 * When the user submits his or her email and password this method will
	 * verify the given credentials. These attributes are passed such that the email is not case-sensitive.
	 * If the email is registered and the password does not match, an error message will display above the input
	 * boxes. Otherwise, if the password did match, the 'loggedIn' attribute will be set to true and the user will be
	 * allowed through to subsequent pages.
	 */
	public void login() throws IOException { 
		try {
			if(!getDatabaseManager().isEmailRegistered(getHashedEmail(), email) || 
					!getDatabaseManager().isPasswordCorrect(getHashedEmail(), password)) {
				BeanUtils.addErrorMessage(LOGIN_MESSAGES_ID, LOGIN_ERROR);
				return;
			}

			loggedIn = true;
			admin = getDatabaseManager().isAdmin(getHashedEmail());
			developer = getDatabaseManager().isDeveloper(getHashedEmail());
			adminCode = getDatabaseManager().getAdminCode(getHashedEmail());
			linkedAdminCodes = getDatabaseManager().getLinkedAdminCodes(getHashedEmail());
			selectedSchema = DEFAULT_SCHEMA_NAME;
			final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect(externalContext.getRequestContextPath() + HOMEPAGE_CONTEXT);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, getHashedEmail());
			}
			BeanUtils.addErrorMessage(LOGIN_MESSAGES_ID, DATABASE_ERROR);
		} 
	}

	public void register() throws IOException {
		try {
			if(getDatabaseManager().isEmailRegistered(getHashedEmail(), email)) {
				BeanUtils.addErrorMessage(REGISTRATION_MESSAGES_ID, REGISTRATION_ERROR);
				return;
			}
			getDatabaseManager().registerUser(getHashedEmail(), email, password);
			login();
		} catch(SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, getHashedEmail());
			}
			BeanUtils.addErrorMessage(REGISTRATION_MESSAGES_ID, DATABASE_ERROR);
		}
	}
	
	/** 
	 * Should the user end up on the login page after he has already logged in, this method will redirect
	 * him or her to the Home Page.
	 * 
	 * @throws IOException		The IOException will be thrown by the redirect method if the URI is not valid.
	 */
	public void loginRedirect(ComponentSystemEvent event) throws IOException {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        boolean onRegistrationPage = ((HttpServletRequest)externalContext.getRequest()).getRequestURI().endsWith(REGISTRATION_PAGE_CONTEXT);
        if(!(loggedIn ^ onRegistrationPage)) {
        	externalContext.responseSendError(404, "");
        }
	}
	
	public void devRedirect(ComponentSystemEvent event) throws IOException {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (!isDeveloper()) {
			externalContext.responseSendError(404, "");
	    }
	}
	
	public void adminRedirect(ComponentSystemEvent event) throws IOException {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (!isAdmin()) {
			externalContext.responseSendError(404, "");
	    }
	}
	
	/** 
	 * @return		The current user's login status (whether the user is logged in or not).
	 */
	public boolean isLoggedIn() {
		return loggedIn;
	}

	/** 
	 * @param loggedIn	Sets the current user's login status (whether the user is logged in or not).
	 * @throws IOException 
	 */
	public void setLoggedIn(boolean loggedIn) throws IOException {
		this.loggedIn = loggedIn;
		if(!loggedIn) {
			password = null;
			hashedEmail = null;
			email = null;
			admin = false;
			developer = false;
			adminCode = null;
			linkedAdminCodes = null;
			final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect(externalContext.getRequestContextPath() + HOMEPAGE_CONTEXT);
		}
	}

	/** 
	 * @return		The database to be used by the tutorial.
	 */
	public String getSelectedSchema() {
		return selectedSchema;
	}

	/** 
	 * @param selectedDatabase		Sets the database to be used by the tutorial.
	 */
	public void setSelectedSchema(String selectedSchema) {
		this.selectedSchema = selectedSchema;
	}
	
	/** 
	 * @return		The user's validation code. If the user is not logged in, this code may not be valid.
	 */
	public String getPassword() {
		return password;
	}
	
	/** 
	 * @param password		Sets the user's validation code. This does not verify that the user's credentials are valid.
	 */
	public void setPassword(String password) {
		this.password = password;
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

	public List<String> getLinkedAdminCodes() {
		return linkedAdminCodes;
	}

	public void setLinkedAdminCodes(List<String> linkedAdminCodes) {
		this.linkedAdminCodes = linkedAdminCodes;
	}

	public boolean isDeveloper() {
		return developer;
	}

	public void setDeveloper(boolean developer) {
		this.developer = developer;
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
