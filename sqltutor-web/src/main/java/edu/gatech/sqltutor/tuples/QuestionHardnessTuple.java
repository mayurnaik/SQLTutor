package edu.gatech.sqltutor.tuples;

import java.io.Serializable;

public class QuestionHardnessTuple implements Comparable<QuestionHardnessTuple>, Serializable {
	private static final long serialVersionUID = 1L;
	
	private int order;
	private int sum;
	private double avg;
	private double hardnessRating;
	
	public QuestionHardnessTuple(int order, double hardnessRating, int sum, double avg) {
		this.order = order;
		this.hardnessRating = hardnessRating;
		this.sum = sum;
		this.avg = avg;
	}
	
	public double getHardnessRating() {
		return hardnessRating;
	}
	
	public void setHardnessRating(double hardnessRating) {
		this.hardnessRating = hardnessRating;
	}

	@Override
	public int compareTo(QuestionHardnessTuple questionHardnessTuple) {
		return hardnessRating < questionHardnessTuple.getHardnessRating() ? -1 : hardnessRating > questionHardnessTuple.getHardnessRating() ? 1 : 0;
	}

	public int getSum() {
		return sum;
	}

	public void setSum(int sum) {
		this.sum = sum;
	}

	public double getAvg() {
		return avg;
	}

	public void setAvg(double avg) {
		this.avg = avg;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
