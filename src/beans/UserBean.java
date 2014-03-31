package beans;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import utilities.JDBC_PostgreSQL_Connection;
import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;

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
	
	/** CONNECTION will be used to connect to the 'Users' PostgreSQL database for user name and password verification. */
	private final JDBC_PostgreSQL_Connection CONNECTION = new JDBC_PostgreSQL_Connection();
	/** LOGIN_ERROR will be displayed above the user name and password input boxes whenever verification fails. */
	private final String LOGIN_ERROR = "The username or password you entered is incorrect.";
	private final String REGISTRATION_ERROR = "The username you entered is already registered.";
	private String username;
	private String password;
	private boolean loggedIn;
	/** The string value of the SelectItem chosen by the user. Formatting should follow: "{Database Type} {Schema Name}". */
	private String selectedSchema;

	public UserBean() {
		loggedIn = false;
		selectedSchema = "PostgreSQL company";
	}
	
	/** 
	 * When the user submits his or her username, password, and Schema selection, this method will
	 * verify the given credentials. These attributes are passed such that the username is not case-sensitive.
	 * If the username is registered and the password does not match, an error message will display above the input
	 * boxes. Otherwise, if the password did match, the 'loggedIn' attribute will be set to true and the user will be
	 * allowed through to the tutorial page. If the username did not exist, the user will be registered and the method
	 * will move on as if the credentials were correct.
	 */
	public void login() throws IOException { 
		String loginMessagesId = FacesContext.getCurrentInstance().getViewRoot().findComponent(":loginForm:loginMessages").getClientId();
		if(!CONNECTION.isUsernameRegistered(username.toLowerCase()) || 
				!CONNECTION.isPasswordCorrect(username.toLowerCase(), password)) {
			
			FacesContext.getCurrentInstance().addMessage(loginMessagesId, new FacesMessage(FacesMessage.SEVERITY_ERROR, LOGIN_ERROR, null));
			return;
		} 
		loggedIn = true;
		final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		externalContext.redirect(((HttpServletRequest)externalContext.getRequest()).getRequestURI());
	}
	
	public void register() throws IOException {
		String registrationMessagesId = FacesContext.getCurrentInstance().getViewRoot().findComponent(":registrationForm:registrationMessages").getClientId();
		if(CONNECTION.isUsernameRegistered(username.toLowerCase())) {
			FacesContext.getCurrentInstance().addMessage(registrationMessagesId, new FacesMessage(FacesMessage.SEVERITY_ERROR, REGISTRATION_ERROR, null));
			return;
		} 
		CONNECTION.registerUser(username.toLowerCase(), password);
		loggedIn = true;
		final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		externalContext.redirect(((HttpServletRequest)externalContext.getRequest()).getRequestURI());
	}
	
	/** 
	 * Should the user end up on the login page after he has already logged in, this method will redirect
	 * him or her to the Tutorial Page.
	 * 
	 * @throws IOException		The IOException will be thrown by the redirect method if the URI is not valid.
	 */
	public void homepageRedirect() throws IOException {
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        boolean onRegistrationPage = ((HttpServletRequest)externalContext.getRequest()).getRequestURI().endsWith("/RegistrationPage.jsf");
        if(loggedIn && onRegistrationPage || !loggedIn && !onRegistrationPage) {
        	externalContext.redirect(externalContext.getRequestContextPath() + "/HomePage.jsf");
        }
	}
	
	/**
	 * Temporary method for special development users.
	 * 
	 * @deprecated
	 * @return if this is a development user
	 */
	public boolean isDevUser() {
		if(isLoggedIn()) {
			Pattern devNames = Pattern.compile("^dev|jake|mayur|sumit|will|msweat$", 
				Pattern.CASE_INSENSITIVE);
			return devNames.matcher(username).matches();
		}
		return false;
	}
	
	/** 
	 * @return		The current user's login status (whether the user is logged in or not).
	 */
	public boolean isLoggedIn() {
		return loggedIn;
	}

	/** 
	 * @param loggedIn	Sets the current user's login status (whether the user is logged in or not).
	 */
	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	/** 
	 * @return		The database to be used by the tutorial.
	 */
	public String getSelectedSchema() {
		return selectedSchema;
	}
	
	public String getSelectedSchemaName() {
		String[] selectedSchemaSplit = selectedSchema.split(" ");
		return selectedSchemaSplit[1];
	}

	/** 
	 * @param selectedDatabase		Sets the database to be used by the tutorial.
	 */
	public void setSelectedSchema(String selectedSchema) {
		this.selectedSchema = selectedSchema;
	}
	
	/** 
	 * @return		The user's identification token. If the user is not logged in, this value may not represent a valid user.
	 */
	public String getUsername() {
		return username;
	}
	
	/** 
	 * @param username		Sets the user's identification token. This does not verify that the user's credentials are valid.
	 */
	public void setUsername(String username) {
		this.username = username;
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
}
