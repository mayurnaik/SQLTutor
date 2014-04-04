package edu.gatech.sqltutor.rules;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/** Markers for log messages. */
public class Markers {
	public static final Marker TIMERS = MarkerFactory.getMarker("TIMERS");
	public static final Marker TIMERS_FINE = MarkerFactory.getMarker("TIMERS_FINE");
	static { TIMERS.add(TIMERS_FINE); }
	
	/** Related to datalog evaluation. */
	public static final Marker DATALOG = MarkerFactory.getMarker("DATALOG");
	public static final Marker DATALOG_FACTS = MarkerFactory.getMarker("DATALOG_FACT");
	static { DATALOG.add(DATALOG_FACTS); }
	public static final Marker DATALOG_RULES = MarkerFactory.getMarker("DATALOG_RULES");
	static { DATALOG.add(DATALOG_RULES); }
	
	/** Related to metarule evaluation. */
	public static final Marker SYMBOLIC = MarkerFactory.getMarker("SYMBOLIC");
	
	/** For metarule-specific info. */
	public static final Marker METARULE = MarkerFactory.getMarker("METARULE");
	static {
		METARULE.add(DATALOG);
		METARULE.add(SYMBOLIC);
	}
}
