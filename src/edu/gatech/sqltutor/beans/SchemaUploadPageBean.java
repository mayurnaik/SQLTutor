package edu.gatech.sqltutor.beans;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.sql.SQLException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;

import beans.UserBean;
import edu.gatech.sqltutor.DatabaseManager;

@ManagedBean
@ViewScoped
public class SchemaUploadPageBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	private String schemaDump;
	
	@ManagedProperty(value="#{databaseManager}")
	private DatabaseManager databaseManager;
	
	public void addSchema() {
		try {
			String schemaName = databaseManager.addSchema(schemaDump, userBean.getEmail());
			userBean.setSelectedSchema(schemaName);
	        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect(externalContext.getRequestContextPath() + "/SchemaOptionsPage.jsf");
		} catch ( IllegalArgumentException e) {
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
				"Argument error.", e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch ( SQLException e ) {
			String detailedMessage = e.getMessage();
			if(detailedMessage.contains("getNextException")) {
				detailedMessage = e.getNextException().getMessage();
			}
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
				"Database error.", detailedMessage);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch ( IOException e ) {
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
				"IO error.", e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		schemaDump = new String(event.getFile().getContents(), Charset.forName("UTF-8"));
		final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Successfully uploaded the file. Click submit to apply.", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
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
}
