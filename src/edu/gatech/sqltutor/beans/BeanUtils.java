package edu.gatech.sqltutor.beans;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class BeanUtils extends AbstractDatabaseBean {

	public static String getIPAddress() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String ip = request.getHeader("X-FORWARDED-FOR");
		if (ip == null) {
		    ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	public static String getSessionId() {
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		return session.getId();
	}	
	
	public static void addErrorMessage(String componentName, String message) {
		String componentId = getComponentId(componentName);
		FacesContext.getCurrentInstance().addMessage(componentId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
	}
	
	public static void addInfoMessage(String componentName, String message) {
		String componentId = getComponentId(componentName);
		FacesContext.getCurrentInstance().addMessage(componentId, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
	}
	
	public static String getComponentId(String componentName) {
		String componentId = null;
		if(componentName != null)
			componentId = FacesContext.getCurrentInstance().getViewRoot().findComponent(componentName).getClientId();
		return componentId;
	}
}
