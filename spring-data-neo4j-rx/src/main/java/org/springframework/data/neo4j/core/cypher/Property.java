/*
 * Copyright (c) 2019 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.neo4j.core.cypher;

/**
 * A property that belongs to a property container (either Node or Relationship).
 *
 * @author Michael J. Simons
 */
public class Property implements Expression {

	static Property create(Node parentContainer, String name) {

		return new Property(parentContainer, name);
	}

	/**
	 * The property container this property belongs to.
	 */
	private final Node parentContainer;

	/**
	 * The name of this property.
	 */
	private final String name;

	Property(Node parentContainer, String name) {

		this.parentContainer = parentContainer;
		this.name = name;
	}

	public String getParentAlias() {
		return parentContainer.getSymbolicName().get().getName();
	}

	public String getName() {
		return name;
	}

	public Condition matches(String s) {

		return Conditions.matches(this, new StringLiteral(s));
	}
}
