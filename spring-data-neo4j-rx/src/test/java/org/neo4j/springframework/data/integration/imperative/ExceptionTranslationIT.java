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
import static org.neo4j.springframework.data.test.Neo4jExtension.*;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.springframework.data.config.AbstractNeo4jConfig;
import org.neo4j.springframework.data.core.Neo4jClient;
import org.neo4j.springframework.data.core.schema.GeneratedValue;
import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;
import org.neo4j.springframework.data.repository.Neo4jRepository;
import org.neo4j.springframework.data.repository.config.EnableNeo4jRepositories;
import org.neo4j.springframework.data.test.Neo4jIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Michael J. Simons
 */
@Neo4jIntegrationTest
class ExceptionTranslationIT {

	protected static Neo4jConnectionSupport neo4jConnectionSupport;

	@BeforeAll
	static void createConstraints(@Autowired Driver driver) {

		try (Session session = driver.session()) {
			session.run("CREATE CONSTRAINT ON (person:SimplePerson) ASSERT person.name IS UNIQUE").consume();
		}
	}

	@AfterAll
	static void dropConstraints(@Autowired Driver driver) {

		try (Session session = driver.session()) {
			session.run("DROP CONSTRAINT ON (person:SimplePerson) ASSERT person.name IS UNIQUE").consume();
		}
	}

	@BeforeEach
	void clearDatabase(@Autowired Driver driver) {

		try (Session session = driver.session()) {
			session.run("MATCH (n) DETACH DELETE n").consume();
		}
	}

	@Test
	void exceptionsFromRepositoriesShouldBeTranslated(@Autowired SimplePersonRepository repository) {
		repository.save(new SimplePerson("Jerry"));

		assertThatExceptionOfType(DataIntegrityViolationException.class)
			.isThrownBy(() -> repository.save(new SimplePerson("Jerry")))
			.withMessageMatching(
				"Node\\(\\d+\\) already exists with label `SimplePerson` and property `name` = '[\\w\\s]+'; Error code 'Neo.ClientError.Schema.ConstraintValidationFailed'");
	}

	/*
	 * Only when an additional {@link PersistenceExceptionTranslationPostProcessor} has been provided.
	 */
	@Test
	void exceptionsOnRepositoryBeansShouldBeTranslated(@Autowired CustomDAO customDAO) {
		ResultSummary summary = customDAO.createPerson();
		assertThat(summary.counters().nodesCreated()).isEqualTo(1L);

		assertThatExceptionOfType(DataIntegrityViolationException.class)
			.isThrownBy(() -> customDAO.createPerson())
			.withMessageMatching(
				"Node\\(\\d+\\) already exists with label `SimplePerson` and property `name` = '[\\w\\s]+'; Error code 'Neo.ClientError.Schema.ConstraintValidationFailed'");
	}

	@Configuration
	@EnableNeo4jRepositories(
		considerNestedRepositories = true,
		includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ExceptionTranslationIT.SimplePersonRepository.class)
	)
	@EnableTransactionManagement
	static class Config extends AbstractNeo4jConfig {

		@Bean
		public Driver driver() {
			return neo4jConnectionSupport.getDriver();
		}

		@Bean
		public CustomDAO customDAO(Neo4jClient neo4jClient) {
			return new CustomDAO(neo4jClient);
		}

		@Override
		protected Collection<String> getMappingBasePackages() {
			return Collections.singletonList(SimplePerson.class.getPackage().getName());
		}

		@Bean
		public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
			return new PersistenceExceptionTranslationPostProcessor();
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

	interface SimplePersonRepository extends Neo4jRepository<SimplePerson, Long> {
	}

	@Repository
	static class CustomDAO {

		private final Neo4jClient neo4jClient;

		CustomDAO(Neo4jClient neo4jClient) {
			this.neo4jClient = neo4jClient;
		}

		public ResultSummary createPerson() {
			return neo4jClient.query("CREATE (:SimplePerson {name: 'Tom'})").run();
		}
	}
}
