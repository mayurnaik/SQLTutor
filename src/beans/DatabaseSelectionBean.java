package beans;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import edu.gatech.sqltutor.DatabaseManager;

@ManagedBean
@ViewScoped
public class DatabaseSelectionBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{databaseManager}")
	private DatabaseManager databaseManager;
	
	/** Databases consists of a list of currently available database instances grouped by types, such as MySQL and PostgreSQL. */
	private List<SelectItem> databases;

	/** 
	 * On initialization, the UserBean class will populate the selection list of databases.
	 */
	public DatabaseSelectionBean() {
		databases = new ArrayList<SelectItem>();
		/*
		SelectItemGroup groupOne = new SelectItemGroup("MySQL");
		SelectItem[] mysqlDatabases = new SelectItem[] {new SelectItem("", "")};
		groupOne.setSelectItems(mysqlDatabases);
		*/
		SelectItemGroup groupTwo = new SelectItemGroup("PostgreSQL");
		SelectItem[] postgresqlDatabases = new SelectItem[] {new SelectItem("PostgreSQL company", "Company")};
		groupTwo.setSelectItems(postgresqlDatabases);
		//databases.add(groupOne);
		databases.add(groupTwo);
	}
	
	@PostConstruct
	public void refreshList() {
		try {
			List<String> schemas = databaseManager.getUserSchemas();
			
			SelectItemGroup postgres = new SelectItemGroup("PostgreSQL");
			SelectItem[] schemaItems = new SelectItem[schemas.size()];
			for( int i = 0; i < schemaItems.length; ++i ) {
				String schema = schemas.get(i);
				schemaItems[i] = new SelectItem("PostgreSQL " + schema, schema);
			}
			postgres.setSelectItems(schemaItems);
			databases.clear();
			databases.add(postgres);
		} catch( SQLException e ) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
				"Internal error retrieving the schema list.", null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(DatabaseManager dbManager) {
		this.databaseManager = dbManager;
	}

	
	/** 
	 * @return 		The list of SelectItems which will populate the database selection menu.
	 */
	public List<SelectItem> getDatabases() {
		return databases;
	}

	/** 
	 * @param databases		Sets the list of databases which will populate the database selection menu.
	 */
	public void setDatabases(List<SelectItem> databases) {
		this.databases = databases;
	}
}
