package edu.gatech.sqltutor.rules.er.mapping;

/**
 * Represents a relationship that is merged 
 * with an entity.
 */
public class ERMergedJoin extends ERJoinMap {
	public ERMergedJoin() {
	}

	@Override
	public MapType getMapType() {
		return MapType.MERGED;
	}
}
