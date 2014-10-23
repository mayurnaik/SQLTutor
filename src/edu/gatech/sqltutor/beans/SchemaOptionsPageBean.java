package edu.gatech.sqltutor.beans;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import edu.gatech.sqltutor.DatabaseTable;

@ManagedBean
@ViewScoped
public class SchemaOptionsPageBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	private List<DatabaseTable> tables;
	
	private String selectedSchema;
	
	private boolean visibleToUsers;
	private boolean inOrderQuestions;
	private boolean deleteThisSchema;
	
	@PostConstruct
	public void init() {
		try {
			selectedSchema = userBean.getSelectedSchema();
			tables = getDatabaseManager().getTables(selectedSchema);
			
			HashMap<String, Boolean> options = 
					getDatabaseManager().getOptions(selectedSchema);
			
			visibleToUsers = options.get("visible_to_users");
			inOrderQuestions = options.get("in_order_questions");
			deleteThisSchema = false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void submit() {
		try {
			boolean hasPermissions = getDatabaseManager().checkSchemaPermissions(userBean.getHashedEmail(), userBean.getSelectedSchema());
			if(!hasPermissions) {
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"You do not have permissions for this schema.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			if(deleteThisSchema) {
				getDatabaseManager().deleteSchema(userBean.getSelectedSchema());
				userBean.setSelectedSchema("company"); //TODO: may need to set a warning that company is the default schema

				ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
				externalContext.redirect(externalContext.getRequestContextPath() + "/AdminPage.jsf");
			} else {
				getDatabaseManager().setOptions(userBean.getSelectedSchema(), visibleToUsers, inOrderQuestions);
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Successfully submitted changes.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public boolean isVisibleToUsers() {
		return visibleToUsers;
	}

	public void setVisibleToUsers(boolean visibleToUsers) {
		this.visibleToUsers = visibleToUsers;
	}

	public boolean isInOrderQuestions() {
		return inOrderQuestions;
	}

	public void setInOrderQuestions(boolean inOrderQuestions) {
		this.inOrderQuestions = inOrderQuestions;
	}

	public boolean isDeleteThisSchema() {
		return deleteThisSchema;
	}

	public void setDeleteThisSchema(boolean deleteThisSchema) {
		this.deleteThisSchema = deleteThisSchema;
	}
	
	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public List<DatabaseTable> getTables() {
		return tables;
	}

	public void setTables(List<DatabaseTable> tables) {
		this.tables = tables;
	}
}
