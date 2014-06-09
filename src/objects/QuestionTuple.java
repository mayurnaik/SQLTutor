package objects;

public class QuestionTuple {
	private int order;
	private String question;
	private String answer;
	private int id;
	
	public QuestionTuple(int order, String question, String answer, int id) {
		this.order = order;
		this.question = question;
		this.answer = answer;
		this.id = id;
	}
	
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
