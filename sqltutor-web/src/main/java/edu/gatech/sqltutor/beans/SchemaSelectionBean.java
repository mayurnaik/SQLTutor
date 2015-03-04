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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.servlet.http.HttpServletRequest;

@ManagedBean
@ViewScoped
public class SchemaSelectionBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	/** Databases consists of a list of currently available database instances grouped by types, such as MySQL and PostgreSQL. */
	private List<SelectItem> userSchemas = new LinkedList<SelectItem>();
	private String selectedSchema;
	
	@PostConstruct
	public void refreshList() {
		Set<String> uSchemas = userBean.getAvailableSchemas();
		if(uSchemas != null) {
			SelectItemGroup itemGroup = new SelectItemGroup();
			SelectItem[] schemaItems = new SelectItem[uSchemas.size()];
	
			Iterator<String> uSchemasIterator = uSchemas.iterator();
			for(int i = 0; i < schemaItems.length; i++) {
				schemaItems[i] = new SelectItem(uSchemasIterator.next());
			}
			itemGroup.setSelectItems(schemaItems);
			userSchemas.clear();
			userSchemas.add(itemGroup);
		}
	}
	
	public void submit() throws IOException {
		userBean.setSelectedSchema(selectedSchema);
		//Refresh page
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
	    ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
	}

	public void setSelectedSchema(String selectedSchema) {
		this.selectedSchema = selectedSchema;
	}

	public String getSelectedSchema() {
		return selectedSchema;
	}

	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public List<SelectItem> getUserSchemas() {
		return userSchemas;
	}

	public void setUserSchemas(List<SelectItem> userSchemas) {
		this.userSchemas = userSchemas;
	}
}
