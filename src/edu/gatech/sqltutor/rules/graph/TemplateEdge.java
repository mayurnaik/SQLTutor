package edu.gatech.sqltutor.rules.graph;

public class TemplateEdge extends TranslationEdge {
	public TemplateEdge(LabelNode source, LabelNode target, String label) {
		super(source, target, label, false);
	}

}
