package edu.gatech.sqltutor.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.FromBaseTable;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultColumn;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.Visitable;
import com.akiban.sql.parser.Visitor;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.graph.LabelNode;
import edu.gatech.sqltutor.rules.graph.TranslationGraph;

public class DefaultLabelRule implements ITranslationRule {
	private static final Logger log = LoggerFactory.getLogger(DefaultLabelRule.class);
	
	public DefaultLabelRule() {
	}
	
	public String transform(String name) {
		return name.replace('_', ' ');
	}
	
	@Override
	public int getPrecedence() {
		return 0;
	}
	
	@Override
	public boolean apply(final TranslationGraph graph, StatementNode statement) {
		try {
			class RuleVisitor implements Visitor {
				private boolean wasApplied;
				
				@Override
				public Visitable visit(Visitable node) throws StandardException {
					QueryTreeNode astNode = (QueryTreeNode)node;
					
					// don't apply more than once to a single node
					RuleMetaData meta = (RuleMetaData)astNode.getUserData();
					if( meta != null && meta.getContributors().contains(DefaultLabelRule.this) )
						return astNode;
					
					String label = null;
					switch( astNode.getNodeType() ) {
						case NodeTypes.FROM_BASE_TABLE:
							label = ((FromBaseTable)astNode).getOrigTableName().getTableName();
							break;
						case NodeTypes.COLUMN_REFERENCE: {
							ColumnReference ref = (ColumnReference)astNode;
							ResultColumn col = ref.getSourceResultColumn();
							if( col != null )
								label = col.getColumnName();
							else
								label = ref.getColumnName();
							break;
						}
						case NodeTypes.RESULT_COLUMN:
							label = ((ResultColumn)astNode).getColumnName();
//							log.info("Visiting RESULT_COLUMN (label={}): {}", label, astNode);
							break;
						default:
							break;
					}
					
					if( label != null ) {
						label = transform(label);
						LabelNode vertex = graph.getVertexForAST(astNode);
//						if( vertex == null ) {
//							vertex = new LabelNode();
//							vertex.setAstNode(astNode);
//							graph.addVertex(vertex);
//						}
						if( vertex == null ) {
							log.warn("No vertex for node: {}", astNode);
						} else {
//							wasApplied = true;
							vertex.getLocalChoices().add(label);
//							QueryUtils.getOrInitMetaData(astNode).addContributor(DefaultLabelRule.this);
							log.debug("Added label '{}' to vertex {}", label, vertex);
						}
						wasApplied = true;
						QueryUtils.getOrInitMetaData(astNode).addContributor(DefaultLabelRule.this);
					}
					
					return astNode;
				}
				
				@Override
				public boolean stopTraversal() {
					return false;
				}
				
				@Override
				public boolean visitChildrenFirst(Visitable node) {
					return true;
				}
				
				@Override
				public boolean skipChildren(Visitable node) throws StandardException {
					return QueryUtils.isHandled((QueryTreeNode)node);
				}
				
				public boolean wasApplied() { return wasApplied; }
			}
			
			RuleVisitor v = new RuleVisitor();
			statement.accept(v);
			return v.wasApplied();
		} catch( StandardException e ) {
			throw new SQLTutorException(e);
		}
	}

}
