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

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;
import org.neo4j.springframework.data.config.AbstractNeo4jConfig;
import org.neo4j.springframework.data.integration.shared.ThingWithAssignedId;
import org.neo4j.springframework.data.repository.config.EnableNeo4jRepositories;
import org.neo4j.springframework.data.repository.event.BeforeBindCallback;
import org.neo4j.springframework.data.test.Neo4jExtension;
import org.neo4j.springframework.data.test.Neo4jExtension.Neo4jConnectionSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Michael J. Simons
 */
@ExtendWith(SpringExtension.class)
@ExtendWith(Neo4jExtension.class)
@ContextConfiguration(classes = CallbacksIT.Config.class)
class CallbacksIT {

	private static Neo4jConnectionSupport neo4jConnectionSupport;

	private final ThingRepository thingRepository;
	private final Driver driver;

	@Autowired CallbacksIT(ThingRepository thingRepository, Driver driver) {

		this.thingRepository = thingRepository;
		this.driver = driver;
	}

	@BeforeEach
	void setupData() {

		try (Transaction transaction = driver.session().beginTransaction()) {
			transaction.run("MATCH (n) detach delete n");
			transaction.success();
		}
	}

	@Test
	void onBeforeBindShouldBeCalledForSingleEntity() {

		ThingWithAssignedId thing = new ThingWithAssignedId("aaBB");
		thing.setName("A name");
		thing = thingRepository.save(thing);

		assertThat(thing.getName()).isEqualTo("A name (Edited)");

		try (Session session = driver.session()) {
			Record record = session
				.run("MATCH (n:Thing) WHERE n.theId = $id RETURN n", Values.parameters("id", thing.getTheId()))
				.single();

			assertThat(record.containsKey("n")).isTrue();
			Node node = record.get("n").asNode();
			assertThat(node.get("theId").asString()).isEqualTo(thing.getTheId());
			assertThat(node.get("name").asString()).isEqualTo("A name (Edited)");
		}
	}

	@Test
	void onBeforeBindShouldBeCalledForAllEntities() {

		ThingWithAssignedId thing1 = new ThingWithAssignedId("id1");
		thing1.setName("A name");
		ThingWithAssignedId thing2 = new ThingWithAssignedId("id2");
		thing2.setName("Another name");
		Iterable<ThingWithAssignedId> savedThings = thingRepository.saveAll(Arrays.asList(thing1, thing2));

		assertThat(savedThings).extracting(ThingWithAssignedId::getName)
			.containsExactlyInAnyOrder("A name (Edited)", "Another name (Edited)");

		try (Session session = driver.session()) {
			Record record = session
				.run("MATCH (n:Thing) WHERE n.theId in $ids RETURN COLLECT(n.name) as names",
					Values.parameters("ids", Arrays.asList("id1", "id2")))
				.single();

			List<String> names = record.get("names").asList(Value::asString);
			assertThat(names)
				.hasSize(2)
				.containsExactlyInAnyOrder("A name (Edited)", "Another name (Edited)");
		}
	}

	@Configuration
	@EnableNeo4jRepositories
	@EnableTransactionManagement
	static class Config extends AbstractNeo4jConfig {

		@Bean
		BeforeBindCallback<ThingWithAssignedId> nameChanger() {
			return entity -> {
				ThingWithAssignedId updatedThing = new ThingWithAssignedId(entity.getTheId());
				updatedThing.setName(entity.getName() + " (Edited)");
				return updatedThing;
			};
		}

		@Bean
		public Driver driver() {
			return neo4jConnectionSupport.openConnection();
		}

		@Override
		protected Collection<String> getMappingBasePackages() {
			return Collections.singletonList(ThingWithAssignedId.class.getPackage().getName());
		}
	}
}
