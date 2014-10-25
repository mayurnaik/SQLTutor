package edu.gatech.sqltutor.beans;

import java.io.Serializable;
import java.sql.SQLException;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import com.google.common.base.Throwables;

import edu.gatech.sqltutor.DatabaseManager;

/**
 * Base class for beans using the database manager.
 */
public class AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private transient DatabaseManager databaseManager;

	public AbstractDatabaseBean() {
	}
	
	public DatabaseManager getDatabaseManager() {
		if (databaseManager == null) {
			FacesContext ctx = FacesContext.getCurrentInstance();
			Application app = ctx.getApplication();
			databaseManager = app.evaluateExpressionGet(ctx, "#{databaseManager}", DatabaseManager.class);
		}
		return databaseManager;
	}
	
	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}
	
	public void logException(Throwable e, String email) {
		try {
			getDatabaseManager().logException(BeanUtils.getSessionId(), email, Throwables.getStackTraceAsString(e));
		} catch (SQLException e1) {
			for(Throwable t : e1)
				t.printStackTrace();
		}
	}
}
