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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class AdminCodesPageBean extends AbstractDatabaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManagedProperty(value="#{userBean}")
	private UserBean userBean;
	
	private static final String CHOOSE_CODE_ERROR = "Please select a code.";
	private static final String REMOVE_CODE_ERROR = "You are not allowed to remove your own admin code, or the \"examples\" admin code.";
	private static final String INVALID_CODE_ERROR = "You entered an invalid admin code.";
	private static final String NONEXISTANT_CODE_ERROR = "This admin code doesn't exist.";
	private static final String ALREADY_LINKED_ERROR = "You are already linked to this admin code.";
	private static final String EXAMPLES_ADMIN_CODE = "xgFabbA";
	private static final String UNLINK_MESSAGES_NAME = "panel2";
	private static final String LINK_MESSAGES_NAME = "panel1";
	
	
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
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(UNLINK_MESSAGES_NAME, DATABASE_ERROR_MESSAGE);
		} 
	}
	
	public void unlinkCode() {
		if(selectedCode == null || selectedCode == "") {
			BeanUtils.addErrorMessage(UNLINK_MESSAGES_NAME, CHOOSE_CODE_ERROR);
		} else if(selectedCode.equals(userBean.getAdminCode()) || selectedCode.equals(EXAMPLES_ADMIN_CODE)) {
			BeanUtils.addErrorMessage(UNLINK_MESSAGES_NAME, REMOVE_CODE_ERROR);
		} else {
			try {
				getDatabaseManager().unlinkAdminCode(userBean.getHashedEmail(), selectedCode);
				codes.remove(selectedCode);
				final String message = "Successfully unlinked \"" + selectedCode + "\".";
				BeanUtils.addInfoMessage(UNLINK_MESSAGES_NAME, message);
				selectedCode = "";
			} catch (SQLException e) {
				for(Throwable t : e) {
					t.printStackTrace();
					logException(t, userBean.getHashedEmail());
				}
				BeanUtils.addErrorMessage(UNLINK_MESSAGES_NAME, DATABASE_ERROR_MESSAGE);
			}
		}
	}
	
	public void linkCode() {
		try {
			if (code == null || code.equals("") || code.length() != 7) {
				BeanUtils.addErrorMessage(LINK_MESSAGES_NAME, INVALID_CODE_ERROR);
			} else if (!getDatabaseManager().adminCodeExists(code)) {
				BeanUtils.addErrorMessage(LINK_MESSAGES_NAME, NONEXISTANT_CODE_ERROR);
			} else if (codes.contains(code)) {
				BeanUtils.addErrorMessage(LINK_MESSAGES_NAME, ALREADY_LINKED_ERROR);
			} else {
					getDatabaseManager().linkAdminCode(userBean.getHashedEmail(), code);
					codes.add(code);
					final String message = "Successfully linked \"" + code + "\".";
					BeanUtils.addInfoMessage(LINK_MESSAGES_NAME, message);
			}
		} catch (SQLException e) {
			for(Throwable t : e) {
				t.printStackTrace();
				logException(t, userBean.getHashedEmail());
			}
			BeanUtils.addErrorMessage(LINK_MESSAGES_NAME, DATABASE_ERROR_MESSAGE);
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
