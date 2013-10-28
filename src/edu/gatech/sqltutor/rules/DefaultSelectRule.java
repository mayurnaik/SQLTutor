package edu.gatech.sqltutor.rules;

import java.util.Arrays;
import java.util.List;

import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.rules.graph.LabelNode;
import edu.gatech.sqltutor.rules.graph.TranslationGraph;

public class DefaultSelectRule implements ITranslationRule {
	private static final List<String> phrases = Arrays.asList(
		"Select the",
		"List the",
		"Retrieve the",
		"Display the",
		"Fetch the",
		"Show the"
	);

	public DefaultSelectRule() {
	}

	@Override
	public int getPrecedence() {
		return 0;
	}

	@Override
	public boolean apply(TranslationGraph graph, StatementNode statement) {
		SelectNode select = QueryUtils.extractSelectNode(statement);
		if( QueryUtils.hasContributed(this, select) )
			return false;
		
		LabelNode selectLabels = graph.getVertexForAST(select);
		selectLabels.addLocalChoices(phrases);
		
		QueryUtils.getOrInitMetaData(select).addContributor(this);
		
		return true;
	}

}
