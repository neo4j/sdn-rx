/*
 * Copyright (c) 2019-2020 "Neo4j,"
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
package org.neo4j.doc.springframework.data.docs.repositories.projection;

import java.util.List;
import java.util.UUID;

import org.neo4j.springframework.data.core.schema.GeneratedValue;
import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;
import org.neo4j.springframework.data.core.schema.Relationship;
import org.neo4j.springframework.data.repository.Neo4jRepository;
import org.springframework.data.repository.Repository;

/**
 * @author Gerrit Meier
 */
// tag::projection.entity[]
@Node
public class Person {

	@Id @GeneratedValue private Long id;
	String firstName;
	String lastName;

	@Relationship("LIVES_AT")
	Address address;

	@Node
	static class Address {
		@Id @GeneratedValue private Long id;
		private String zipCode;
		private String city;
		private String street;
	}

}
// end::projection.entity[]

// tag::projection.repository[]
interface PersonRepository extends Neo4jRepository<Person, UUID> {

	// tag::projection.repository.concrete[]
	List<Person> findByLastName(String lastName);
	// end::projection.repository.concrete[]

	// tag::projection.repository.interface[]
	List<NamesOnly> findByFirstName(String firstName);
	// end::projection.repository.interface[]
}
// end::projection.repository[]

