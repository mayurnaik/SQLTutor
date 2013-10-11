package beans;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@ViewScoped
public class DatabaseSelectionBean {
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
		// new SelectItem("PostgreSQL World", "World"), new SelectItem("PostgreSQL Booktown", "Booktown")
		groupTwo.setSelectItems(postgresqlDatabases);
		//databases.add(groupOne);
		databases.add(groupTwo);
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
