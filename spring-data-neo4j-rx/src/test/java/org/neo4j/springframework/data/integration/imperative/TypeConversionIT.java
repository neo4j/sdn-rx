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

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.springframework.data.config.AbstractNeo4jConfig;
import org.neo4j.springframework.data.integration.shared.ThingWithPropertiesRequiringConversion;
import org.neo4j.springframework.data.integration.shared.TypeConversionITBase;
import org.neo4j.springframework.data.repository.Neo4jRepository;
import org.neo4j.springframework.data.repository.config.EnableNeo4jRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Michael J. Simons
 */
class TypeConversionIT extends TypeConversionITBase {

	private final RequiredConversionRepository thingsWithPropertiesRequiringConversion;

	@Autowired TypeConversionIT(RequiredConversionRepository thingsWithPropertiesRequiringConversion, Driver driver) {
		super(driver);
		this.thingsWithPropertiesRequiringConversion = thingsWithPropertiesRequiringConversion;
	}

	@Test
	void writeConversionShouldWork() {

		ThingWithPropertiesRequiringConversion t =
			thingsWithPropertiesRequiringConversion
				.save(ThingWithPropertiesRequiringConversion.builder().aFloatScalar(23.42F).build());

		assertThat(t.getId()).isNotNull();

		try (Session session = super.driver.session()) {
			Value aFloatValue = session.run(
				"MATCH (n:ThingWithPropertiesRequiringConversion) WHERE id(n) = $id RETURN n.aFloatScalar as aFloatScalar",
				Values.parameters("id", t.getId())).single().get("aFloatScalar");
			assertThat(aFloatValue).isNotNull();
			assertThat(aFloatValue.asObject()).isInstanceOf(String.class).isEqualTo("23.42");
		}
	}

	@Test
	void readConversionShouldWork() {

		Long id;
		try (Session session = TypeConversionIT.this.driver.session()) {
			id = session
				.run("CREATE (n:ThingWithPropertiesRequiringConversion {aFloatScalar: '23.42'}) RETURN id(n) as ID")
				.single().get("ID").asLong();
		}

		Optional<ThingWithPropertiesRequiringConversion> optionalThing = thingsWithPropertiesRequiringConversion
			.findById(id);
		assertThat(optionalThing)
			.map(ThingWithPropertiesRequiringConversion::getAFloatScalar)
			.isPresent()
			.hasValue(23.42F);
	}

	public interface RequiredConversionRepository
		extends Neo4jRepository<ThingWithPropertiesRequiringConversion, Long> {
	}

	@Configuration
	@EnableNeo4jRepositories(considerNestedRepositories = true)
	@EnableTransactionManagement
	static class Config extends AbstractNeo4jConfig {

		@Bean
		public Driver driver() {
			return neo4jConnectionSupport.getDriver();
		}

		@Override
		protected Collection<String> getMappingBasePackages() {
			return Collections.singletonList(ThingWithPropertiesRequiringConversion.class.getPackage().getName());
		}

	}
}
