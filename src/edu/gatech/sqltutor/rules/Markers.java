package edu.gatech.sqltutor.rules;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/** Markers for log messages. */
public class Markers {
	public static final Marker DATALOG = MarkerFactory.getMarker("DATALOG");
	public static final Marker DATALOG_FACTS = MarkerFactory.getMarker("DATALOG_FACT");
	static { DATALOG.add(DATALOG_FACTS); }
	public static final Marker DATALOG_RULES = MarkerFactory.getMarker("DATALOG_RULES");
	static { DATALOG.add(DATALOG_RULES); }
}
