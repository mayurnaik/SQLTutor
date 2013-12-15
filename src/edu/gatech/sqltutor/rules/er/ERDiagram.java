package edu.gatech.sqltutor.rules.er;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.Multigraph;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.gatech.sqltutor.rules.er.util.ERNodeMap;

@XStreamAlias("erdiagram")
public class ERDiagram {
	private static class IsNodeType implements Predicate<ERNode> {
		private final int nodeType;
		public IsNodeType(int nodeType) { this.nodeType = nodeType; }
		@Override
		public boolean apply(ERNode node) {
			return node != null && node.getNodeType() == nodeType;
		}
	}
	private static final IsNodeType 
		isEntity       = new IsNodeType(ERNode.TYPE_ENTITY),
		isRelationship = new IsNodeType(ERNode.TYPE_RELATIONSHIP),
		isAttribute    = new IsNodeType(ERNode.TYPE_ATTRIBUTE);
	
	
	// FIXME track here?
	@XStreamOmitField
	private Multigraph<ERNode, Object> graph = 
		new Multigraph<ERNode, Object>(Object.class);
	
	@XStreamOmitField
	private ERNodeMap<ERNamedNode> nodes = new ERNodeMap<ERNamedNode>();
	
//	private Set<EREntity> entities = new HashSet<EREntity>();
	private ERNodeMap<EREntity> entities = new ERNodeMap<EREntity>();
	private Set<ERRelationship> relationships = new HashSet<ERRelationship>();

	public ERDiagram() {
	}
	
	public EREntity newEntity(String name) {
		if( name == null ) throw new NullPointerException("name is null");
		if( nodes.hasNode(name) )
			throw new IllegalArgumentException("Node exists with name: " + name);
		EREntity entity = new EREntity(name);
		nodes.addNode(entity);
//		entities.add(entity);
		return entity;
	}

	public boolean addEntity(EREntity entity) {
		if( !entities.hasNode(entity) ) {
			entities.addNode(entity);
			return true;
		}
		return false;
//		return entities.add(entity);
	}
	
//	public boolean removeEntity(EREntity entity) {
//		return entities.remove(entity);
//	}

	@SuppressWarnings({"unchecked","rawtypes"})
	public Set<EREntity> getEntities() {
		return (Set)Sets.filter(nodes.getNodes(), isEntity);
//		return entities;
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
