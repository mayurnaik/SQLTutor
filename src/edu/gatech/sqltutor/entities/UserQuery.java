package edu.gatech.sqltutor.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

//import javax.persistence.Entity;
//import javax.persistence.FetchType;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.OneToMany;
//import javax.validation.constraints.NotNull;

import beans.UserBean;

//@Entity
public class UserQuery implements Serializable {
	private static final long serialVersionUID = 1L;
//	
//	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
//	@NotNull
	private UserBean user;
	
	private String username;
	
//	@NotNull
	private String query;
	
//	@NotNull
	private String schema;
	
	private Date time;
	
//	@OneToMany(mappedBy="query", fetch=FetchType.LAZY)
	private Collection<QueryTranslation> translations = 
		new ArrayList<QueryTranslation>();
	
	// FIXME want a collection of these eventually
	private String naturalLanguage;
	
	private String userDescription;
	
	private Integer nlpRating;

	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public UserBean getUser() {
		return user;
	}

	public String getUsername() { return username; }
	
	public void setUsername(String username) {
		this.username = username;
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
