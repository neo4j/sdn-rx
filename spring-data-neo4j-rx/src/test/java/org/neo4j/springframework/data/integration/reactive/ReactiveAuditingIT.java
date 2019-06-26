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

import static org.assertj.core.api.Assertions.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.driver.Driver;
import org.neo4j.springframework.data.config.AbstractReactiveNeo4jConfig;
import org.neo4j.springframework.data.config.EnableNeo4jAuditing;
import org.neo4j.springframework.data.integration.shared.AuditingITBase;
import org.neo4j.springframework.data.integration.shared.ImmutableAuditableThing;
import org.neo4j.springframework.data.repository.config.EnableReactiveNeo4jRepositories;
import org.neo4j.springframework.data.test.Neo4jExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
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
@ContextConfiguration(classes = ReactiveAuditingIT.Config.class)
class ReactiveAuditingIT extends AuditingITBase {

	private final ReactiveTransactionManager transactionManager;
	private final ReactiveTestRepository thingRepository;

	@Autowired
	ReactiveAuditingIT(ReactiveTransactionManager transactionManager, ReactiveTestRepository thingRepository,
		Driver driver) {
		super(driver);
		this.thingRepository = thingRepository;
		this.transactionManager = transactionManager;
	}

	@Test
	void auditingOfCreationShouldWork() {

		List<ImmutableAuditableThing> newThings = new ArrayList<>();
		TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);
		transactionalOperator
			.execute(t -> thingRepository.save(new ImmutableAuditableThing("A thing")))
			.as(StepVerifier::create)
			.recordWith(() -> newThings)
			.expectNextCount(1L)
			.verifyComplete();

		ImmutableAuditableThing savedThing = newThings.get(0);
		assertThat(savedThing.getCreatedAt()).isEqualTo(DEFAULT_CREATION_AND_MODIFICATION_DATE);
		assertThat(savedThing.getCreatedBy()).isEqualTo("A user");

		assertThat(savedThing.getModifiedAt()).isNull();
		assertThat(savedThing.getModifiedBy()).isNull();

		verifyDatabase(savedThing.getId(), savedThing);
	}

	@Test
	void auditingOfModificationShouldWork() {

		Mono<ImmutableAuditableThing> findAndUpdateAThing = thingRepository.findById(idOfExistingThing)
			.flatMap(thing -> thingRepository.save(thing.withName("A new name")));

		TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);
		transactionalOperator
			.execute(t -> findAndUpdateAThing)
			.as(StepVerifier::create)
			.consumeNextWith(savedThing -> {

				assertThat(savedThing.getCreatedAt()).isEqualTo(EXISTING_THING_CREATED_AT);
				assertThat(savedThing.getCreatedBy()).isEqualTo(EXISTING_THING_CREATED_BY);

				assertThat(savedThing.getModifiedAt()).isEqualTo(DEFAULT_CREATION_AND_MODIFICATION_DATE);
				assertThat(savedThing.getModifiedBy()).isEqualTo("A user");

				assertThat(savedThing.getName()).isEqualTo("A new name");
			})
			.verifyComplete();

		// Need to happen outside the reactive flow, as we use the blocking session to verify the database
		verifyDatabase(idOfExistingThing,
			new ImmutableAuditableThing(null, EXISTING_THING_CREATED_AT, EXISTING_THING_CREATED_BY,
				DEFAULT_CREATION_AND_MODIFICATION_DATE, "A user", "A new name"));
	}

	public interface ReactiveTestRepository extends ReactiveCrudRepository<ImmutableAuditableThing, Long> {
	}

	@Configuration
	@EnableTransactionManagement
	@EnableReactiveNeo4jRepositories(considerNestedRepositories = true)
	@EnableNeo4jAuditing(modifyOnCreate = false, auditorAwareRef = "auditorProvider", dateTimeProviderRef = "fixedDateTimeProvider")
	static class Config extends AbstractReactiveNeo4jConfig {

		@Bean
		public Driver driver() {
			return neo4jConnectionSupport.openConnection();
		}

		@Override
		protected Collection<String> getMappingBasePackages() {
			return Collections.singletonList(ImmutableAuditableThing.class.getPackage().getName());
		}

		@Bean
		public AuditorAware<String> auditorProvider() {
			return () -> Optional.of("A user");
		}

		@Bean
		public DateTimeProvider fixedDateTimeProvider() {
			return () -> Optional.of(DEFAULT_CREATION_AND_MODIFICATION_DATE);
		}
	}
}
