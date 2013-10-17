package edu.gatech.sqltutor;

public class RuleMetaData {
	private boolean isHandled;
	private String label;
	
	public RuleMetaData() { }

	public boolean isHandled() {
		return isHandled;
	}

	public void setHandled(boolean isHandled) {
		this.isHandled = isHandled;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return String.format(
			"%s{handled=%b, label=\"%s\"}",
			this.getClass().getSimpleName(), isHandled, label
		);
	}
}
