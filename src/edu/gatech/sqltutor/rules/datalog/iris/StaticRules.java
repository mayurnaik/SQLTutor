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

import com.google.common.base.Charsets;

import edu.gatech.sqltutor.SQLTutorException;
import edu.gatech.sqltutor.Utils;

/**
 * Static datalog rules parsed from some resource.
 */
public class StaticRules {
	private Parser parser;

	protected StaticRules() { }
	
	public StaticRules(String resource) {
		parser = parseResource(resource);
	}
	
	public StaticRules(InputStream input) {
		parser = parse(input);
	}
	
	public StaticRules(Reader reader) {
		parser = parse(reader);
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
}
