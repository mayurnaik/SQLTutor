package edu.gatech.sqltutor.rules.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.deri.iris.EvaluationException;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.StatementNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.gatech.sqltutor.IQueryTranslator;
import edu.gatech.sqltutor.QueryUtils;
import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.rules.AbstractQueryTranslator;
import edu.gatech.sqltutor.rules.ISQLTranslationRule;
import edu.gatech.sqltutor.rules.ISymbolicTranslationRule;
import edu.gatech.sqltutor.rules.ITranslationRule;
import edu.gatech.sqltutor.rules.Markers;
import edu.gatech.sqltutor.rules.SQLState;
import edu.gatech.sqltutor.rules.SymbolicState;
import edu.gatech.sqltutor.rules.datalog.iris.ERFacts;
import edu.gatech.sqltutor.rules.datalog.iris.ERRules;
import edu.gatech.sqltutor.rules.datalog.iris.IrisUtil;
import edu.gatech.sqltutor.rules.datalog.iris.SQLFacts;
import edu.gatech.sqltutor.rules.datalog.iris.SQLRules;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicFacts;
import edu.gatech.sqltutor.rules.datalog.iris.SymbolicRules;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.rules.symbolic.SymbolicCreator;
import edu.gatech.sqltutor.rules.symbolic.SymbolicCreatorNew;
import edu.gatech.sqltutor.rules.symbolic.SymbolicReader;
import edu.gatech.sqltutor.rules.symbolic.SymbolicUtil;
import edu.gatech.sqltutor.rules.symbolic.UnhandledSymbolicTypeException;
import edu.gatech.sqltutor.rules.symbolic.tokens.RootToken;

