package edu.gatech.sqltutor.rules.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.akiban.sql.parser.QueryTreeNode;
import com.google.common.base.Objects;

public class LabelNode {
	protected QueryTreeNode astNode;
	protected boolean isChildSuppressing;
	
	protected List<String> localChoices = new ArrayList<String>();
	protected List<List<String>> childChoices;
	protected List<List<String>> outputChoices;

	public LabelNode() {
		// TODO Auto-generated constructor stub
	}

	public void modified() {
		outputChoices = null;
	}
	
	public void addLocalChoice(String choice) {
		localChoices.add(choice);
		modified();
	}
	
	public void addLocalChoices(Collection<String> choices) {
		localChoices.addAll(choices);
		modified();
	}
	
	public List<String> getLocalChoices() {
		return localChoices;
	}
	
	protected List<List<String>> singletonLocalChoices() {
		List<List<String>> list = new ArrayList<List<String>>(localChoices.size());
		for( String label: localChoices ) {
			list.add(Collections.singletonList(label));
		}
		return list;
	}
	
	public List<List<String>> getChoices() {
		if( outputChoices == null ) {
			List<List<String>> choices = singletonLocalChoices();
			if( childChoices != null ) {
				if( isChildSuppressing )
					choices.addAll(childChoices); // add children as mutually exclusive alternatives
				else
					choices = GraphUtils.mergeLists(choices, childChoices); // generate combinations of choices
			}
			outputChoices = choices;
		}
		return outputChoices;
	}


	public QueryTreeNode getAstNode() {
		return astNode;
	}


	public void setAstNode(QueryTreeNode astNode) {
		this.astNode = astNode;
	}


	public boolean isChildSuppressing() {
		return isChildSuppressing;
	}


	public void setChildSuppressing(boolean isChildSuppressing) {
		this.isChildSuppressing = isChildSuppressing;
	}


	public void setLocalLabels(List<String> localLabels) {
		List<String> oldChoices = this.localChoices;
		this.localChoices = localLabels;
		if( localLabels != oldChoices )
			modified();
	}
	
	public void setChildChoices(List<List<String>> childChoices) {
		List<List<String>> oldChoices = this.childChoices;
		this.childChoices = childChoices;
		if( childChoices != oldChoices )
			modified();
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("isChildSupressing", isChildSuppressing)
			.add("localChoices", localChoices)
			.toString();
	}
}
