package edu.gatech.sqltutor.rules.er.util;

import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import edu.gatech.sqltutor.rules.er.ERNamedNode;
import edu.gatech.sqltutor.rules.er.converters.NodeMapConverter;

@XStreamConverter(NodeMapConverter.class)
public class ERNodeMap<NodeType extends ERNamedNode> {
	@XStreamImplicit
	private BiMap<String, NodeType> nodes = HashBiMap.create();
	
	public ERNodeMap() {
	}
	
	protected String getNodeKey(NodeType node) {
		return node.getFullName();
	}

	public void addNode(NodeType node) {
		if( node == null ) throw new NullPointerException("node is null");
		String name = getNodeKey(node);
		if( nodes.containsKey(name) )
			throw new IllegalArgumentException("Name already exists: " + name);
		nodes.put(name, node);
	}
	
	public boolean hasNode(String name) {
		return nodes.containsKey(name);
	}
	
	public boolean hasNode(NodeType node) {
		return nodes.containsValue(node);
	}
	
	public NodeType getNode(String name) {
		return nodes.get(name);
	}
	
	public NodeType removeNode(String name) {
		return nodes.remove(name);
	}
	
	public void removeNode(NodeType node) {
		nodes.inverse().remove(node);
	}
	
	public Set<NodeType> getNodes() {
		return nodes.values();
	}
}
