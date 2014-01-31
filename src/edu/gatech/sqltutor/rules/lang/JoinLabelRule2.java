package edu.gatech.sqltutor.rules.lang;

import static edu.gatech.sqltutor.rules.datalog.iris.IrisUtil.newLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;

import com.akiban.sql.parser.StatementNode;

import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.datalog.iris.SQLPredicates;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.ERRelationship;
import edu.gatech.sqltutor.rules.er.mapping.ERForeignKeyJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap;
import edu.gatech.sqltutor.rules.er.mapping.ERLookupTableJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.util.Pair;

public class JoinLabelRule2 extends AbstractSQLRule implements ITranslationRule {
	// rules defined statically
	private static final IPredicate joinRuleFK = Factory.BASIC.createPredicate("joinRuleFK", 5);
	private static final IPredicate joinRuleLookup = Factory.BASIC.createPredicate("joinRuleLookup", 8);
	
	private Stack<ERForeignKeyJoin> fkJoins = new Stack<ERForeignKeyJoin>();
	private Stack<ERLookupTableJoin> lookupJoins = new Stack<ERLookupTableJoin>();

	public JoinLabelRule2(ERDiagram erDiagram, ERMapping erMapping) {
		super(erDiagram, erMapping);
		findRelationships();
	}
	
	@Override
	public boolean apply(StatementNode statement) {
		select = QueryUtils.extractSelectNode(statement);
		
		try {
			if( detectFKJoin() )
				return true;
			
			if( detectLookupJoins() )
				return true;
			
			return false;
		} finally {
			select = null;
		}
	}
	
	private static ILiteral tableName(String var, String name) {
		return newLiteral(SQLPredicates.tableName, var, name);
	}
	
	private boolean detectFKJoin() {
		while( !fkJoins.isEmpty() ) {
			ERForeignKeyJoin join = fkJoins.pop();
			Pair<String,String> pk = QueryUtils.splitKeyParts(join.getKeyPair().getPrimaryKey());
			Pair<String,String> fk = QueryUtils.splitKeyParts(join.getKeyPair().getForeignKey());
			ERRelationship rel = erDiagram.getRelationship(join.getRelationship());
			
			ILiteral t1Name = tableName("?t1", pk.getFirst());
			ILiteral t2Name = tableName("?t2", fk.getFirst());
			ILiteral joinRule = newLiteral(joinRuleFK, 
				"?t1", pk.getSecond(), "?t2", fk.getSecond(), "?eq");
			
			List<IVariable> bindings = new ArrayList<IVariable>(3);
		}
		return false;
	}

	private void findRelationships() {		
		Set<ERJoinMap> joins = erMapping.getJoins();
		for( ERJoinMap join: joins ) {
			switch( join.getMapType() ) {
				case FOREIGN_KEY:
					fkJoins.push((ERForeignKeyJoin)join);
//					visitFKJoin((ERForeignKeyJoin)join);
					break;
				case LOOKUP_TABLE:
					lookupJoins.push((ERLookupTableJoin)join);
//					visitLookupTableJoin((ERLookupTableJoin)join);
					break;
				case MERGED:
//					visitMergedJoin((ERMergedJoin)join);
					break;
			}
		}
	}
	
}
