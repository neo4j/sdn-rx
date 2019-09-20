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

import javax.swing.*;

import org.intellij.lang.annotations.Language;
import org.neo4j.springframework.data.core.schema.GeneratedValue;
import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;
import org.neo4j.springframework.data.core.schema.Relationship;

/**
 * @author Gerrit Meier
 */
@Node
public class Person {

	@Id @GeneratedValue private Long id;
	private String firstName;
	private String lastName;

	@Relationship("LIVES_AT")
	private Address address;

	@Node
	static class Address {
		@Id @GeneratedValue private Long id;
		private String zipCode;
		private String city;
		private String street;
	}

	// The getters are needed for Spring Expression Language in `NamesOnly`
	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
}
