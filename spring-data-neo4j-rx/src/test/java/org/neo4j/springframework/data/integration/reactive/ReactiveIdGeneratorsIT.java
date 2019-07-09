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

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.springframework.data.config.AbstractReactiveNeo4jConfig;
import org.neo4j.springframework.data.integration.shared.IdGeneratorsITBase;
import org.neo4j.springframework.data.integration.shared.ThingWithGeneratedId;
import org.neo4j.springframework.data.repository.config.EnableReactiveNeo4jRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;

/**
 * @author Michael J. Simons
 */
@ContextConfiguration(classes = ReactiveIdGeneratorsIT.Config.class)
class ReactiveIdGeneratorsIT extends IdGeneratorsITBase {

	private final ReactiveTransactionManager transactionManager;
	private final TestRepository testRepository;

	@Autowired ReactiveIdGeneratorsIT(ReactiveTransactionManager transactionManager, TestRepository testRepository,
		Driver driver) {
		super(driver);
		this.transactionManager = transactionManager;
		this.testRepository = testRepository;
	}

	@Test
	void idGenerationWithNewEntityShouldWork() {

		List<ThingWithGeneratedId> savedThings = new ArrayList<>();
		TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);
		transactionalOperator
			.execute(t -> testRepository.save(new ThingWithGeneratedId("Foobar")))
			.as(StepVerifier::create)
			.recordWith(() -> savedThings)
			.consumeNextWith(savedThing -> {

				assertThat(savedThing.getName()).isEqualTo("Foobar");
				assertThat(savedThing.getTheId())
					.isNotBlank()
					.matches("thingWithGeneratedId-\\d+");
			})
			.verifyComplete();

		verifyDatabase(savedThings.get(0));
	}

	@Test
	void idGenerationWithNewEntitiesShouldWork() {

		List<ThingWithGeneratedId> things = IntStream.rangeClosed(1, 10)
			.mapToObj(i -> new ThingWithGeneratedId("name" + i))
			.collect(toList());

		Set<String> generatedIds = new HashSet<>();
		TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);
		transactionalOperator
			.execute(t -> testRepository.saveAll(things))
			.map(ThingWithGeneratedId::getTheId)
			.as(StepVerifier::create)
			.recordWith(() -> generatedIds)
			.expectNextCount(things.size())
			.expectRecordedMatches(recorded -> {
				assertThat(recorded)
					.hasSize(things.size())
					.allMatch(generatedId -> generatedId.matches("thingWithGeneratedId-\\d+"));
				return true;
			})
			.verifyComplete();
	}

	@Test
	void shouldNotOverwriteExistingId() {

		Mono<ThingWithGeneratedId> findAndUpdateAThing = testRepository.findById(ID_OF_EXISTING_THING)
			.flatMap(thing -> {
				thing.setName("changed");
				return testRepository.save(thing);
			});

		List<ThingWithGeneratedId> savedThings = new ArrayList<>();
		TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);
		transactionalOperator
			.execute(t -> findAndUpdateAThing)
			.as(StepVerifier::create)
			.recordWith(() -> savedThings)
			.consumeNextWith(savedThing -> {

				assertThat(savedThing.getName()).isEqualTo("changed");
				assertThat(savedThing.getTheId()).isEqualTo(ID_OF_EXISTING_THING);
			})
			.verifyComplete();

		verifyDatabase(savedThings.get(0));
	}

	public interface TestRepository extends ReactiveCrudRepository<ThingWithGeneratedId, String> {
	}

	@Configuration
	@EnableTransactionManagement
	@EnableReactiveNeo4jRepositories(considerNestedRepositories = true)
	static class Config extends AbstractReactiveNeo4jConfig {

		@Bean
		public Driver driver() {
			return neo4jConnectionSupport.openConnection();
		}

		@Override
		protected Collection<String> getMappingBasePackages() {
			return Collections.singletonList(ThingWithGeneratedId.class.getPackage().getName());
		}
	}
}
