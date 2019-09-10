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
package org.springframework.boot.autoconfigure.data.neo4j_rx;

import java.util.Set;

import org.neo4j.driver.Driver;
import org.neo4j.springframework.data.core.convert.Neo4jConversions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.neo4j.springframework.data.core.mapping.Neo4jMappingContext;
import org.neo4j.springframework.data.core.schema.Node;

/**
 * Automatic configuration of base infrastructure that imports configuration for both imperative and reactive Neo4j
 * repositories. Depends on the configured Neo4j driver.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(Driver.class)
@Import({ Neo4jImperativeDataConfiguration.class, Neo4jReactiveDataConfiguration.class })
public final class Neo4jDataAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public Neo4jConversions neo4jConversions() {
		return new Neo4jConversions();
	}

	@Bean
	@ConditionalOnMissingBean
	public Neo4jMappingContext neo4jMappingContext(ApplicationContext applicationContext, Neo4jConversions neo4jConversions)
		throws ClassNotFoundException {

		Set<Class<?>> initialEntityClasses = new EntityScanner(applicationContext).scan(Node.class);
		Neo4jMappingContext context = new Neo4jMappingContext(neo4jConversions);
		context.setInitialEntitySet(initialEntityClasses);

		return context;
	}
}
