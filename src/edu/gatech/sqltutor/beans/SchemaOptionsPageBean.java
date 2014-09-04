package edu.gatech.sqltutor.beans;

import java.io.IOException;
import java.io.Serializable;
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

import objects.DatabaseTable;
import beans.UserBean;
import edu.gatech.sqltutor.DatabaseManager;

@ManagedBean
@ViewScoped
public class SchemaOptionsPageBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	@ManagedProperty(value="#{databaseManager}")
	private DatabaseManager databaseManager;
	
	private List<DatabaseTable> tables;
	
	private String selectedSchema;
	
	private boolean visibleToUsers;
	private boolean inOrderQuestions;
	private boolean deleteThisSchema;
	
	@PostConstruct
	public void init() {
		try {
			selectedSchema = userBean.getSelectedSchema();
			tables = databaseManager.getTables(selectedSchema);
			
			HashMap<String, Boolean> options = 
					databaseManager.getOptions(selectedSchema);
			
			visibleToUsers = options.get("visible_to_users");
			inOrderQuestions = options.get("in_order_questions");
			deleteThisSchema = false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void submit() {
		try {
			boolean hasPermissions = databaseManager.checkSchemaPermissions(userBean.getEmail(), userBean.getSelectedSchema());
			if(!hasPermissions) {
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"You do not have permissions for this schema.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			if(deleteThisSchema) {
				databaseManager.deleteSchema(userBean.getSelectedSchema());
				userBean.setSelectedSchema("company"); //TODO: may need to set a warning that company is the default schema

				ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
				externalContext.redirect(externalContext.getRequestContextPath() + "/AdminPage.jsf");
			} else {
				databaseManager.setOptions(userBean.getSelectedSchema(), visibleToUsers, inOrderQuestions);
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Successfully submitted changes.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(DatabaseManager dbManager) {
		this.databaseManager = dbManager;
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
