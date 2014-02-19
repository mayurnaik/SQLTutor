package edu.gatech.sqltutor.rules.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.deri.iris.EvaluationException;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.unparser.NodeToString;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.gatech.sqltutor.IQueryTranslator;
import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.AbstractQueryTranslator;
import edu.gatech.sqltutor.rules.ISQLTranslationRule;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.SQLState;
import edu.gatech.sqltutor.rules.datalog.iris.ERFacts;
import edu.gatech.sqltutor.rules.datalog.iris.ERPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.ERRules;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.LearnedPredicates;
import edu.gatech.sqltutor.rules.datalog.iris.SQLFacts;
import edu.gatech.sqltutor.rules.datalog.iris.SQLRules;
import edu.gatech.sqltutor.rules.datalog.iris.StaticRules;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;

public class SymbolicFragmentTranslator 
		extends AbstractQueryTranslator implements IQueryTranslator {
	private static final Logger _log = 
		LoggerFactory.getLogger(SymbolicFragmentTranslator.class);
	
	// FIXME temp flag to enable non-logging debug output
	private static final boolean DEBUG = true;
	
	protected ERFacts erFacts = new ERFacts();
	protected SQLFacts sqlFacts = new SQLFacts();
	protected ERDiagram erDiagram;
	protected ERMapping erMapping;
	protected boolean withDefaults;
	protected boolean defaultsAdded;

	public SymbolicFragmentTranslator() {
		this(true);
	}
	
	public SymbolicFragmentTranslator(boolean withDefaults) {
		translationRules = new ArrayList<ITranslationRule>();
		this.withDefaults = withDefaults;
	}
	
	@Override
	protected void computeTranslation() throws SQLTutorException {
		if( erDiagram == null ) throw new SQLTutorException("No ER diagram set.");
		if( erMapping == null ) throw new SQLTutorException("No ER-relational mapping set.");
		erMapping.setDiagram(erDiagram);
		
		if( withDefaults && !defaultsAdded ) {
			translationRules.addAll(makeDefaultRules());
			defaultsAdded = true;
		}
		
		// ER diagram generated once now
		// TODO may need to adjust if updating
		erFacts.generateFacts(erDiagram);
		erFacts.generateFacts(erMapping);
		
		StatementNode statement = parseQuery();
		SelectNode select = QueryUtils.extractSelectNode(statement);
		
		SQLState sqlState = new SQLState();
		sqlState.setErDiagram(erDiagram);
		sqlState.setErMapping(erMapping);
		sqlState.setAst(select);
		sqlState.setSqlFacts(sqlFacts);
		sqlState.setErFacts(erFacts);
		loadStaticRules();
		
		IKnowledgeBase kb = createSQLKnowledgeBase(select, sqlState);
		sqlState.setKnowledgeBase(kb);
		
		sortRules();
		for( ITranslationRule rule: translationRules ) {
			switch( rule.getType() ) {
				case ITranslationRule.TYPE_SQL: {
					ISQLTranslationRule sqlRule = (ISQLTranslationRule)rule;
					while( sqlRule.apply(sqlState) ) {
						kb = createSQLKnowledgeBase(select, sqlState); // regenerate as update may be destructive
						sqlState.setKnowledgeBase(kb);
						
						// apply each rule as many times as possible
						// FIXME non-determinism when precedences match?
						_log.debug("Applied rule: {}", rule);
					}
					break;
				}
				case ITranslationRule.TYPE_SYMBOLIC: {
					ISymbolicTranslationRule symRule = (ISymbolicTranslationRule)rule;
					_log.error("FIXME: Symbolic handling not implemented.");
					break;
				}
				default:
					throw new SQLTutorException("Unknown rule type for rule: " + rule);
			}
		}
		
		if( _log.isInfoEnabled() )
			_log.info("statement: {}", QueryUtils.nodeToString(statement));
		
		// FIXME remove this eventually
		if( DEBUG ) {
			IrisUtil.dumpFacts(makeFacts(sqlState));
			IrisUtil.dumpRules(staticRules);
			
			dumpQuery(kb, Factory.BASIC.createQuery(
				IrisUtil.literal(LearnedPredicates.tableLabel, "?table", "?label", "?source")
			));
			dumpQuery(kb, Factory.BASIC.createQuery(
				IrisUtil.literal(ERPredicates.erFKJoin, "?rel", "?pktable", "?pkattr", "?fktable", "?fkattr")
			));
			dumpQuery(kb, Factory.BASIC.createQuery(
				IrisUtil.literal(ERPredicates.erFKJoinSides, "?rel", "?pkSide", "?fkSide")
			));
		}
		
		throw new SQLTutorException("FIXME: Not implemented.");
	}
	
	private static void dumpQuery(IKnowledgeBase kb, IQuery q) {
		try {
			IRelation rel = kb.execute(q);
			for( int i = 0; i < rel.size(); ++i ) {
				System.out.println(rel.get(i));
			}
		} catch( EvaluationException e ) {
			e.printStackTrace();
		}
	}
	
	private List<IRule> staticRules;
	private void loadStaticRules() {
		SQLRules sqlRules = SQLRules.getInstance();
		ERRules erRules = ERRules.getInstance();
		
		staticRules = Lists.newArrayList();
		staticRules.addAll(sqlRules.getRules());
		staticRules.addAll(erRules.getRules());
		staticRules.addAll(astRules.getRules());
		for( ITranslationRule rule: translationRules ) {
			staticRules.addAll(rule.getDatalogRules());
		}
	}
	
	// FIXME add datalog rules on a per-meta-rule basis?
	@Deprecated
	private static final StaticRules astRules = new StaticRules("/astrules.dlog");
	
	private static Map<IPredicate, IRelation> mergeFacts(Map<IPredicate, IRelation>... facts) {
		int size = 1;
		for( Map<IPredicate, IRelation> someFacts: facts )
			size += someFacts.size();
		Map<IPredicate, IRelation> mergedFacts = Maps.newHashMapWithExpectedSize(size);
		for( Map<IPredicate, IRelation> someFacts: facts )
			mergedFacts.putAll(someFacts);
		return mergedFacts;
	}
	
	protected Map<IPredicate, IRelation> makeFacts(SQLState state) {
		SQLRules sqlRules = SQLRules.getInstance();
		ERRules erRules = ERRules.getInstance();
		@SuppressWarnings("unchecked")
		Map<IPredicate, IRelation> facts = mergeFacts(		
			sqlFacts.getFacts(),
			sqlRules.getFacts(),
			erFacts.getFacts(),
			erRules.getFacts(),
			astRules.getFacts(),
			state.getRuleFacts()
		);
		return facts;
	}
	
	protected IKnowledgeBase createSQLKnowledgeBase(SelectNode select, SQLState state) {
		long duration = -System.currentTimeMillis();
		sqlFacts.generateFacts(select, true);
		Map<IPredicate, IRelation> facts = makeFacts(state);
		
		List<IRule> rules = staticRules;
		
		_log.info("KB creation prep in {} ms.", duration + System.currentTimeMillis());
		
		try {
			duration = -System.currentTimeMillis();
			IKnowledgeBase kb = KnowledgeBaseFactory.createKnowledgeBase(facts, rules);
			_log.info("KB creation in {} ms.", duration + System.currentTimeMillis());
			return kb;
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
	}

	private Collection<ITranslationRule> makeDefaultRules() {
		return Arrays.<ITranslationRule>asList(
			new JoinLabelRule(),
			new DefaultTableLabelRule(),
			new DescribingAttributeLabelRule()
		);
	}
	
	@Override
	public void clearResult() {
		super.clearResult();
		sqlFacts.reset();
		erFacts.reset();
		defaultsAdded = false;
	}

	@Override
	public Object getTranslatorType() {
		return "Symbolic Language Fragments";
	}

	public ERDiagram getERDiagram() {
		return erDiagram;
	}

	public void setERDiagram(ERDiagram erDiagram) {
		this.erDiagram = erDiagram;
		clearResult();
	}

	public ERMapping getERMapping() {
		return erMapping;
	}

	public void setERMapping(ERMapping erMapping) {
		this.erMapping = erMapping;
		clearResult();
	}
}
