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

import com.google.common.base.Objects;

public abstract class AbstractERNamedNode implements ERNamedNode {
	private final String name;
	public AbstractERNamedNode(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getFullName() {
		return getName();
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ (31 * getNodeType());
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == this ) return true;
		if( obj == null || !(obj instanceof AbstractERNamedNode) )
			return false;
		AbstractERNamedNode that = (AbstractERNamedNode)obj;
		return this.getNodeType() == that.getNodeType() && 
			Objects.equal(this.name, that.name);
	}
}
