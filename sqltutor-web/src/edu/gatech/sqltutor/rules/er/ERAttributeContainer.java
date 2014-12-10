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

import java.util.Set;

/**
 * Nodes containing attributes, such as entities and relationships.
 */
public interface ERAttributeContainer extends ERNamedNode {
	public void addAttribute(ERAttribute attr);
	public ERAttribute addAttribute(String name);
	public ERCompositeAttribute addCompositeAttribute(String name);
	
	public ERAttribute addChildAttribute(String parent, String name);
	public ERAttribute getAttribute(String name);
	
	public ERAttribute removeAttribute(String name);
	public void removeAttribute(ERAttribute attr);
	
	public Set<ERAttribute> getSimpleAttributes();
	public Set<ERAttribute> getTopLevelAttributes();
	public Set<ERAttribute> getAttributes();
}
