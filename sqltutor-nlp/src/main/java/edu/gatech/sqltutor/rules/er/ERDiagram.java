/*
 *   Copyright (c) 2014 Program Analysis Group, Georgia Tech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package edu.gatech.sqltutor.rules.er;

import java.io.Serializable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.gatech.sqltutor.rules.er.converters.ERDiagramConverter;
import edu.gatech.sqltutor.rules.er.util.ERNodeMap;

@XStreamAlias("erdiagram")
@XStreamConverter(ERDiagramConverter.class)
public class ERDiagram implements Serializable {
	private static final long serialVersionUID = 1L;
	
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
	
	private static final Pattern attributeNamePattern = Pattern.compile(
		"^([^\\.]+)(\\.[^\\.]+)*\\.([^\\.]+)$"
	);
	
	@XStreamOmitField
	private ERNodeMap<ERNamedNode> nodes;
	
	private Set<EREntity> entities;
	private Set<ERRelationship> relationships;

	public ERDiagram() {
		initialize();
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	protected void initialize() {
		nodes = new ERNodeMap<ERNamedNode>();
		entities = (Set)Sets.filter(nodes.getNodes(), isEntity);
		relationships = (Set)Sets.filter(nodes.getNodes(), isRelationship);
	}
	
	private Object readResolve() {
		this.initialize();
		return this;
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
	
	public EREntity getEntity(String name) {
		ERNamedNode node = nodes.getNode(name);
		if( node.getNodeType() == ERNode.TYPE_ENTITY )
			return (EREntity)node;
		return null;
	}

	public void addEntity(EREntity entity) {
		if( nodes.hasNode(entity.getName()) )
			throw new IllegalArgumentException("Already exists.");
		nodes.addNode(entity);
	}
	
	public void removeEntity(EREntity entity) {
		entities.remove(entity);
	}

	public Set<EREntity> getEntities() {
		return entities;
	}
	
	public ERRelationship getRelationship(String name) {
		ERNamedNode node = nodes.getNode(name);
		if( node != null && node.getNodeType() == ERNode.TYPE_RELATIONSHIP )
			return (ERRelationship)node;
		return null;
	}

	public ERRelationship newRelationship(String name) {
		if( name == null ) throw new NullPointerException("name is null");
		if( nodes.hasNode(name) )
			throw new IllegalArgumentException("Name is already in use: " + name);
		ERRelationship rel = new ERRelationship(name);
		nodes.addNode(rel);
		return rel;
	}
	
	public void addRelationship(ERRelationship relationship) {
		addNode(relationship);
	}
	
	public void removeRelationship(ERRelationship relationship) {
		relationships.remove(relationship);
	}
	
	public Set<ERRelationship> getRelationships() {
		return relationships;
	}
	
	public void addNode(ERNamedNode node) {
		nodes.addNode(node);
	}
	
	public ERNamedNode removeNode(String name) {
		return nodes.removeNode(name);
	}
	
	public ERAttribute getAttribute(String name) {
		Matcher matcher = attributeNamePattern.matcher(name);
		if( !matcher.matches() ) {
			throw new IllegalArgumentException(
				"Attribute name must be qualified with an entity or relationship: " + name);
		}
		
		String parentName = matcher.group(1);
		String attrName = matcher.group(3);
		
		ERAttributeContainer parent = (ERAttributeContainer)nodes.getNode(parentName);
		if( parent == null )
			return null; // exception?
		
		return parent.getAttribute(attrName);
	}
}
