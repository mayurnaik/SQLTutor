package edu.gatech.sqltutor.beans;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
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
	private List<SelectItem> userSchemas = new ArrayList<SelectItem>();
	private String selectedSchema;
	
	@PostConstruct
	public void refreshList() {
		try {
			List<String> uSchemas = getDatabaseManager().getUserSchemas(getUserBean().isAdmin());

			SelectItemGroup itemGroup = new SelectItemGroup();
			SelectItem[] schemaItems = new SelectItem[uSchemas.size()];
			for(int i = 0; i < schemaItems.length; ++i) {
				schemaItems[i] = new SelectItem(uSchemas.get(i));
			}
			itemGroup.setSelectItems(schemaItems);
			userSchemas.clear();
			userSchemas.add(itemGroup);
		} catch( SQLException e ) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
				"Internal error retrieving the schema list.", null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void submit() {
		userBean.setSelectedSchema(selectedSchema);
		//Refresh page
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
	    try {
			ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
