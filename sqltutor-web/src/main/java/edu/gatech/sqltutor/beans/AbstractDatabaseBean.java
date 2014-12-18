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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import com.google.common.base.Throwables;

import edu.gatech.sqltutor.ColumnInfo;
import edu.gatech.sqltutor.DatabaseManager;
import edu.gatech.sqltutor.DatabaseTable;

/**
 * Base class for beans using the database manager.
 */
public class AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private transient DatabaseManager databaseManager;
	
	protected static final String DATABASE_ERROR = "Internal database error. Please try again momentarily.";

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
	
	// TODO find an appropriate place for this
	public List<String> getColumnNames(DatabaseTable tableInfo) {
		List<ColumnInfo> cols = tableInfo.getColumns();
		List<String> names = new ArrayList<>(cols.size());
		for( ColumnInfo col: cols )
			names.add(col.getName());
		return names;
	}
}
