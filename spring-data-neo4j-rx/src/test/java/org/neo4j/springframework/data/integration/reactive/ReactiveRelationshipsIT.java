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
package org.neo4j.springframework.data.integration.reactive;

import static org.assertj.core.api.Assertions.*;

import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.springframework.data.config.AbstractReactiveNeo4jConfig;
import org.neo4j.springframework.data.integration.shared.MultipleRelationshipsThing;
import org.neo4j.springframework.data.integration.shared.RelationshipsITBase;
import org.neo4j.springframework.data.repository.config.EnableReactiveNeo4jRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Test cases for various relationship scenarios (self references, multiple times to same instance).
 *
 * @author Michael J. Simons
 */
class ReactiveRelationshipsIT extends RelationshipsITBase {

	@Autowired ReactiveRelationshipsIT(Driver driver) {
		super(driver);
	}

	@Test
	void shouldSaveSingleRelationship(@Autowired MultipleRelationshipsThingRepository repository) {

		MultipleRelationshipsThing p = new MultipleRelationshipsThing("p");
		p.setTypeA(new MultipleRelationshipsThing("c"));

		repository.save(p)
			.map(MultipleRelationshipsThing::getId)
			.flatMap(repository::findById)
			.as(StepVerifier::create)
			.assertNext(loadedThing -> assertThat(loadedThing)
				.extracting(MultipleRelationshipsThing::getTypeA)
				.extracting(MultipleRelationshipsThing::getName)
				.isEqualTo("c"))
			.verifyComplete();

		try (Session session = driver.session()) {
			List<String> names = session.run("MATCH (n:MultipleRelationshipsThing) RETURN n.name AS name")
				.list(r -> r.get("name").asString());
			assertThat(names).hasSize(2).containsExactlyInAnyOrder("p", "c");
		}
	}

	@Test
	void shouldSaveSingleRelationshipInList(@Autowired MultipleRelationshipsThingRepository repository) {

		MultipleRelationshipsThing p = new MultipleRelationshipsThing("p");
		p.setTypeB(Collections.singletonList(new MultipleRelationshipsThing("c")));

		repository.save(p)
			.map(MultipleRelationshipsThing::getId)
			.flatMap(repository::findById)
			.as(StepVerifier::create)
			.assertNext(loadedThing -> assertThat(loadedThing.getTypeB())
				.extracting(MultipleRelationshipsThing::getName)
				.containsExactly("c"))
			.verifyComplete();

		try (Session session = driver.session()) {
			List<String> names = session.run("MATCH (n:MultipleRelationshipsThing) RETURN n.name AS name")
				.list(r -> r.get("name").asString());
			assertThat(names).hasSize(2).containsExactlyInAnyOrder("p", "c");
		}
	}

	/**
	 * This stores multiple, different instances.
	 *
	 * @param repository The repository to use.
	 */
	@Test
	void shouldSaveMultipleRelationshipsOfSameObjectType(@Autowired MultipleRelationshipsThingRepository repository) {

		MultipleRelationshipsThing p = new MultipleRelationshipsThing("p");
		p.setTypeA(new MultipleRelationshipsThing("c1"));
		p.setTypeB(Collections.singletonList(new MultipleRelationshipsThing("c2")));
		p.setTypeC(Collections.singletonList(new MultipleRelationshipsThing("c3")));

		repository.save(p)
			.map(MultipleRelationshipsThing::getId)
			.flatMap(repository::findById)
			.as(StepVerifier::create)
			.assertNext(loadedThing -> {
				MultipleRelationshipsThing typeA = loadedThing.getTypeA();
				List<MultipleRelationshipsThing> typeB = loadedThing.getTypeB();
				List<MultipleRelationshipsThing> typeC = loadedThing.getTypeC();

				assertThat(typeA).isNotNull();
				assertThat(typeA).extracting(MultipleRelationshipsThing::getName).isEqualTo("c1");
				assertThat(typeB).extracting(MultipleRelationshipsThing::getName).containsExactly("c2");
				assertThat(typeC).extracting(MultipleRelationshipsThing::getName).containsExactly("c3");
			})
			.verifyComplete();

		try (Session session = driver.session()) {

			List<String> names = session.run(
				"MATCH (n:MultipleRelationshipsThing {name: 'p'}) - [r:TYPE_A|TYPE_B|TYPE_C] -> (o) RETURN r, o")
				.list(record -> {
					String type = record.get("r").asRelationship().type();
					String name = record.get("o").get("name").asString();
					return type + "_" + name;
				});
			assertThat(names).containsExactlyInAnyOrder("TYPE_A_c1", "TYPE_B_c2", "TYPE_C_c3");
		}
	}

