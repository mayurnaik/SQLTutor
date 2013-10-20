package edu.gatech.sqltutor.beans;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;

import edu.gatech.sqltutor.DatabaseManager;

import beans.UserBean;

@ManagedBean
@ViewScoped
public class SchemaUploadPageBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	private String schemaName;
	
	@ManagedProperty(value="#{databaseManager}")
	private DatabaseManager databaseManager;

	public SchemaUploadPageBean() {
	}

	// FIXME redundant with other dev-protected pages
	public void devRedirect() throws IOException {
      final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (!userBean.isLoggedIn() || !userBean.isDevUser()) {
	        externalContext.redirect(externalContext.getRequestContextPath() + "/HomePage.jsf");
	    }
	}    
	
	public void handleFileUpload(FileUploadEvent event) {
		try {
			String sqlDumpContents = new String(event.getFile().getContents(), "utf8");
			
			try {
				databaseManager.addUserSchema(schemaName, sqlDumpContents);
				
				FacesMessage msg = new FacesMessage(
					"Successfully added schema \"" + schemaName + "\"");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} catch( IllegalArgumentException e ) {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), null);
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} catch( SQLException e ) {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Database error creating the schema.", e.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch( UnsupportedEncodingException e ) {
			// UTF-8 is always supported by the standard
		}
	}

	public UserBean getUserBean() {
		return userBean;
	}
	
	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}
	
	public String getSchemaName() {
		return schemaName;
	}
	
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}
	
	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}
}
