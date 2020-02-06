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

import static org.neo4j.springframework.data.test.Neo4jExtension.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.reactive.RxSession;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.springframework.data.config.AbstractReactiveNeo4jConfig;
import org.neo4j.springframework.data.core.ReactiveNeo4jClient;
import org.neo4j.springframework.data.core.schema.GeneratedValue;
import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;
import org.neo4j.springframework.data.repository.ReactiveNeo4jRepository;
import org.neo4j.springframework.data.repository.config.EnableReactiveNeo4jRepositories;
import org.neo4j.springframework.data.repository.support.ReactivePersistenceExceptionTranslationPostProcessor;
import org.neo4j.springframework.data.test.Neo4jIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Michael J. Simons
 */
@Neo4jIntegrationTest
@Tag(NEEDS_REACTIVE_SUPPORT)
class ReactiveExceptionTranslationIT {

	protected static Neo4jConnectionSupport neo4jConnectionSupport;

	// @formatter:off
	private final Predicate<Throwable> aTranslatedException =
		ex -> ex instanceof DataIntegrityViolationException && //
			ex.getMessage().matches("Node\\(\\d+\\) already exists with label `SimplePerson` and property `name` = '[\\w\\s]+'; Error code 'Neo.ClientError.Schema.ConstraintValidationFailed'");
	// @formatter:on

	@BeforeAll
	static void createConstraints(@Autowired Driver driver) {

		Flux.using(driver::rxSession,
			session -> session.run("CREATE CONSTRAINT ON (person:SimplePerson) ASSERT person.name IS UNIQUE").consume(),
			RxSession::close
		).then().as(StepVerifier::create).verifyComplete();
	}

	@AfterAll
	static void dropConstraints(@Autowired Driver driver) {

		Flux.using(driver::rxSession,
			session -> session.run("DROP CONSTRAINT ON (person:SimplePerson) ASSERT person.name IS UNIQUE").consume(),
			RxSession::close
		).then().as(StepVerifier::create).verifyComplete();
	}

	@BeforeEach
	void clearDatabase(@Autowired Driver driver) {

		Flux.using(driver::rxSession,
			session -> session.run("MATCH (n) DETACH DELETE n").consume(),
			RxSession::close
		).then().as(StepVerifier::create).verifyComplete();
	}

	@Test
	void exceptionsFromRepositoriesShouldBeTranslated(@Autowired SimplePersonRepository repository) {
		repository.save(new SimplePerson("Tom")).then().as(StepVerifier::create).verifyComplete();

		repository.save(new SimplePerson("Tom"))
			.as(StepVerifier::create)
			.verifyErrorMatches(aTranslatedException);
	}

	@Test
	void exceptionsOnRepositoryBeansShouldBeTranslated(@Autowired CustomDAO customDAO) {
		customDAO.createPerson().then().as(StepVerifier::create).verifyComplete();

		customDAO
			.createPerson()
			.as(StepVerifier::create)
			.verifyErrorMatches(aTranslatedException);
	}

	@Configuration
	@EnableReactiveNeo4jRepositories(
		considerNestedRepositories = true
	)
	@EnableTransactionManagement
	static class Config extends AbstractReactiveNeo4jConfig {

		@Bean
		public Driver driver() {
			return neo4jConnectionSupport.getDriver();
		}

		@Bean
		public CustomDAO customDAO(ReactiveNeo4jClient neo4jClient) {
			return new CustomDAO(neo4jClient);
		}

		@Override
		protected Collection<String> getMappingBasePackages() {
			return Collections.singletonList(SimplePerson.class.getPackage().getName());
		}

		@Bean
		public ReactivePersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
			return new ReactivePersistenceExceptionTranslationPostProcessor();
		}
	}

	@Node
	static class SimplePerson {

		@Id @GeneratedValue
		private Long id;

		private String name;

		SimplePerson(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	interface SimplePersonRepository extends ReactiveNeo4jRepository<SimplePerson, Long> {

	}

	@Repository
	static class CustomDAO {

		private final ReactiveNeo4jClient neo4jClient;

		CustomDAO(ReactiveNeo4jClient neo4jClient) {
			this.neo4jClient = neo4jClient;
		}

		public Mono<ResultSummary> createPerson() {
			return neo4jClient.query("CREATE (:SimplePerson {name: 'Aaron Paul'})").run();
		}
	}
}
