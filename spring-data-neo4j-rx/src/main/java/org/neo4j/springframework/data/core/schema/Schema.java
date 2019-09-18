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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.lang.Nullable;

/**
 * Contains the descriptions of all nodes, their properties and relationships known to SDN-RX.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@API(status = API.Status.STABLE, since = "1.0")
public interface Schema {

	/**
	 * Registers  the given set of classes to be available as Neo4j domain entities.
	 *
	 * @param initialEntitySet The set of classes to register with this schema
	 */
	void setInitialEntitySet(Set<? extends Class<?>> initialEntitySet);

	/**
	 * Triggers the scanning of the registered, initial entity set.
	 */
	void initialize();

	/**
	 * Retrieves a nodes description by its primary label.
	 *
	 * @param primaryLabel The primary label under which the node is described
	 * @return The description if any, null otherwise
	 */
	@Nullable NodeDescription<?> getNodeDescription(String primaryLabel);

	/**
	 * Retrieves a nodes description by its underlying class.
	 *
	 * @param underlyingClass The underlying class of the node description to be retrieved
	 * @return The description if any, null otherwise
	 */
	@Nullable NodeDescription<?> getNodeDescription(Class<?> underlyingClass);

	default NodeDescription<?> getRequiredNodeDescription(Class<?> underlyingClass) {
		NodeDescription<?> nodeDescription = getNodeDescription(underlyingClass);
		if (nodeDescription == null) {
			throw new UnknownEntityException(underlyingClass);
		}
		return nodeDescription;
	}

	/**
	 * This returns the outgoing relationships this node has to other nodes.
	 *
	 * @param primaryLabel The primary label of the node whos relationships should be retrieved
	 * @return The relationships defined by instances of this node.
	 */
	Collection<RelationshipDescription> getRelationshipsOf(String primaryLabel);

	<T> Function<T, Map<String, Object>> getRequiredBinderFunctionFor(Class<T> sourceClass);

	/**
	 * Creates or retrieves an instance of the given id generator class. During the lifetime of the schema,
	 * this method returns the same instance of reoccurring requests of the same type.
	 *
	 * @param idGeneratorType The type of the ID generator to return
	 * @return The id generator.
	 */
	<T extends IdGenerator<?>> T getOrCreateIdGeneratorOfType(Class<T> idGeneratorType);

	<T extends IdGenerator<?>> Optional<T> getIdGenerator(String reference);
}
