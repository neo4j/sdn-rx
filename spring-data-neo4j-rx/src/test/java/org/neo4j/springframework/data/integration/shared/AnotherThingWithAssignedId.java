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
package org.neo4j.springframework.data.integration.shared;

import java.util.Objects;

import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;

/**
 * @author Gerrit Meier
 */
@Node("Thing2")
public class AnotherThingWithAssignedId {

	@Id
	private final Long theId;

	private String name;

	public AnotherThingWithAssignedId(Long theId) {
		this.theId = theId;
	}

	public Long getTheId() {
		return theId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AnotherThingWithAssignedId that = (AnotherThingWithAssignedId) o;
		return theId.equals(that.theId) &&
			Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(theId, name);
	}
}
