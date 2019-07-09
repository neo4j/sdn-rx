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
package org.neo4j.springframework.data.core.schema;

import java.util.Optional;

import org.apiguardian.api.API;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Description how to generate Ids for entities.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@API(status = API.Status.INTERNAL, since = "1.0")
public final class IdDescription {

	/**
	 * The class representing a generator for new ids or null for assigned ids.
	 */
	private @Nullable final Class<? extends IdGenerator<?>> idGeneratorClass;

	/**
	 * A reference to an ID generator.
	 */
	private @Nullable final String idGeneratorRef;

	/**
	 * The property that stores the id if applicable.
	 */
	private @Nullable final String graphPropertyName;

	public static IdDescription forAssignedIds(String graphPropertyName) {

		Assert.notNull(graphPropertyName, "Graph property name is required.");
		return new IdDescription(null, null, graphPropertyName);
	}

	public static IdDescription forInternallyGeneratedIds() {
		return new IdDescription(GeneratedValue.InternalIdGenerator.class, null, null);
	}

	public static IdDescription forExternallyGeneratedIds(
		@Nullable Class<? extends IdGenerator<?>> idGeneratorClass,
		@Nullable String idGeneratorRef,
		String graphPropertyName) {

		Assert.notNull(graphPropertyName, "Graph property name is required.");
		try {
			Assert.hasText(idGeneratorRef, "Reference to an ID generator has precedence.");

			return new IdDescription(null, idGeneratorRef, graphPropertyName);
		} catch (IllegalArgumentException e) {
			Assert.notNull(idGeneratorClass, "Class of id generator is required.");
			Assert.isTrue(idGeneratorClass != GeneratedValue.InternalIdGenerator.class,
				"Cannot use InternalIdGenerator for externally generated ids.");

			return new IdDescription(idGeneratorClass, null, graphPropertyName);
		}
	}

	public IdDescription(
		@Nullable Class<? extends IdGenerator<?>> idGeneratorClass,
		@Nullable String idGeneratorRef,
		@Nullable String graphPropertyName
	) {
		this.idGeneratorClass = idGeneratorClass;
		this.idGeneratorRef = idGeneratorRef != null && idGeneratorRef.isEmpty() ? null : idGeneratorRef;
		this.graphPropertyName = graphPropertyName;
	}

	public Optional<Class<? extends IdGenerator<?>>> getIdGeneratorClass() {
		return Optional.ofNullable(idGeneratorClass);
	}

	public Optional<String> getIdGeneratorRef() {
		return Optional.ofNullable(idGeneratorRef);
	}

	/**
	 * @return True, if the ID is assigned to the entity before the entity hits the database, either manually or through a generator.
	 */
	public boolean isAssignedId() {
		return this.idGeneratorClass == null && this.idGeneratorRef == null;
	}

	/**
	 * @return True, if the database generated the ID.
	 */
	public boolean isInternallyGeneratedId() {
		return this.idGeneratorClass == GeneratedValue.InternalIdGenerator.class;
	}

	/**
	 * @return True, if the ID is externally generated.
	 */
	public boolean isExternallyGeneratedId() {
		return (this.idGeneratorClass != null && this.idGeneratorClass != GeneratedValue.InternalIdGenerator.class)
			|| this.idGeneratorRef != null;
	}

	/**
	 * An ID description has only a corresponding graph property name when it's bas on an external assigment.
	 * An internal id has no corresponding graph property and therefor this method
	 * will return an empty {@link Optional} in such cases.
	 *
	 * @return The name of an optional graph property.
	 */
	public Optional<String> getOptionalGraphPropertyName() {
		return Optional.ofNullable(graphPropertyName);
	}
}
