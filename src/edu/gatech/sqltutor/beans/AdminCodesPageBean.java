package edu.gatech.sqltutor.beans;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import edu.gatech.sqltutor.DatabaseManager;

@ManagedBean
@ViewScoped
public class AdminCodesPageBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	@ManagedProperty(value="#{databaseManager}")
	private DatabaseManager databaseManager;
	
	private List<String> codes;
	private String selectedCode;
	private String code;
	
	@PostConstruct
	public void init() {
		try {
			codes = getDatabaseManager().getLinkedAdminCodes(userBean.getEmail());
			selectedCode = "";
			code = "";
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void unlinkCode() {
		FacesMessage msg = null;
		if(selectedCode == null || selectedCode == "") {
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Please select a code.", "");
		} else if(selectedCode.equals(userBean.getAdminCode()) || selectedCode.equals("xgFabbA")) {
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"You are not allowed to remove your own admin code, or the \"examples\" admin code.", "");
		} else {
			try {
				getDatabaseManager().unlinkCode(userBean.getEmail(), selectedCode);
				codes.remove(selectedCode);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Successfully unlinked \"" + selectedCode + "\".", "");
				selectedCode = "";
			} catch (SQLException e) {
				if(e.getNextException() != null)
					e.getNextException().printStackTrace();
				else
					e.printStackTrace();
			}
		}
		FacesContext.getCurrentInstance().addMessage("panel2", msg);
	}
	
	public void linkCode() {
		try {
			FacesMessage msg = null;
			if(code == null || code.equals("") || code.length() != 7) {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"You entered an invalid admin code.", "");
			} else if(!getDatabaseManager().adminCodeExists(code)) {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"This admin code doesn't exist.", "");
			} else if(codes.contains(code)) {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"You are already linked to this admin code.", "");
			} else {
					getDatabaseManager().linkCode(userBean.getEmail(), code);
					codes.add(code);
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Successfully linked \"" + code + "\".", "");
			}
			FacesContext.getCurrentInstance().addMessage("panel1", msg);
		} catch (SQLException e) {
			if(e.getNextException() != null)
				e.getNextException().printStackTrace();
			else
				e.printStackTrace();
		}
	}

	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public List<String> getCodes() {
		return codes;
	}

	public void setCodes(List<String> codes) {
		this.codes = codes;
	}

	public String getSelectedCode() {
		return selectedCode;
	}

	public void setSelectedCode(String selectedCode) {
		this.selectedCode = selectedCode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
