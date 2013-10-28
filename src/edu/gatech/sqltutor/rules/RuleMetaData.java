package edu.gatech.sqltutor.rules;

import java.util.LinkedHashSet;
import java.util.Set;


public class RuleMetaData {
	private boolean isInOutput = true;
	private boolean isHandled;
	private String label;
	
	// FIXME not a great API, only applies to certain rules like joins
	/** Table alias an entity belongs to, e.g. label="supervisor" ofAlias="e1" where e1 is the employee table */
	private String ofAlias;
	
	/** Rules that contributed to the information in this metadata. */
	private Set<ITranslationRule> contributors = 
		new LinkedHashSet<ITranslationRule>();
	
	public RuleMetaData() { }

	public Set<ITranslationRule> getContributors() {
		return this.contributors;
	}
	
	public void addContributor(ITranslationRule rule) {
		this.contributors.add(rule);
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
			this.getClass().getSimpleName(), isHandled, label, contributors
		);
	}
}
