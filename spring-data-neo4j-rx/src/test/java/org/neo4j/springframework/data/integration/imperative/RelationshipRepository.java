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
package org.neo4j.springframework.data.integration.imperative;

import org.neo4j.springframework.data.integration.shared.PersonWithRelationship;
import org.neo4j.springframework.data.repository.Neo4jRepository;
import org.neo4j.springframework.data.repository.query.Query;

/**
 * @author Gerrit Meier
 */
public interface RelationshipRepository extends Neo4jRepository<PersonWithRelationship, Long> {

	@Query("MATCH (n:PersonWithRelationship{name:'Freddie'}) "
		+ "OPTIONAL MATCH (n)-[r1:Has]->(p:Pet) WITH n, collect(r1) as petRels, collect(p) as pets "
		+ "OPTIONAL MATCH (n)-[r2:Has]->(h:Hobby) "
		+ "return n, petRels, pets, collect(r2) as hobbyRels, collect(h) as hobbies")
	PersonWithRelationship getPersonWithRelationshipsViaQuery();

}
