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
package org.neo4j.springframework.data.integration.reactive;

import reactor.core.publisher.Flux;

import org.neo4j.springframework.data.integration.shared.NamesOnly;
import org.neo4j.springframework.data.integration.shared.NamesOnlyDto;
import org.neo4j.springframework.data.integration.shared.Person;
import org.neo4j.springframework.data.integration.shared.PersonSummary;
import org.neo4j.springframework.data.repository.ReactiveNeo4jRepository;

/**
 * @author Gerrit Meier
 */
public interface ReactiveProjectionPersonRepository extends ReactiveNeo4jRepository<Person, Long> {

	Flux<NamesOnly> findByLastName(String lastName);

	Flux<PersonSummary> findByFirstName(String firstName);

	Flux<NamesOnlyDto> findByFirstNameAndLastName(String firstName, String lastName);

	<T> Flux<T> findByLastNameAndFirstName(String lastName, String firstName, Class<T> projectionClass);
}
