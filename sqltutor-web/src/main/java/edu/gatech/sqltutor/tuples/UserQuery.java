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
package edu.gatech.sqltutor.tuples;

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

import edu.gatech.sqltutor.beans.UserBean;

//@Entity
public class UserQuery implements Serializable {
	private static final long serialVersionUID = 1L;
//	
//	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
//	@NotNull
	private UserBean user;
	
	private String email;
	
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

	private String source;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
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

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
