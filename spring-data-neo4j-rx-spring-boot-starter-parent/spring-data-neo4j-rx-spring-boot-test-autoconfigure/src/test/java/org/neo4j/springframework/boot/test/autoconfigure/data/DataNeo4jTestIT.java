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
package org.neo4j.springframework.boot.test.autoconfigure.data;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for the SDN/RX Neo4j test slice.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@ContextConfiguration(initializers = DataNeo4jTestIT.Initializer.class)
@DataNeo4jTest
@Testcontainers
class DataNeo4jTestIT {

	@Container
	static final Neo4jContainer<?> neo4j = new Neo4jContainer<>().withoutAuthentication();

	@Autowired
	private Driver driver;

	@Autowired
	private ExampleRepository exampleRepository;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void testRepository() {
		ExampleEntity entity = new ExampleEntity("Look, new @DataNeo4jTest!");
		assertThat(entity.getId()).isNull();
		ExampleEntity persistedEntity = this.exampleRepository.save(entity);
		assertThat(persistedEntity.getId()).isNotNull();
		/*
		try(Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(AccessMode.READ).build())) {
			// assertThat(this.session.countEntitiesOfType(ExampleGraph.class)).isEqualTo(1);
		}

		 */

	}

	@Test
	void didNotInjectExampleService() {
		assertThatExceptionOfType(NoSuchBeanDefinitionException.class)
			.isThrownBy(() -> this.applicationContext.getBean(ExampleService.class));
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of("org.neo4j.driver.uri=" + neo4j.getBoltUrl())
				.applyTo(configurableApplicationContext.getEnvironment());
		}

	}
}