public class SymbolicFragmentTranslator 
		extends AbstractQueryTranslator implements IQueryTranslator {
	private static final Logger _log = 
		LoggerFactory.getLogger(SymbolicFragmentTranslator.class);
	
	// FIXME temp flag to enable non-logging debug output
	private static final boolean DUMP_DATALOG = false;
	private static final boolean DUMP_SYMBOLIC_REWRITES = false;
	
	protected ERFacts erFacts = new ERFacts();
	protected SQLFacts sqlFacts = new SQLFacts();
	protected SymbolicFacts symFacts = new SymbolicFacts();
	protected List<String> outputs;
	
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
		final boolean SYM_DEBUG = _log.isDebugEnabled(Markers.SYMBOLIC);
		this.result = null;
		this.outputs = new ArrayList<String>();
		
		long duration = -System.currentTimeMillis();
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
		
		// parse query
		StatementNode statement = parseQuery();
		SelectNode select = QueryUtils.extractSelectNode(statement);

		// create initial symbolic state
		RootToken symbolic = new SymbolicCreatorNew(select).makeSymbolic();
		SymbolicState symState = new SymbolicState();
		symState.setErDiagram(erDiagram);
		symState.setErMapping(erMapping);
		symState.setErFacts(erFacts);
		symState.setSymbolicFacts(symFacts);
		buildMaps();
		loadStaticRules();

		Map<IPredicate, IRelation> queryFacts = makeFacts(symState);
//		symFacts.setNodeMap(sqlFacts.getNodeMap());
		IKnowledgeBase kb = createSymbolicKnowledgeBase(queryFacts, symbolic);
		symState.setKnowledgeBase(kb);
		
//		SQLState sqlState = new SQLState();
//		sqlState.setErDiagram(erDiagram);
//		sqlState.setErMapping(erMapping);
//		sqlState.setAst(select);
//		sqlState.setSqlFacts(sqlFacts);
//		sqlState.setErFacts(erFacts);
//		loadStaticRules();
		
		
//		IKnowledgeBase kb = createSQLKnowledgeBase(select, sqlState);
//		sqlState.setKnowledgeBase(kb);
		
		sortRules();
		
//		// apply analysis rules to discover new facts
//		for( ISQLTranslationRule sqlRule: 
//				Iterables.filter(translationRules, ISQLTranslationRule.class) ) {
//			while( sqlRule.apply(sqlState) ) {
//				kb = createSQLKnowledgeBase(select, sqlState); // regenerate as update may be destructive
//				sqlState.setKnowledgeBase(kb);
//				
//				// apply each rule as many times as possible
//				// FIXME non-determinism when precedences match?
//				_log.debug(Markers.METARULE, "Applied rule: {}", sqlRule);
//			}
//			
//		}
		
		if( _log.isInfoEnabled() )
			_log.info("statement: {}", QueryUtils.nodeToString(statement));
		
//		// all non-symbolic facts and rules are now frozen
//		Map<IPredicate, IRelation> queryFacts = makeFacts(sqlState);
//		queryFacts.putAll(SymbolicRules.getInstance().getFacts());
//		staticRules.addAll(SymbolicRules.getInstance().getRules());
//		symFacts.setNodeMap(sqlFacts.getNodeMap());
//		
//		// create initial symbolic state
//		RootToken symbolic = makeSymbolic(sqlState);
//		if( _log.isDebugEnabled(Markers.SYMBOLIC) )
			_log.info(Markers.SYMBOLIC, "Initial symbolic state: {}", SymbolicUtil.prettyPrint(symbolic));
		
//		SymbolicState symState = new SymbolicState(sqlState);
//		symState.setSymbolicFacts(symFacts);
//		kb = createSymbolicKnowledgeBase(queryFacts, symbolic);
//		symState.setKnowledgeBase(kb);
		
		// FIXME remove this eventually
		if( DUMP_DATALOG ) {
			IrisUtil.dumpFacts(queryFacts);
			symFacts.generateFacts(symbolic, false);
			IrisUtil.dumpFacts(symFacts.getFacts());
			IrisUtil.dumpRules(staticRules);
		}
		
		// perform rewriting rules
		SymbolicReader symReader = new SymbolicReader();
		List<ISymbolicTranslationRule> symbolicRules = getSymbolicMetarules();
		// track states seen
		HashSet<String> symbolicStates = new HashSet<String>();
		symbolicStates.add(symbolic.toString());
		boolean sawNewState = false;
		
		do {
			sawNewState = false;
			for( ISymbolicTranslationRule metarule: symbolicRules ) { 
				while( metarule.apply(symState) ) {
					if( DUMP_SYMBOLIC_REWRITES && SYM_DEBUG) {
						_log.debug(Markers.SYMBOLIC, "Transformed symbolic state:\n{}", SymbolicUtil.prettyPrint(symbolic));
					}
					@SuppressWarnings("unchecked")
					Map<IPredicate, IRelation> facts = mergeFacts(queryFacts, symState.getRuleFacts());
					kb = createSymbolicKnowledgeBase(/*queryFacts*/facts, symbolic);
					symState.setKnowledgeBase(kb);
					
					// FIXME non-determinism and final output checks
					if( SymbolicUtil.areAllLeavesLiterals(kb) ) {
						try {
							String output = symReader.readSymbolicState(symbolic);
							this.outputs.add(output);
							_log.info("Output: {}", output);
							if( this.result == null || Math.random() < 0.5d )
								this.result = output;
						} catch ( UnhandledSymbolicTypeException e ) {
							_log.warn("Could not read output due to unhandled type: {}", e.getSymbolicType());
						}
					}
					
					// apply each rule as many times as possible
					// FIXME non-determinism when precedences match?
					_log.info(Markers.METARULE, "Applied rule: {}", metarule);
					_log.trace(Markers.SYMBOLIC, "New symbolic state: {}", symbolic);
					
					if( symbolicStates.add(symbolic.toString()) )
						sawNewState = true;
				}
				_log.debug(Markers.METARULE, "Done with metarule: {}", metarule);
			}
		} while( sawNewState );
		
		duration += System.currentTimeMillis();
		_log.info(Markers.TIMERS, "Total translation time: {} ms", duration);
		
		_log.info(Markers.SYMBOLIC, "Final symbolic state: {}", SymbolicUtil.prettyPrint(symbolic));
		
		_log.info(Markers.SYMBOLIC, "Saw {} total symbolic states.", symbolicStates.size());
		
		if( this.result == null )
			throw new SQLTutorException("No concrete translation was computed.");
	}
	
	private List<ISymbolicTranslationRule> getSymbolicMetarules() {
		return ImmutableList.copyOf(Iterables.filter(translationRules, ISymbolicTranslationRule.class));
	}
	
	private List<ISQLTranslationRule> getAnalysisMetarules() {
		return ImmutableList.copyOf(Iterables.filter(translationRules, ISQLTranslationRule.class));
	}
	
	private IKnowledgeBase createSymbolicKnowledgeBase(Map<IPredicate, IRelation> queryFacts, 
			RootToken symbolic) {

		long duration = -System.currentTimeMillis();
		symFacts.generateFacts(symbolic, false);
		@SuppressWarnings("unchecked")
		Map<IPredicate, IRelation> facts = mergeFacts(queryFacts, symFacts.getFacts());
		
		List<IRule> rules = staticRules;
		
		_log.debug(Markers.TIMERS_FINE, "KB creation prep in {} ms.", duration + System.currentTimeMillis());
		
		try {
			duration = -System.currentTimeMillis();
			IKnowledgeBase kb = KnowledgeBaseFactory.createKnowledgeBase(facts, rules);
			_log.debug(Markers.TIMERS_FINE, "KB creation in {} ms.", duration + System.currentTimeMillis());
			return kb;
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
	}
	
	private List<IRule> staticRules;
	private void loadStaticRules() {
		SQLRules sqlRules = SQLRules.getInstance();
		ERRules erRules = ERRules.getInstance();
		
		staticRules = Lists.newArrayList();
		staticRules.addAll(sqlRules.getRules());
		staticRules.addAll(erRules.getRules());
		staticRules.addAll(SymbolicRules.getInstance().getRules());
		for( ITranslationRule rule: translationRules ) {
			staticRules.addAll(rule.getDatalogRules());
		}
	}
	
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
			state.getRuleFacts()
		);
		return facts;
	}
	
	protected Map<IPredicate, IRelation> makeFacts(SymbolicState state) {
		SQLRules sqlRules = SQLRules.getInstance();
		ERRules erRules = ERRules.getInstance();
		SymbolicRules symRules = SymbolicRules.getInstance();
		
		@SuppressWarnings("unchecked")
		Map<IPredicate, IRelation> facts = mergeFacts(		
			sqlFacts.getFacts(),
			sqlRules.getFacts(),
			erFacts.getFacts(),
			erRules.getFacts(),
			symRules.getFacts()
//			state.getRuleFacts()
		);
		return facts;
		
	}
	
	protected IKnowledgeBase createSQLKnowledgeBase(SelectNode select, SQLState state) {
		long duration = -System.currentTimeMillis();
		sqlFacts.generateFacts(select, true);
		Map<IPredicate, IRelation> facts = makeFacts(state);
		
		List<IRule> rules = staticRules;
		
		_log.debug(Markers.TIMERS_FINE, "KB creation prep in {} ms.", duration + System.currentTimeMillis());
		
		try {
			duration = -System.currentTimeMillis();
			IKnowledgeBase kb = KnowledgeBaseFactory.createKnowledgeBase(facts, rules);
			_log.debug(Markers.TIMERS_FINE, "KB creation in {} ms.", duration + System.currentTimeMillis());
			return kb;
		} catch( EvaluationException e ) {
			throw new SQLTutorException(e);
		}
	}

	private Collection<ITranslationRule> makeDefaultRules() {
		List<ISymbolicTranslationRule> symbolicRules = SymbolicUtil.loadSymbolicRules();
		List<ITranslationRule> rules = new ArrayList<ITranslationRule>(symbolicRules.size() + 5);
		rules.addAll(Arrays.<ITranslationRule>asList(
			// analysis rules
//			new JoinLabelRule(),
			new DefaultTableLabelRule(),
			new DefaultAttributeLabelRule(),
			new DefaultColumnLabelRule(),
			new DescribingAttributeLabelRule()
		));
		rules.addAll(symbolicRules);
		return rules;
	}
	
//	private RootToken makeSymbolic(SQLState sqlState) {
//		this.buildMaps();
//		return new SymbolicCreator(sqlState, sqlMaps).makeSymbolic();
//	}
//	
	@Override
	public void clearResult() {
		super.clearResult();
		sqlFacts.reset();
		erFacts.reset();
		symFacts.reset();
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
