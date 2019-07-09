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
package org.neo4j.springframework.data.repository.event;

import org.neo4j.springframework.data.core.mapping.Neo4jMappingContext;
import org.neo4j.springframework.data.core.mapping.Neo4jPersistentEntity;
import org.neo4j.springframework.data.core.mapping.Neo4jPersistentProperty;
import org.neo4j.springframework.data.core.schema.IdDescription;
import org.neo4j.springframework.data.core.schema.IdGenerator;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.util.Assert;

/**
 * Support for populating id properties with user specified id generators.
 *
 * @author Michael J. Simons
 */
final class IdPopulator {

	private final Neo4jMappingContext neo4jMappingContext;

	IdPopulator(Neo4jMappingContext neo4jMappingContext) {

		Assert.notNull(neo4jMappingContext, "A mapping context is required.");

		this.neo4jMappingContext = neo4jMappingContext;
	}

	Object populateIfNecessary(Object entity) {

		Assert.notNull(entity, "Entity may not be null!");

		Neo4jPersistentEntity<?> nodeDescription = neo4jMappingContext.getRequiredPersistentEntity(entity.getClass());
		IdDescription idDescription = nodeDescription.getIdDescription();

		// Filter in two steps to avoid unnecessary object creation.
		if (!idDescription.idIsGeneratedExternally()) {
			return entity;
		}

		PersistentPropertyAccessor propertyAccessor = nodeDescription.getPropertyAccessor(entity);
		Neo4jPersistentProperty idProperty = nodeDescription.getRequiredIdProperty();

		// Check existing ID
		if (propertyAccessor.getProperty(idProperty) != null) {
			return entity;
		}

		// Get or create the shared generator
		IdGenerator<?> idGenerator = neo4jMappingContext
			.getOrCreateIdGeneratorOfType(idDescription.getIdGeneratorClass().get());
		propertyAccessor.setProperty(idProperty, idGenerator.generateId(nodeDescription.getPrimaryLabel(), entity));
		return propertyAccessor.getBean();
	}
}
