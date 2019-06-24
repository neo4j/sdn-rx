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

import static org.neo4j.driver.Values.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.driver.reactive.RxSession;
import org.neo4j.springframework.data.config.AbstractReactiveNeo4jConfig;
import org.neo4j.springframework.data.integration.shared.ThingWithAssignedId;
import org.neo4j.springframework.data.repository.config.EnableReactiveNeo4jRepositories;
import org.neo4j.springframework.data.repository.event.ReactiveBeforeBindCallback;
import org.neo4j.springframework.data.test.Neo4jExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;

/**
 * @author Michael J. Simons
 */
@ExtendWith(SpringExtension.class)
@ExtendWith(Neo4jExtension.class)
@ContextConfiguration(classes = ReactiveCallbacksIT.Config.class)
class ReactiveCallbacksIT {

	private static Neo4jExtension.Neo4jConnectionSupport neo4jConnectionSupport;

	private final ReactiveTransactionManager transactionManager;
	private final ReactiveThingRepository thingRepository;
	private final Driver driver;

	@Autowired
	ReactiveCallbacksIT(ReactiveTransactionManager transactionManager,
		ReactiveThingRepository thingRepository, Driver driver) {

		this.transactionManager = transactionManager;
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

		Mono<ThingWithAssignedId> operationUnderTest = Mono.just(thing).flatMap(thingRepository::save);

		TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);
		transactionalOperator
			.execute(t -> operationUnderTest)
			.map(ThingWithAssignedId::getName)
			.as(StepVerifier::create)
			.expectNext("A name (Edited)")
			.verifyComplete();

		Flux
			.usingWhen(
				Mono.fromSupplier(() -> driver.rxSession()),
				s -> s.run("MATCH (n:Thing) WHERE n.theId = $id RETURN n", parameters("id", "aaBB")).records(),
				RxSession::close
			)
			.map(r -> r.get("n").asNode().get("name").asString())
			.as(StepVerifier::create)
			.expectNext("A name (Edited)")
			.verifyComplete();
	}

	@Test
	void onBeforeBindShouldBeCalledForAllEntitiesUsingIterable() {

		ThingWithAssignedId thing1 = new ThingWithAssignedId("id1");
		thing1.setName("A name");
		ThingWithAssignedId thing2 = new ThingWithAssignedId("id2");
		thing2.setName("Another name");
		thingRepository.saveAll(Arrays.asList(thing1, thing2));

		Flux<ThingWithAssignedId> operationUnderTest = thingRepository.saveAll(Arrays.asList(thing1, thing2));

		TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);
		transactionalOperator
			.execute(t -> operationUnderTest)
			.map(ThingWithAssignedId::getName)
			.as(StepVerifier::create)
			.expectNext("A name (Edited)")
			.expectNext("Another name (Edited)")
			.verifyComplete();

		Flux
			.usingWhen(
				Mono.fromSupplier(() -> driver.rxSession()),
				s -> {
					Value parameters = parameters("ids", Arrays.asList("id1", "id2"));
					return s.run("MATCH (n:Thing) WHERE n.theId IN ($ids) RETURN n.name as name ORDER BY n.name ASC",
						parameters)
						.records();
				},
				RxSession::close
			)
			.map(r -> r.get("name").asString())
			.as(StepVerifier::create)
			.expectNext("A name (Edited)")
			.expectNext("Another name (Edited)")
			.verifyComplete();
	}

	@Test
	void onBeforeBindShouldBeCalledForAllEntitiesUsingPublisher() {

		ThingWithAssignedId thing1 = new ThingWithAssignedId("id1");
		thing1.setName("A name");
		ThingWithAssignedId thing2 = new ThingWithAssignedId("id2");
		thing2.setName("Another name");
		thingRepository.saveAll(Arrays.asList(thing1, thing2));

		Flux<ThingWithAssignedId> operationUnderTest = thingRepository.saveAll(Flux.just(thing1, thing2));

		TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);
		transactionalOperator
			.execute(t -> operationUnderTest)
			.map(ThingWithAssignedId::getName)
			.as(StepVerifier::create)
			.expectNext("A name (Edited)")
			.expectNext("Another name (Edited)")
			.verifyComplete();

		Flux
			.usingWhen(
				Mono.fromSupplier(() -> driver.rxSession()),
				s -> {
					Value parameters = parameters("ids", Arrays.asList("id1", "id2"));
					return s.run("MATCH (n:Thing) WHERE n.theId IN ($ids) RETURN n.name as name ORDER BY n.name ASC",
						parameters)
						.records();
				},
				RxSession::close
			)
			.map(r -> r.get("name").asString())
			.as(StepVerifier::create)
			.expectNext("A name (Edited)")
			.expectNext("Another name (Edited)")
			.verifyComplete();
	}

	@Configuration
	@EnableReactiveNeo4jRepositories
	@EnableTransactionManagement
	static class Config extends AbstractReactiveNeo4jConfig {

		@Bean
		ReactiveBeforeBindCallback<ThingWithAssignedId> nameChanger() {
			return entity -> {
				ThingWithAssignedId updatedThing = new ThingWithAssignedId(entity.getTheId());
				updatedThing.setName(entity.getName() + " (Edited)");
				return Mono.just(updatedThing);
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
