package edu.gatech.sqltutor.rules.graph;

import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.Objects;

public class TranslationEdge extends DefaultEdge {
	private static final long serialVersionUID = 1L;

	protected LabelNode source, target;
	protected boolean isChildEdge;
	protected String label;
	
	public TranslationEdge(LabelNode source, LabelNode target) {
		this.source = source;
		this.target = target;
		this.isChildEdge = true;
	}
	
	public TranslationEdge(LabelNode source, LabelNode target, String label) {
		this.source = source;
		this.target = target;
		this.label = label;
		this.isChildEdge = false;
	}
	
	public TranslationEdge(LabelNode source, LabelNode target, String label, boolean isChildEdge) {
		this.source = source;
		this.target = target;
		this.label = label;
		this.isChildEdge = isChildEdge;
	}
	
	

	public boolean isChildEdge() {
		return isChildEdge;
	}

	public void setChildEdge(boolean isChildEdge) {
		this.isChildEdge = isChildEdge;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public LabelNode getSource() {
		return source;
	}

	public LabelNode getTarget() {
		return target;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(source, target, isChildEdge, label);
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == null || !(obj instanceof TranslationEdge) )
			return false;
		TranslationEdge that = (TranslationEdge)obj;
		
		return Objects.equal(this.source, that.source) &&
				Objects.equal(this.target, that.target) &&
				this.isChildEdge == that.isChildEdge && 
				Objects.equal(this.label, that.label);
	}
}
