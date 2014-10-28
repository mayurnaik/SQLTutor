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

@ManagedBean
@ViewScoped
public class SchemaUploadPageBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	private static final String UPLOAD_CONFIRMATION_MESSAGE = "Successfully uploaded the file. Click submit to apply.";
	private static final String SCHEMA_OPTIONS_PAGE_CONTEXT = "/SchemaOptionsPage.jsf";
	
	private String schemaDump;
	
	public void addSchema() throws IOException {
		try {
			String schemaName = getDatabaseManager().addSchema(schemaDump, userBean.getHashedEmail());
			userBean.setSelectedSchema(schemaName);
	        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect(externalContext.getRequestContextPath() + SCHEMA_OPTIONS_PAGE_CONTEXT);
		} catch ( IllegalArgumentException e) {
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
				"Argument error.", e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch ( SQLException e ) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Database error.", t.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} 
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		schemaDump = new String(event.getFile().getContents(), Charset.forName("UTF-8"));
		BeanUtils.addInfoMessage(null, UPLOAD_CONFIRMATION_MESSAGE);
	}
	
	public UserBean getUserBean() {
		return userBean;
	}
	
	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}
}
