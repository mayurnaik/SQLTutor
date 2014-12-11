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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
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
	
	private static final String PERMISSIONS_ERROR = "You do not have permissions for this schema.";
	private static final String CONFIRMATION_MESSAGE = "Successfully submitted changes.";
	private static final String ADMIN_PAGE_CONTEXT = "/AdminPage.jsf";
	// FIXME: probably don't want to have a default schema here
	private static final String DEFAULT_SCHEMA_NAME = "company";
	
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
			//FIXME: probably want to change from using concrete string names here
			visibleToUsers = options.get("visible_to_users"); 
			inOrderQuestions = options.get("in_order_questions");
			deleteThisSchema = false;
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR);
		}
	}
	
	public void submit() throws IOException {
		if(!hasPermissions())
			return;
		
		try {
			if(deleteThisSchema) {
				getDatabaseManager().deleteSchema(userBean.getSelectedSchema());
				userBean.setSelectedSchema(DEFAULT_SCHEMA_NAME);

				ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
				externalContext.redirect(externalContext.getRequestContextPath() + ADMIN_PAGE_CONTEXT);
			} else {
				getDatabaseManager().setOptions(userBean.getSelectedSchema(), visibleToUsers, inOrderQuestions);
				BeanUtils.addInfoMessage(null, CONFIRMATION_MESSAGE);
			}
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR);
		}
	}
	
	private boolean hasPermissions() {
		boolean hasPermissions = false;
		try {
			hasPermissions = getDatabaseManager().checkSchemaPermissions(userBean.getHashedEmail(), userBean.getSelectedSchema());

			if(!hasPermissions) 
				BeanUtils.addErrorMessage(null, PERMISSIONS_ERROR);
		} catch(SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR);
		}
		return hasPermissions;
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
