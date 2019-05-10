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
package org.springframework.data.neo4j.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.neo4j.driver.Transaction;
import org.springframework.data.neo4j.core.cypher.Condition;
import org.springframework.data.neo4j.core.cypher.Node;
import org.springframework.data.neo4j.core.cypher.StatementBuilder;
import org.springframework.data.neo4j.core.schema.NodeDescription;
import org.springframework.lang.Nullable;

/**
 * Entry point for creating queries that return managed Nodes. The node manager is not supposed to be kept around
 * for longer than necessary. Try to keep your transactions short to avoid memory pressure due to keeping track of
 * managed nodes.
 *
 * @author Michael J. Simons
 */
@API(status = API.Status.STABLE, since = "1.0")
public interface NodeManager {

	/**
	 * Clears all managed entities and flushes any open state to the underlying storage.
	 * TODO reflect if we really, really want to have this.
	 * If an user flushes the node manager, we will end up in a state where we do not have any information about
	 * the nodes / entities currently processed in the ongoing transaction.
	 */
	default void flush() {
	}

	@Nullable
	Transaction getTransaction();

	Object executeQuery(String query);

	default <T> Collection<T> executeTypedQueryForObjects(Class<T> resultType, String query) {
		return executeTypedQueryForObjects(resultType, query, Collections.emptyMap());
	}

	<T> Collection<T> executeTypedQueryForObjects(Class<T> resultType, String query, Map<String, Object> parameters);

	default <T> Optional<T> executeTypedQueryForObject(Class<T> resultType, String query) {
		return executeTypedQueryForObject(resultType, query, Collections.emptyMap());
	}

	<T> Optional<T> executeTypedQueryForObject(Class<T> resultType, String query, Map<String, Object> parameters);

	/**
	 * Saves an entity. When the entity is not yet managed in this instance of the NodeManager, and will be registered as
	 * a managed instance. In either way, the state of the entity will be written to the underlying store afterwards.
	 * It is recommended to use the returned, managed instance of the object. This is especially true when dealing with
	 * immutable entities, where SDN RX has to return new instances to fill in generated keys and the like.
	 *
	 * @param entityWithUnknownState An entity that is either managed or unmanaged
	 * @return A managed object
	 */
	<T> T save(T entityWithUnknownState);

	/**
	 * Delete an object from the persistence context and the underlying store.
	 *
	 * @param managedEntity Object to be removed
	 */
	void delete(Object managedEntity);
}
