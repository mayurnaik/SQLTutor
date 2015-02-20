package edu.gatech.sqltutor.entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.beans.BeanUtils;

public class SchemaOptionsTuple implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private boolean visibleToUsers;
	private boolean  inOrderQuestions;
	private Timestamp openAccess;
	private Timestamp closeAccess;
	private String link;
	
	public SchemaOptionsTuple(boolean visibleToUsers, boolean inOrderQuestions, String link,
			Timestamp openAccess, Timestamp closeAccess) {
		super();
		this.setLink(link);
		this.visibleToUsers = visibleToUsers;
		this.inOrderQuestions = inOrderQuestions;
		this.openAccess = openAccess;
		this.closeAccess = closeAccess;
	}

	public SchemaOptionsTuple(SchemaOptionsTuple options) {
		super();
		this.visibleToUsers = options.isVisibleToUsers();
		this.inOrderQuestions = options.isInOrderQuestions();
		this.link = options.getLink();
		this.openAccess = options.getOpenAccess();
		this.closeAccess = options.getCloseAccess();
	}

	public boolean isVisibleToUsers() {
		return visibleToUsers;
	}

	public void setVisibleToUsers(boolean visibleToUsers) {
		this.visibleToUsers = visibleToUsers;
	}

	public boolean isInOrderQuestions() {
		return inOrderQuestions;
	}

	public void setInOrderQuestions(boolean inOrderQuestions) {
		this.inOrderQuestions = inOrderQuestions;
	}

	public Timestamp getOpenAccess() {
		return openAccess;
	}
	
	public java.util.Date getOpenAccessDate() {
		if(openAccess != null) {
			final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
			cal.setTime(openAccess);
			return new java.util.Date(cal.getTimeInMillis());
		} else
			return openAccess;
	}

	public void setOpenAccess(Timestamp openAccess) {
		this.openAccess = openAccess;
	}
	
	public void setOpenAccessDate(java.util.Date openAccess) {
		if(openAccess != null) {
			final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
			cal.setTime(openAccess);
			cal.set(Calendar.MILLISECOND, 0);
			this.openAccess = new Timestamp(cal.getTimeInMillis());
		} else
			this.openAccess = null;
	}

	public Timestamp getCloseAccess() {
		return closeAccess;
	}
	
	public java.util.Date getCloseAccessDate() {
		if(closeAccess != null) {
			final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
			cal.setTime(closeAccess);
			return new java.util.Date(cal.getTimeInMillis());
		} else
			return closeAccess;
	}

	public void setCloseAccess(Timestamp closeAccess) {
		this.closeAccess = closeAccess;
	}
	
	public void setCloseAccessDate(java.util.Date closeAccess) {
		if(closeAccess != null) {
			final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"));
			cal.setTime(closeAccess);
			cal.set(Calendar.MILLISECOND, 0);
			this.closeAccess = new Timestamp(cal.getTimeInMillis());
		} else
			this.closeAccess = null;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
}
