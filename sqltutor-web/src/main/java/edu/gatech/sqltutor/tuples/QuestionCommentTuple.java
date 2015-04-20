package edu.gatech.sqltutor.tuples;

import java.io.Serializable;

public class QuestionCommentTuple implements Serializable {
	private static final long serialVersionUID = 1L;

	private int order;
	private String comment;
	private String email;
	private String schema;
	private String adminCode;
	
	public QuestionCommentTuple(int order, String comment, String email, String schema, String adminCode) {
		this.order = order;
		this.comment = comment;
		this.email = email;
		this.schema = schema;
		this.adminCode = adminCode;
	}
	
	public int getOrder() {
		return order;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getAdminCode() {
		return adminCode;
	}

	public void setAdminCode(String adminCode) {
		this.adminCode = adminCode;
	}
}
