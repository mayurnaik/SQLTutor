package edu.gatech.sqltutor.rules.datalog.iris;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.Utils;
import edu.gatech.sqltutor.rules.Markers;

/**
 * Static datalog rules parsed from some resource.
 */
public class StaticRules {
	private static final Logger _log = LoggerFactory.getLogger(StaticRules.class);
	
	private Parser parser;

	protected StaticRules() { }
	
	/**
	 * Parse rules matching the class.  If the 
	 * class is foo.Bar, then the resource should be 
	 * <code>/foo/Bar.dlog</code>.
	 * 
	 * @param clazz
	 */
	public StaticRules(Class<?> clazz) {
		String resource = "/" + clazz.getName().replace('.', '/') + ".dlog";
		parser = parseResource(resource);
		logParse();
	}
	
	public StaticRules(String resource) {
		parser = parseResource(resource);
		logParse();
	}
	
	public StaticRules(InputStream input) {
		parser = parse(input);
		logParse();
	}
	
	public StaticRules(Reader reader) {
		parser = parse(reader);
		logParse();
	}
	
	protected Parser parseString(String program) {
		if( program == null )
			throw new NullPointerException("program is null");
		Reader r = new StringReader(program);
		return parse(r);
	}
	
	protected Parser parse(Reader reader) {
		if( reader == null ) throw new NullPointerException("reader is null");
		Parser parser = IrisUtil.newParser();
		try {
			parser.parse(reader);
		} catch( ParserException e ) {
			throw new SQLTutorException(e);
		} finally {
			Utils.tryClose(reader);
		}
		return parser;
	}
	
	protected Parser parse(InputStream instream) {
		if( instream == null )
			throw new NullPointerException("instream is null");
		Reader r = new InputStreamReader(instream, Charsets.UTF_8);
		return parse(r);
	}
	
	protected Parser parseResource(String resourcePath) {
		if( resourcePath == null )
			throw new NullPointerException("resourcePath is null");
		InputStream in = StaticRules.class.getResourceAsStream(resourcePath);
		if( in == null )
			throw new SQLTutorException("Rule datalog resource not found: " + resourcePath);
		_log.debug(Markers.DATALOG_RULES, "Parsing rules from resource: {}", resourcePath);
		return parse(in);
	}

	public Map<IPredicate, IRelation> getFacts() {
		return parser.getFacts();
	}

	public List<IRule> getRules() {
		return parser.getRules();
	}

	public Parser getParser() {
		return parser;
	}
	
	private void logParse() {
		List<IRule> rules = parser.getRules();
		Map<IPredicate, IRelation> facts = parser.getFacts();
		_log.debug(Markers.DATALOG_RULES, "Parsed {} rules and {} facts.", 
			rules.size(), facts.size());
		if( facts.size() > 0 && _log.isDebugEnabled(Markers.DATALOG_FACTS) ) {
			for( Map.Entry<IPredicate, IRelation> entry: facts.entrySet() ) {
				_log.debug(Markers.DATALOG_FACTS, "Parsed fact: {}{}", entry.getKey().getPredicateSymbol(), entry.getValue());
			}
		}
		if( rules.size() > 0 && _log.isTraceEnabled(Markers.DATALOG_RULES) ) {
			for( IRule rule: rules ) {
				_log.trace(Markers.DATALOG_RULES, "Parsed rule: {}", rule);
			}
		}
	}
}
