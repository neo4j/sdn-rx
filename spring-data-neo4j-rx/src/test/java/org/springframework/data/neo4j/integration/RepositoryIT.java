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
package org.springframework.data.neo4j.integration;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.core.NodeManagerFactory;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RepositoryIT.Config.class)
@Testcontainers
class RepositoryIT {
	private static final String TEST_PERSON1_NAME = "Test";
	private static final String TEST_PERSON2_NAME = "Test2";
	private static final String TEST_PERSON_SAMEVALUE = "SameValue";

	@Container
	private static Neo4jContainer neo4jContainer = new Neo4jContainer().withoutAuthentication();

	private final PersonRepository repository;
	private final Driver driver;
	private Long id1;
	private Long id2;
	private PersonWithAllConstructor person1;
	private PersonWithAllConstructor person2;

	@Autowired
	RepositoryIT(PersonRepository repository, Driver driver) {

		this.repository = repository;
		this.driver = driver;
	}

	@BeforeEach
	void setupData() {

		Transaction transaction = driver.session().beginTransaction();
		transaction.run("MATCH (n) detach delete n");
		id1 = transaction.run("CREATE (n:PersonWithAllConstructor) SET n.name = '" + TEST_PERSON1_NAME + "', n.sameValue = '" + TEST_PERSON_SAMEVALUE + "' return id(n)")
				.next().get(0).asLong();
		id2 = transaction.run("CREATE (n:PersonWithAllConstructor) SET n.name = '" + TEST_PERSON2_NAME + "', n.sameValue = '" + TEST_PERSON_SAMEVALUE + "' return id(n)")
				.next().get(0).asLong();
		transaction.run("CREATE (n:PersonWithNoConstructor) SET n.name = '" + TEST_PERSON1_NAME + "'");
		transaction.run("CREATE (n:PersonWithWither) SET n.name = '" + TEST_PERSON1_NAME + "'");
		transaction.success();
		transaction.close();

		// note that there is no id setting in the mapping right now
		person1 = new PersonWithAllConstructor(null, TEST_PERSON1_NAME, TEST_PERSON_SAMEVALUE);
		person2 = new PersonWithAllConstructor(null, TEST_PERSON2_NAME, TEST_PERSON_SAMEVALUE);
	}

	@Test
	void findAll() {
		Iterable<PersonWithAllConstructor> people = repository.findAll();
		assertThat(people).hasSize(2);
		assertThat(people).extracting("name").containsExactlyInAnyOrder(TEST_PERSON1_NAME, TEST_PERSON2_NAME);
	}

	@Test
	void findById() {
		Optional<PersonWithAllConstructor> person = repository.findById(id1);
		assertThat(person).isPresent();
		assertThat(person.get().getName()).isEqualTo(TEST_PERSON1_NAME);
	}

	@Test
	void existsById() {
		boolean exists = repository.existsById(id1);
		assertThat(exists).isTrue();
	}

	@Test
	void findAllById() {
		Iterable<PersonWithAllConstructor> persons = repository.findAllById(Arrays.asList(id1, id2));
		assertThat(persons).hasSize(2);
	}

	@Test
	void count() {
		assertThat(repository.count()).isEqualTo(2);
	}

	@Test
	void findAllWithSortByOrderDefault() {
		Iterable<PersonWithAllConstructor> persons = repository.findAll(Sort.by("name"));

		assertThat(persons).containsExactly(person1, person2);
	}

	@Test
	void findAllWithSortByOrderAsc() {
		Iterable<PersonWithAllConstructor> persons = repository.findAll(Sort.by(Sort.Order.asc("name")));

		assertThat(persons).containsExactly(person1, person2);
	}

	@Test
	void findAllWithSortByOrderDesc() {
		Iterable<PersonWithAllConstructor> persons = repository.findAll(Sort.by(Sort.Order.desc("name")));

		assertThat(persons).containsExactly(person2, person1);
	}

	@Test
	void findAllWithPageable() {
		Sort sort = Sort.by("name");
		int page = 0;
		int limit = 1;
		Page<PersonWithAllConstructor> persons = repository.findAll(PageRequest.of(page, limit, sort));

		assertThat(persons).containsExactly(person1);

		page = 1;
		persons = repository.findAll(PageRequest.of(page, limit, sort));
		assertThat(persons).containsExactly(person2);
	}

	@Test
	void findOneByExample() {
		Example<PersonWithAllConstructor> example = Example.of(person1, ExampleMatcher.matchingAll().withIgnoreNullValues());
		Optional<PersonWithAllConstructor> person = repository.findOne(example);

		assertThat(person).isPresent();
		assertThat(person.get()).isEqualTo(person1);
	}

