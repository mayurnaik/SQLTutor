package edu.gatech.sqltutor.beans;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import edu.gatech.sqltutor.DatabaseManager;

/**
 * UserBean is a class attended to handle user login status, registration, 
 * and Schema selection.
 * @author		William J. Holton
 * @version		0.0
 */
@ManagedBean
@SessionScoped
public class UserBean implements Serializable {
	private static final long serialVersionUID = 1L;

	@ManagedProperty(value="#{databaseManager}")
	private DatabaseManager databaseManager;
	
	/** LOGIN_ERROR will be displayed above the user name and password input boxes whenever verification fails. */
	private static final String LOGIN_ERROR = "The email or password you entered is incorrect.";
	private static final String REGISTRATION_ERROR = "The email you entered is already registered.";
	private String password;
	private String email;
	private boolean loggedIn = false;
	private boolean admin = false;
	private boolean developer = false;
	private String adminCode = null;
	private List<String> linkedAdminCodes = null;
	/** The string value of the SelectItem chosen by the user. Formatting should follow: "{Database Type} {Schema Name}". */
	private String selectedSchema = "company";

	/** 
	 * When the user submits his or her email and password this method will
	 * verify the given credentials. These attributes are passed such that the email is not case-sensitive.
	 * If the email is registered and the password does not match, an error message will display above the input
	 * boxes. Otherwise, if the password did match, the 'loggedIn' attribute will be set to true and the user will be
	 * allowed through to subsequent pages.
	 */
	public void login() throws IOException { 
		String loginMessagesId = FacesContext.getCurrentInstance().getViewRoot().findComponent(":loginForm:loginMessages").getClientId();
		try {
			if(!getDatabaseManager().isUsernameRegistered(email.toLowerCase()) || 
					!getDatabaseManager().isPasswordCorrect(email.toLowerCase(), password)) {
				
				FacesContext.getCurrentInstance().addMessage(loginMessagesId, new FacesMessage(FacesMessage.SEVERITY_ERROR, LOGIN_ERROR, null));
				return;
			}

			loggedIn = true;
			admin = getDatabaseManager().isAdmin(email.toLowerCase());
			developer = getDatabaseManager().isDeveloper(email.toLowerCase());
			adminCode = getDatabaseManager().getAdminCode(email.toLowerCase());
			linkedAdminCodes = getDatabaseManager().getLinkedAdminCodes(email.toLowerCase());
			selectedSchema = "company";
			final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect(((HttpServletRequest)externalContext.getRequest()).getRequestURI());
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

	public void register() throws IOException {
		String registrationMessagesId = FacesContext.getCurrentInstance().getViewRoot().findComponent(":registrationForm:registrationMessages").getClientId();
		try {
			if(getDatabaseManager().isUsernameRegistered(email.toLowerCase())) {
				FacesContext.getCurrentInstance().addMessage(registrationMessagesId, new FacesMessage(FacesMessage.SEVERITY_ERROR, REGISTRATION_ERROR, null));
				return;
			}
			getDatabaseManager().registerUser(email.toLowerCase(), password);
			loggedIn = true;
			final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect(((HttpServletRequest)externalContext.getRequest()).getRequestURI());
		} catch(SQLException e) {
			String msg = e.getMessage();
			if(msg.contains("user_email")) {
				msg = "The email you entered is already tied to an account.";
				FacesContext.getCurrentInstance().addMessage(registrationMessagesId, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** 
	 * Should the user end up on the login page after he has already logged in, this method will redirect
	 * him or her to the Home Page.
	 * 
	 * @throws IOException		The IOException will be thrown by the redirect method if the URI is not valid.
	 */
	public void loginRedirect() throws IOException {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        boolean onRegistrationPage = ((HttpServletRequest)externalContext.getRequest()).getRequestURI().endsWith("/RegistrationPage.jsf");
        if(!(loggedIn ^ onRegistrationPage)) {
        	externalContext.responseSendError(404, "");
        }
	}
	
	public void devRedirect() throws IOException {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (!isDeveloper()) {
			externalContext.responseSendError(404, "");
	    }
	}
	
	public void adminRedirect() throws IOException {
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
			email = null;
			admin = false;
			developer = false;
			adminCode = null;
			linkedAdminCodes = null;
			final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect(externalContext.getRequestContextPath() + "/HomePage.jsf");
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
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
}
