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

@ManagedBean
@ViewScoped
public class AdminCodesPageBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	private List<String> codes;
	private String selectedCode;
	private String code;
	
	@PostConstruct
	public void init() {
		try {
			codes = getDatabaseManager().getLinkedAdminCodes(userBean.getHashedEmail());
			selectedCode = "";
			code = "";
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getEmail());
			}
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
				getDatabaseManager().unlinkCode(userBean.getHashedEmail(), selectedCode);
				codes.remove(selectedCode);
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Successfully unlinked \"" + selectedCode + "\".", "");
				selectedCode = "";
			} catch (SQLException e) {
				for(Throwable t : e) {
					t.printStackTrace();
					logException(t, userBean.getEmail());
				}
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
					getDatabaseManager().linkCode(userBean.getHashedEmail(), code);
					codes.add(code);
					msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Successfully linked \"" + code + "\".", "");
			}
			FacesContext.getCurrentInstance().addMessage("panel1", msg);
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getEmail());
			}
		}
	}

	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
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
