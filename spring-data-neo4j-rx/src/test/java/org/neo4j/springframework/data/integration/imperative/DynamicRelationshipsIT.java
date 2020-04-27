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
package org.neo4j.springframework.data.integration.imperative;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Values;
import org.neo4j.springframework.data.config.AbstractNeo4jConfig;
import org.neo4j.springframework.data.integration.shared.DynamicRelationshipsITBase;
import org.neo4j.springframework.data.integration.shared.Person;
import org.neo4j.springframework.data.integration.shared.PersonWithRelatives;
import org.neo4j.springframework.data.integration.shared.Pet;
import org.neo4j.springframework.data.repository.config.EnableNeo4jRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author Michael J. Simons
 */
class DynamicRelationshipsIT extends DynamicRelationshipsITBase {

	@Autowired
	DynamicRelationshipsIT(Driver driver) {
		super(driver);
	}

	@Test
	void shouldReadDynamicRelationships(@Autowired PersonWithRelativesRepository repository) {

		PersonWithRelatives personWithRelatives = repository.findById(idOfExistingPerson).get();
		assertThat(personWithRelatives).isNotNull();
		assertThat(personWithRelatives.getName()).isEqualTo("A");

		Map<String, Person> relatives = personWithRelatives.getRelatives();
		assertThat(relatives).containsOnlyKeys("HAS_WIFE", "HAS_DAUGHTER");
		assertThat(relatives.get("HAS_WIFE").getFirstName()).isEqualTo("B");
		assertThat(relatives.get("HAS_DAUGHTER").getFirstName()).isEqualTo("C");
	}

	@Test // GH-216
	void shouldReadDynamicCollectionRelationships(@Autowired PersonWithRelativesRepository repository) {

		PersonWithRelatives personWithRelatives = repository.findById(idOfExistingPerson).get();
		assertThat(personWithRelatives).isNotNull();
		assertThat(personWithRelatives.getName()).isEqualTo("A");

		Map<String, List<Pet>> pets = personWithRelatives.getPets();
		assertThat(pets).containsOnlyKeys("CATS", "DOGS");
		assertThat(pets.get("CATS")).extracting(Pet::getName).containsExactlyInAnyOrder("Tom", "Garfield");
		assertThat(pets.get("DOGS")).extracting(Pet::getName).containsExactlyInAnyOrder("Benji", "Lassie");
	}

	@Test
	void shouldUpdateDynamicRelationships(@Autowired PersonWithRelativesRepository repository) {

		PersonWithRelatives personWithRelatives = repository.findById(idOfExistingPerson).get();
		assumeThat(personWithRelatives).isNotNull();
		assumeThat(personWithRelatives.getName()).isEqualTo("A");

		Map<String, Person> relatives = personWithRelatives.getRelatives();
		assumeThat(relatives).containsOnlyKeys("HAS_WIFE", "HAS_DAUGHTER");

		relatives.remove("HAS_WIFE");
		Person d = new Person();
		ReflectionTestUtils.setField(d, "firstName", "D");
		relatives.put("HAS_SON", d);
		ReflectionTestUtils.setField(relatives.get("HAS_DAUGHTER"), "firstName", "C2");

		personWithRelatives = repository.save(personWithRelatives);
		relatives = personWithRelatives.getRelatives();
		assertThat(relatives).containsOnlyKeys("HAS_DAUGHTER", "HAS_SON");
		assertThat(relatives.get("HAS_DAUGHTER").getFirstName()).isEqualTo("C2");
		assertThat(relatives.get("HAS_SON").getFirstName()).isEqualTo("D");
	}

