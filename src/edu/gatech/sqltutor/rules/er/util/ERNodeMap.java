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
