package edu.gatech.sqltutor.beans;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;

import edu.gatech.sqltutor.DatabaseTable;
import edu.gatech.sqltutor.QueryResult;

@ManagedBean
@ViewScoped
public class SchemaInstancesPageBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;

	private List<DatabaseTable> tables;
	private HashMap<String, QueryResult> tableData;
	
	private String selectedSchema;

	private String query;
	private QueryResult queryResult;
	
	@PostConstruct
	public void init() {
		selectedSchema = userBean.getSelectedSchema();
		setupTables();
	}
	
	public void setupTables() {
		try {
			tables = getDatabaseManager().getTables(selectedSchema);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getEmail());
			}
		}

		List<String> tableNames = new ArrayList<String>();
		
		for(DatabaseTable table : tables) {
			tableNames.add(table.getTableName());
		}
		
		try {
			setTableData(getDatabaseManager().getAllData(selectedSchema, tableNames));
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getEmail());
			}
		}
	}
	
	public void processSQL() {
		try {
			boolean hasPermissions = getDatabaseManager().checkSchemaPermissions(userBean.getHashedEmail(), userBean.getSelectedSchema());

			if(!hasPermissions) {
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"You do not have permissions for this schema.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			queryResult = getDatabaseManager().getQueryResult(selectedSchema, query, userBean.isAdmin());
		} catch(SQLException e) {
			queryResult = null;
			String message = e.getMessage();
			
			if(message.equals("No results were returned by the query.")) {
				RequestContext requestContext = RequestContext.getCurrentInstance();  
				requestContext.execute("window.location.replace(window.location.href);");
				final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						message, "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}
			
			final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					message, "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	} 

	public List<DatabaseTable> getTables() {
		return tables;
	}

	public void setTables(List<DatabaseTable> tables) {
		this.tables = tables;
	}
	
	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public QueryResult getQueryResult() {
		return queryResult;
	}

	public void setQueryResult(QueryResult queryResult) {
		this.queryResult = queryResult;
	}

	public HashMap<String, QueryResult> getTableData() {
		return tableData;
	}

	public void setTableData(HashMap<String, QueryResult> tableData) {
		this.tableData = tableData;
	}
}