	@Test // GH-216
	void shouldUpdateDynamicCollectionRelationships(@Autowired PersonWithRelativesRepository repository) {

		PersonWithRelatives personWithRelatives = repository.findById(idOfExistingPerson).get();
		assertThat(personWithRelatives).isNotNull();
		assertThat(personWithRelatives.getName()).isEqualTo("A");

		Map<String, List<Pet>> pets = personWithRelatives.getPets();
		assertThat(pets).containsOnlyKeys("CATS", "DOGS");

		pets.remove("DOGS");
		pets.get("CATS").add(new Pet("Delilah"));

		pets.put("FISH", Collections.singletonList(new Pet("Nemo")));

		personWithRelatives = repository.save(personWithRelatives);
		pets = personWithRelatives.getPets();
		assertThat(pets).containsOnlyKeys("CATS", "FISH");
		assertThat(pets.get("CATS")).extracting(Pet::getName).containsExactlyInAnyOrder("Tom", "Garfield", "Delilah");
		assertThat(pets.get("FISH")).extracting(Pet::getName).containsExactlyInAnyOrder("Nemo");
	}

	@Test
	void shouldWriteDynamicRelationships(@Autowired PersonWithRelativesRepository repository) {

		PersonWithRelatives personWithRelatives = new PersonWithRelatives("Test");
		Map<String, Person> relatives;
		relatives = personWithRelatives.getRelatives();
		Person d;
		d = new Person();
		ReflectionTestUtils.setField(d, "firstName", "R1");
		relatives.put("RELATIVE_1", d);
		d = new Person();
		ReflectionTestUtils.setField(d, "firstName", "R2");
		relatives.put("RELATIVE_2", d);

		personWithRelatives = repository.save(personWithRelatives);
		relatives = personWithRelatives.getRelatives();
		assertThat(relatives).containsOnlyKeys("RELATIVE_1", "RELATIVE_2");

		try (Transaction transaction = driver.session().beginTransaction()) {
			long numberOfRelations = transaction.run(""
				+ "MATCH (t:PersonWithRelatives) WHERE id(t) = $id "
				+ "RETURN size((t)-->(:Person))"
				+ " as numberOfRelations", Values.parameters("id", personWithRelatives.getId()))
				.single().get("numberOfRelations").asLong();
			assertThat(numberOfRelations).isEqualTo(2L);
		}
	}

	@Test // GH-216
	void shouldWriteDynamicCollectionRelationships(@Autowired PersonWithRelativesRepository repository) {

		PersonWithRelatives personWithRelatives = new PersonWithRelatives("Test");
		Map<String, List<Pet>> pets;
		pets = personWithRelatives.getPets();

		List<Pet> monsters = pets.computeIfAbsent("MONSTERS", s -> new ArrayList<>());
		monsters.add(new Pet("Godzilla"));
		monsters.add(new Pet("King Kong"));

		List<Pet> fish = pets.computeIfAbsent("FISH", s -> new ArrayList<>());
		fish.add(new Pet("Nemo"));

		personWithRelatives = repository.save(personWithRelatives);
		pets = personWithRelatives.getPets();
		assertThat(pets).containsOnlyKeys("MONSTERS", "FISH");

		try (Transaction transaction = driver.session().beginTransaction()) {
			long numberOfRelations = transaction.run(""
				+ "MATCH (t:PersonWithRelatives) WHERE id(t) = $id "
				+ "RETURN size((t)-->(:Pet))"
				+ " as numberOfRelations", Values.parameters("id", personWithRelatives.getId()))
				.single().get("numberOfRelations").asLong();
			assertThat(numberOfRelations).isEqualTo(3L);
		}
	}

	interface PersonWithRelativesRepository extends CrudRepository<PersonWithRelatives, Long> {
	}

	@Configuration
	@EnableTransactionManagement
	@EnableNeo4jRepositories(considerNestedRepositories = true)
	static class Config extends AbstractNeo4jConfig {

		@Bean
		public Driver driver() {
			return neo4jConnectionSupport.getDriver();
		}

		@Override
		protected Collection<String> getMappingBasePackages() {
			return Collections.singletonList(PersonWithRelatives.class.getPackage().getName());
		}
	}
}
