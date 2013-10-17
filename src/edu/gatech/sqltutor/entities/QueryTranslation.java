package edu.gatech.sqltutor.entities;

//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.validation.constraints.NotNull;

//@Entity
public class QueryTranslation {
//	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private Long queryId;
	
//	@NotNull
	private String text;
	
	public QueryTranslation() { }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getQueryId() {
		return queryId;
	}

	public void setQueryId(Long queryId) {
		this.queryId = queryId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
