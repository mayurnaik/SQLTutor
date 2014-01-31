package edu.gatech.sqltutor.rules;

import org.deri.iris.api.IKnowledgeBase;

import com.akiban.sql.parser.SelectNode;

public interface ISQLTranslationRule extends ITranslationRule {
	public boolean apply(IKnowledgeBase knowledgeBase, SelectNode select);
}
