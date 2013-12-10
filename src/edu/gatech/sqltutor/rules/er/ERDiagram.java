package edu.gatech.sqltutor.rules.er;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.Multigraph;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("erdiagram")
public class ERDiagram {
	
	// FIXME track here?
	@XStreamOmitField
	private Multigraph<ERNode, Object> graph = 
		new Multigraph<ERNode, Object>(Object.class);
	
	private Set<EREntity> entities = new HashSet<EREntity>();
	private Set<ERRelationship> relationships = new HashSet<ERRelationship>();

	public ERDiagram() {
	}

	public boolean addEntity(EREntity entity) {
		return entities.add(entity);
	}
	
	public boolean removeEntity(EREntity entity) {
		return entities.remove(entity);
	}
	
	public Set<EREntity> getEntities() {
		return entities;
	}
	
	public boolean addRelationship(ERRelationship relationship) {
		return relationships.add(relationship);
	}
	
	public boolean removeRelationship(ERRelationship relationship) {
		return relationships.remove(relationship);
	}
	
	public Set<ERRelationship> getRelationships() {
		return relationships;
	}
}