	/**
	 * This stores the same instance in different relationships
	 *
	 * @param repository The repository to use.
	 */
	@Test
	void shouldSaveMultipleRelationshipsOfSameInstance(@Autowired MultipleRelationshipsThingRepository repository) {

		MultipleRelationshipsThing p = new MultipleRelationshipsThing("p");
		MultipleRelationshipsThing c = new MultipleRelationshipsThing("c1");
		p.setTypeA(c);
		p.setTypeB(Collections.singletonList(c));
		p.setTypeC(Collections.singletonList(c));

		repository.save(p)
			.map(MultipleRelationshipsThing::getId)
			.flatMap(repository::findById)
			.as(StepVerifier::create)
			.assertNext(loadedThing -> {

				MultipleRelationshipsThing typeA = loadedThing.getTypeA();
				List<MultipleRelationshipsThing> typeB = loadedThing.getTypeB();
				List<MultipleRelationshipsThing> typeC = loadedThing.getTypeC();

				assertThat(typeA).isNotNull();
				assertThat(typeA).extracting(MultipleRelationshipsThing::getName).isEqualTo("c1");
				assertThat(typeB).extracting(MultipleRelationshipsThing::getName).containsExactly("c1");
				assertThat(typeC).extracting(MultipleRelationshipsThing::getName).containsExactly("c1");
			})
			.verifyComplete();

		try (Session session = driver.session()) {

			List<String> names = session.run(
				"MATCH (n:MultipleRelationshipsThing {name: 'p'}) - [r:TYPE_A|TYPE_B|TYPE_C] -> (o) RETURN r, o")
				.list(record -> {
					String type = record.get("r").asRelationship().type();
					String name = record.get("o").get("name").asString();
					return type + "_" + name;
				});
			assertThat(names).containsExactlyInAnyOrder("TYPE_A_c1", "TYPE_B_c1", "TYPE_C_c1");
		}
	}

	/**
	 * This stores the same instance in different relationships
	 *
	 * @param repository The repository to use.
	 */
	@Test
	void shouldSaveMultipleRelationshipsOfSameInstanceWithBackReference(
		@Autowired MultipleRelationshipsThingRepository repository) {

		MultipleRelationshipsThing p = new MultipleRelationshipsThing("p");
		MultipleRelationshipsThing c = new MultipleRelationshipsThing("c1");
		p.setTypeA(c);
		p.setTypeB(Collections.singletonList(c));
		p.setTypeC(Collections.singletonList(c));

		c.setTypeA(p);

		repository.save(p)
			.map(MultipleRelationshipsThing::getId)
			.flatMap(repository::findById)
			.as(StepVerifier::create)
			.assertNext(loadedThing -> {

				MultipleRelationshipsThing typeA = loadedThing.getTypeA();
				List<MultipleRelationshipsThing> typeB = loadedThing.getTypeB();
				List<MultipleRelationshipsThing> typeC = loadedThing.getTypeC();

				assertThat(typeA).isNotNull();
				assertThat(typeA).extracting(MultipleRelationshipsThing::getName).isEqualTo("c1");
				assertThat(typeB).extracting(MultipleRelationshipsThing::getName).containsExactly("c1");
				assertThat(typeC).extracting(MultipleRelationshipsThing::getName).containsExactly("c1");
			})
			.verifyComplete();

		try (Session session = driver.session()) {

			Function<Record, String> withMapper = record -> {
				String type = record.get("r").asRelationship().type();
				String name = record.get("o").get("name").asString();
				return type + "_" + name;
			};

			String query = "MATCH (n:MultipleRelationshipsThing {name: $name}) - [r:TYPE_A|TYPE_B|TYPE_C] -> (o) RETURN r, o";
			List<String> names = session.run(query, Collections.singletonMap("name", "p")).list(withMapper);
			assertThat(names).containsExactlyInAnyOrder("TYPE_A_c1", "TYPE_B_c1", "TYPE_C_c1");

			names = session.run(query, Collections.singletonMap("name", "c1")).list(withMapper);
			assertThat(names).containsExactlyInAnyOrder("TYPE_A_p");
		}
	}

	interface MultipleRelationshipsThingRepository extends ReactiveCrudRepository<MultipleRelationshipsThing, Long> {
	}

	@Configuration
	@EnableTransactionManagement
	@EnableReactiveNeo4jRepositories(considerNestedRepositories = true)
	static class Config extends AbstractReactiveNeo4jConfig {

		@Bean
		public Driver driver() {
			return neo4jConnectionSupport.getDriver();
		}
	}
}