	@Test
	void findAllByExample() {
		Example<PersonWithAllConstructor> example = Example.of(person1, ExampleMatcher.matchingAll().withIgnoreNullValues());
		Iterable<PersonWithAllConstructor> persons = repository.findAll(example);

		assertThat(persons).containsExactly(person1);
	}

	@Test
	void findAllByExampleWithSort() {
		Example<PersonWithAllConstructor> example = Example.of(PersonWithAllConstructor.of(TEST_PERSON_SAMEVALUE));
		Iterable<PersonWithAllConstructor> persons = repository.findAll(example, Sort.by(Sort.Direction.DESC, "name"));

		assertThat(persons).containsExactly(person2, person1);
	}

	@Test
	void findAllByExampleWithPagination() {
		Example<PersonWithAllConstructor> example = Example.of(PersonWithAllConstructor.of(TEST_PERSON_SAMEVALUE));
		Iterable<PersonWithAllConstructor> persons = repository.findAll(example, PageRequest.of(1, 1, Sort.by("name")));

		assertThat(persons).containsExactly(person2);
	}

	@Test
	void existsByExample() {
		Example<PersonWithAllConstructor> example = Example.of(PersonWithAllConstructor.of(TEST_PERSON_SAMEVALUE));
		boolean exists = repository.exists(example);

		assertThat(exists).isTrue();
	}

	@Test
	void countByExample() {
		Example<PersonWithAllConstructor> example = Example.of(person1);
		long count = repository.count(example);

		assertThat(count).isEqualTo(1);
	}

	@Test
	void loadAllPersonsWithAllConstructor() {
		List<PersonWithAllConstructor> persons = repository.getAllPersonsViaQuery();

		assertThat(persons).anyMatch(person -> person.getName().equals(TEST_PERSON1_NAME));
	}

	@Test
	void loadOnePersonWithAllConstructor() {
		PersonWithAllConstructor person = repository.getOnePersonViaQuery();
		assertThat(person.getName()).isEqualTo(TEST_PERSON1_NAME);
	}

	@Test
	void loadOptionalPersonWithAllConstructor() {
		Optional<PersonWithAllConstructor> person = repository.getOptionalPersonsViaQuery();
		assertThat(person).isPresent();
		assertThat(person.get().getName()).isEqualTo(TEST_PERSON1_NAME);
	}

	@Test
	void loadAllPersonsWithNoConstructor() {
		List<PersonWithNoConstructor> persons = repository.getAllPersonsWithNoConstructorViaQuery();

		assertThat(persons).anyMatch(person -> person.getName().equals(TEST_PERSON1_NAME));
	}

	@Test
	void loadOnePersonWithNoConstructor() {
		PersonWithNoConstructor person = repository.getOnePersonWithNoConstructorViaQuery();
		assertThat(person.getName()).isEqualTo(TEST_PERSON1_NAME);
	}

	@Test
	void loadOptionalPersonWithNoConstructor() {
		Optional<PersonWithNoConstructor> person = repository.getOptionalPersonsWithNoConstructorViaQuery();
		assertThat(person).isPresent();
		assertThat(person.get().getName()).isEqualTo(TEST_PERSON1_NAME);
	}

	@Test
	void loadAllPersonsWithWither() {
		List<PersonWithWither> persons = repository.getAllPersonsWithWitherViaQuery();

		assertThat(persons).anyMatch(person -> person.getName().equals(TEST_PERSON1_NAME));
	}

	@Test
	void loadOnePersonWithWither() {
		PersonWithWither person = repository.getOnePersonWithWitherViaQuery();
		assertThat(person.getName()).isEqualTo(TEST_PERSON1_NAME);
	}

	@Test
	void loadOptionalPersonWithWither() {
		Optional<PersonWithWither> person = repository.getOptionalPersonsWithWitherViaQuery();
		assertThat(person).isPresent();
		assertThat(person.get().getName()).isEqualTo(TEST_PERSON1_NAME);
	}

	@Test
	void callCustomCypher() {
		Long fixedLong = repository.customQuery();
		assertThat(fixedLong).isEqualTo(1L);
	}

	@Test
	void findBySimpleProperty() {
		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> repository.findByName(TEST_PERSON1_NAME));
	}

	@Configuration
	@EnableNeo4jRepositories
	@EnableTransactionManagement
	static class Config {

		@Bean
		public Driver driver() {

			String boltUrl = neo4jContainer.getBoltUrl();
			return GraphDatabase.driver(boltUrl, AuthTokens.basic("neo4j", "secret"));
		}

		@Bean
		public NodeManagerFactory nodeManagerFactory(Driver driver) {

			return new NodeManagerFactory(driver, PersonWithAllConstructor.class, PersonWithNoConstructor.class,
					PersonWithWither.class);
		}

		@Bean
		public PlatformTransactionManager transactionManager(Driver driver) {

			return new Neo4jTransactionManager(driver);
		}
	}
}
