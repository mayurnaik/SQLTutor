package edu.gatech.sqltutor.entities;

import java.util.Date;

import beans.UserBean;

public class UserQuery {
	private UserBean user;
	private String query;
	private String schema;
	private Date time;
	
	// FIXME want a collection of these eventually
	private String naturalLanguage;
	
	private String userDescription;
	
	private Integer nlpRating;

	public UserBean getUser() {
		return user;
	}

	public void setUser(UserBean user) {
		this.user = user;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getNaturalLanguage() {
		return naturalLanguage;
	}

	public void setNaturalLanguage(String naturalLanguage) {
		this.naturalLanguage = naturalLanguage;
	}

	public String getUserDescription() {
		return userDescription;
	}

	public void setUserDescription(String userDescription) {
		this.userDescription = userDescription;
	}
	
	public Integer getRating() { return nlpRating; }
	
	public void setRating(Integer rating) { nlpRating = rating; }
	
	
}
