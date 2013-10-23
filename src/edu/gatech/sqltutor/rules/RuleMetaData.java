package edu.gatech.sqltutor.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RuleMetaData {
	private boolean isInOutput = true;
	private boolean isHandled;
	private String label;
	
	// FIXME not a great API, only applies to certain rules like joins
	/** Table alias an entity belongs to, e.g. label="supervisor" ofAlias="e1" where e1 is the employee table */
	private String ofAlias;
	
	/** Rules that contributed to the information in this metadata. */
	private List<ITranslationRule> rules = 
			new ArrayList<ITranslationRule>();
	
	public RuleMetaData() { }

	public List<ITranslationRule> getContributors() {
		return Collections.unmodifiableList(rules);
	}
	
	public void setContributors(List<ITranslationRule> rules) {
		this.rules.clear();
		this.rules.addAll(rules);
	}
	
	public void addContributor(ITranslationRule rule) {
		this.rules.add(rule);
	}
	
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
	
	public void setOfAlias(String ofAlias) {
		this.ofAlias = ofAlias;
	}
	
	public String getOfAlias() {
		return ofAlias;
	}
	
	public boolean isInOutput() {
		return isInOutput;
	}

	public void setInOutput(boolean isInOutput) {
		this.isInOutput = isInOutput;
	}

	@Override
	public String toString() {
		return String.format(
			"%s{handled=%b, label=\"%s\", rules=%s}",
			this.getClass().getSimpleName(), isHandled, label, rules
		);
	}
}
