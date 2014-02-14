package edu.gatech.sqltutor.rules.datalog.iris;

import static edu.gatech.sqltutor.QueryUtils.splitKeyParts;

import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.sqltutor.rules.er.ERAttribute;
import edu.gatech.sqltutor.rules.er.ERDiagram;
import edu.gatech.sqltutor.rules.er.EREdgeConstraint;
import edu.gatech.sqltutor.rules.er.EREntity;
import edu.gatech.sqltutor.rules.er.ERRelationship;
import edu.gatech.sqltutor.rules.er.ERAttribute.DescriptionType;
import edu.gatech.sqltutor.rules.er.ERRelationship.ERRelationshipEdge;
import edu.gatech.sqltutor.rules.er.mapping.ERForeignKeyJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap;
import edu.gatech.sqltutor.rules.er.mapping.ERLookupTableJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap.ERKeyPair;
import edu.gatech.sqltutor.rules.er.mapping.ERJoinMap.MapType;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;
import edu.gatech.sqltutor.util.Pair;

/** Dynamic ER diagram facts. */
public class ERFacts extends DynamicFacts {
	private static final Logger _log = LoggerFactory.getLogger(ERFacts.class);
	
	public ERFacts() { }
	
	public void generateFacts(ERDiagram erDiagram) {
		long duration = -System.currentTimeMillis();
		for( EREntity entity: erDiagram.getEntities() )
			addEntity(entity);
		for( ERRelationship rel: erDiagram.getRelationships() )
			addRelationship(rel);
		_log.info("er-diagram facts generated in {} ms.", duration += System.currentTimeMillis());
	}
	
	public void generateFacts(ERMapping erMapping) {
		long duration = -System.currentTimeMillis();
		addAttributeMappings(erMapping);
		addJoinMappings(erMapping);
		_log.info("er-mapping facts generated in {} ms.", duration += System.currentTimeMillis());
	}

	private void addAttributeMappings(ERMapping erMapping) {
		for( String attr: erMapping.getAttributes() ) {
			String col = erMapping.getColumnName(attr);
			if( col == null ) {
				_log.error("No column for attribute: " + attr);
			}
			Pair<String,String> attrParts = splitKeyParts(attr),
				colParts = splitKeyParts(col);
			
			addFact(ERPredicates.erAttributeMapsTo, 
				attrParts.getFirst(), attrParts.getSecond(),
				colParts.getFirst(),  colParts.getSecond());
		}
	}
	
	private void addJoinMappings(ERMapping erMapping) {
		Set<ERJoinMap> joins = erMapping.getJoins();
		for( ERJoinMap join: joins ) {
			MapType type = join.getMapType();
			addFact(ERPredicates.erRelationshipJoinType, 
				join.getRelationship(), type.toString().toLowerCase());
			switch( join.getMapType() ) {
				case FOREIGN_KEY:
					addFKJoin((ERForeignKeyJoin)join);
					break;
				case LOOKUP_TABLE:
					addLookupTableJoin((ERLookupTableJoin)join);
					break;
				case MERGED:
					_log.warn("FIXME: Merged type not handled yet."); // FIXME
			}
		}
	}
	
	private void addFKJoin(ERForeignKeyJoin join) {
		String rel = join.getRelationship();
		ERKeyPair keys = join.getKeyPair();
		addTableJoinKeys(rel, 0, keys);
	}
	
	private void addLookupTableJoin(ERLookupTableJoin join) {
		String rel = join.getRelationship();
		addTableJoinKeys(rel, 0, join.getLeftKeyPair());
		addTableJoinKeys(rel, 1, join.getRightKeyPair());
	}
	
	private void addTableJoinKeys(String rel, Integer pos, ERKeyPair keys) {
		assert pos != null && pos >= 0 : "pos should be a non-negative integer.";
		Pair<String,String> pk = splitKeyParts(keys.getPrimaryKey()),
				fk = splitKeyParts(keys.getForeignKey());
		
		addFact(ERPredicates.erJoinPK, rel, pos, pk.getFirst(), pk.getSecond());
		addFact(ERPredicates.erJoinFK, rel, pos, fk.getFirst(), fk.getSecond());
	}
	
	private void addEntity(EREntity entity) {
		String name = entity.getFullName();
		addFact(ERPredicates.erEntity, name);
		addFact(ERPredicates.erEntityType, name, 
			entity.getEntityType().toString().toLowerCase());
		
		for( ERAttribute attr: entity.getAttributes() ) {
			addAttribute(name, attr);
		}
	}
	
	private void addRelationship(ERRelationship rel) {
		String name = rel.getFullName();
		addFact(ERPredicates.erRelationship, name);
		
		for( ERAttribute attr: rel.getAttributes() ) {
			addAttribute(name, attr);
		}
		
		addRelationshipEdge(name, 0, rel.getLeftEdge());
		addRelationshipEdge(name, 1, rel.getRightEdge());
	}
	
	private void addAttribute(String parent, ERAttribute attr) {
		String attrName = attr.getName();
		addFact(ERPredicates.erAttribute, parent, attrName);
		if( attr.isKey() )
			addFact(ERPredicates.erAttributeIsKey, parent, attrName);
		DescriptionType dtype = attr.getDescribesEntity();
		if( dtype != null && dtype != DescriptionType.NONE )
			addFact(ERPredicates.erAttributeDescribes, parent, attrName, dtype.toString().toLowerCase(Locale.ENGLISH));
	}

	private void addRelationshipEdge(String rel, Integer pos, ERRelationshipEdge edge) {
		assert pos != null && pos >= 0 : "pos should be a non-negative int";
		addFact(ERPredicates.erRelationshipEdgeEntity, 
			rel, pos, edge.getEntity().getFullName());
		
		EREdgeConstraint constraint = edge.getConstraint();
		addFact(ERPredicates.erRelationshipEdgeLabel, 
			rel, pos, constraint.getLabel());
		addFact(ERPredicates.erRelationshipEdgeCardinality,
			rel, pos, constraint.getCardinality());
	}
}
