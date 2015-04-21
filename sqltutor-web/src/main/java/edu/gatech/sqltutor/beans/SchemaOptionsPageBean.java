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
import java.util.UUID;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

import edu.gatech.sqltutor.tuples.TutorialOptionsTuple;

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
	private static final String OPEN_ACCESS_CHECK_ERROR_MESSAGE = "Your opening date/time must be before your closing date/time.";
	
	private TutorialOptionsTuple options;
	
	private boolean deleteThisSchema;
	private boolean linkable;
	
	public void preRenderSetup(ComponentSystemEvent event) throws IOException {
		if (!userBean.isLoggedIn())
			return; //TODO: this is to avoid both preRenderEvents firing, not sure if there is a better way.
		
		if (userBean.getSelectedTutorial() == null || userBean.getSelectedTutorial().isEmpty()) {
			BeanUtils.addErrorMessage(null, "To modify a tutorial, you must first select one.", true);
			BeanUtils.redirect("/AdminPage.jsf");
			return;
		}

		try {
			options = getDatabaseManager().getOptions(userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode());
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
	}
	
	public void submit() throws IOException {
		if(!hasPermissions())
			return;
		
		try {
			if(deleteThisSchema) {
				getDatabaseManager().deleteTutorial(userBean.getSelectedTutorial(), userBean.getSelectedTutorialName(), userBean.getSelectedTutorialAdminCode());
				userBean.removeLinkedTutorial(userBean.getSelectedTutorialAdminCode(), userBean.getSelectedTutorialName());
				userBean.setSelectedTutorial(null);

				ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
				externalContext.redirect(externalContext.getRequestContextPath() + ADMIN_PAGE_CONTEXT);
			} else {
				if(linkable) {
					if(options.getLink() == null)
						options.setLink(UUID.randomUUID().toString());
				} else
					options.setLink(null);
				
				getDatabaseManager().setOptions(userBean.getSelectedTutorialName(), options, userBean.getSelectedTutorialAdminCode());
				BeanUtils.addInfoMessage(null, CONFIRMATION_MESSAGE);
			}
		} catch (SQLException e) {
			if(e.getMessage().contains("open_access_check")) {
				BeanUtils.addErrorMessage(null, OPEN_ACCESS_CHECK_ERROR_MESSAGE);
			} else {
				for(Throwable t : e) {
					t.printStackTrace();
					logException(t, userBean.getHashedEmail());
				}
				BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
			}
		} 
	}
	
	private boolean hasPermissions() {
		boolean hasPermissions = false;
		try {
			hasPermissions = getDatabaseManager().checkTutorialPermissions(userBean.getSelectedTutorialName(), userBean.getAdminCode());

			if(!hasPermissions) 
				BeanUtils.addErrorMessage(null, PERMISSIONS_ERROR);
		} catch(SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(null, DATABASE_ERROR_MESSAGE);
		}
		return hasPermissions;
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

	public TutorialOptionsTuple getOptions() {
		return options;
	}

	public void setOptions(TutorialOptionsTuple options) {
		this.options = options;
	}
	
	public String getVisibleUsingLinkMessage() {
		String message = "Visible using shared link (";
		message += options == null || options.getLink() == null ? "generated on submission" : "https://sqltutor.cc.gatech.edu/TutorialPage.jsf?l="+options.getLink();
		return message + ").";
	}

	public boolean isLinkable() {
		if (options == null) 
			return false;
		linkable = options.getLink() != null;
		return linkable;
	}

	public void setLinkable(boolean linkable) {
		this.linkable = linkable;
	}
}
