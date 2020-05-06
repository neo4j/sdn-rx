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
package org.neo4j.springframework.data.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import org.apiguardian.api.API;
import org.neo4j.opencypherdsl.Statement;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * Specifies reactive operations one can perform on a database, based on an <em>Domain Type</em>.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@API(status = API.Status.STABLE, since = "1.0")
public interface ReactiveNeo4jOperations {

	/**
	 * Counts the number of entities of a given type.
	 *
	 * @param domainType the type of the entities to be counted.
	 * @return the number of instances stored in the database. Guaranteed to be not {@code null}.
	 */
	Mono<Long> count(Class<?> domainType);

	/**
	 * Counts the number of entities of a given type.
	 *
	 * @param statement the Cypher {@link Statement} that returns the count.
	 * @return the number of instances stored in the database. Guaranteed to be not {@code null}.
	 */
	Mono<Long> count(Statement statement);

	/**
	 * Counts the number of entities of a given type.
	 *
	 * @param statement  the Cypher {@link Statement} that returns the count.
	 * @param parameters Map of parameters. Must not be {@code null}.
	 * @return the number of instances stored in the database. Guaranteed to be not {@code null}.
	 */
	Mono<Long> count(Statement statement, Map<String, Object> parameters);

	/**
	 * Counts the number of entities of a given type.
	 *
	 * @param cypherQuery the Cypher query that returns the count.
	 * @return the number of instances stored in the database. Guaranteed to be not {@code null}.
	 */
	Mono<Long> count(String cypherQuery);

	/**
	 * Counts the number of entities of a given type.
	 *
	 * @param cypherQuery the Cypher query that returns the count.
	 * @param parameters Map of parameters. Must not be {@code null}.
	 * @return the number of instances stored in the database. Guaranteed to be not {@code null}.
	 */
	Mono<Long> count(String cypherQuery, Map<String, Object> parameters);

	/**
	 * Load all entities of a given type.
	 *
	 * @param domainType the type of the entities. Must not be {@code null}.
	 * @param <T>        the type of the entities. Must not be {@code null}.
	 * @return Guaranteed to be not {@code null}.
	 */
	<T> Flux<T> findAll(Class<T> domainType);

	/**
	 * Load all entities of a given type by executing given statement.
	 *
	 * @param statement  Cypher {@link Statement}. Must not be {@code null}.
	 * @param domainType the type of the entities. Must not be {@code null}.
	 * @param <T>        the type of the entities. Must not be {@code null}.
	 * @return Guaranteed to be not {@code null}.
	 */
	<T> Flux<T> findAll(Statement statement, Class<T> domainType);

	/**
	 * Load all entities of a given type by executing given statement with parameters.
	 *
	 * @param statement  Cypher {@link Statement}. Must not be {@code null}.
	 * @param parameters Map of parameters. Must not be {@code null}.
	 * @param domainType the type of the entities. Must not be {@code null}.
	 * @param <T>        the type of the entities. Must not be {@code null}.
	 * @return Guaranteed to be not {@code null}.
	 */
	<T> Flux<T> findAll(Statement statement, Map<String, Object> parameters, Class<T> domainType);

	/**
	 * Load one entity of a given type by executing given statement with parameters.
	 *
	 * @param statement  Cypher {@link Statement}. Must not be {@code null}.
	 * @param parameters Map of parameters. Must not be {@code null}.
	 * @param domainType the type of the entities. Must not be {@code null}.
	 * @param <T>        the type of the entities. Must not be {@code null}.
	 * @return Guaranteed to be not {@code null}.
	 */
	<T> Mono<T> findOne(Statement statement, Map<String, Object> parameters, Class<T> domainType);

	/**
	 * Load all entities of a given type by executing given statement.
	 *
	 * @param cypherQuery  Cypher query string. Must not be {@code null}.
	 * @param domainType       the type of the entities. Must not be {@code null}.
	 * @param <T>              the type of the entities. Must not be {@code null}.
	 * @return Guaranteed to be not {@code null}.
	 */
	<T> Flux<T> findAll(String cypherQuery, Class<T> domainType);

	/**
	 * Load all entities of a given type by executing given statement with parameters.
	 *
	 * @param cypherQuery  Cypher query string. Must not be {@code null}.
	 * @param parameters       Map of parameters. Must not be {@code null}.
	 * @param domainType       the type of the entities. Must not be {@code null}.
	 * @param <T>              the type of the entities. Must not be {@code null}.
	 * @return Guaranteed to be not {@code null}.
	 */
	<T> Flux<T> findAll(String cypherQuery, Map<String, Object> parameters, Class<T> domainType);

	/**
	 * Load one entity of a given type by executing given statement with parameters.
	 *
	 * @param cypherQuery  Cypher query string. Must not be {@code null}.
	 * @param parameters       Map of parameters. Must not be {@code null}.
	 * @param domainType       the type of the entities. Must not be {@code null}.
	 * @param <T>              the type of the entities. Must not be {@code null}.
	 * @return Guaranteed to be not {@code null}.
	 */
	<T> Mono<T> findOne(String cypherQuery, Map<String, Object> parameters, Class<T> domainType);

	/**
	 * Load an entity from the database.
	 *
	 * @param id         the id of the entity to load. Must not be {@code null}.
	 * @param domainType the type of the entity. Must not be {@code null}.
	 * @param <T>        the type of the entity.
	 * @return the loaded entity. Might return an empty optional.
	 */
	<T> Mono<T> findById(Object id, Class<T> domainType);

	/**
	 * Load all entities of a given type that are identified by the given ids.
	 *
	 * @param ids        of the entities identifying the entities to load. Must not be {@code null}.
	 * @param domainType the type of the entities. Must not be {@code null}.
	 * @param <T>        the type of the entities. Must not be {@code null}.
	 * @return Guaranteed to be not {@code null}.
	 */
	<T> Flux<T> findAllById(Iterable<?> ids, Class<T> domainType);

	/**
	 * Saves an instance of an entity, including all the related entities of the entity.
	 *
	 * @param instance the entity to be saved. Must not be {@code null}.
	 * @param <T>      the type of the entity.
	 * @return the saved instance.
	 */
	<T> Mono<T> save(T instance);

	/**
	 * Saves several instances of an entity, including all the related entities of the entity.
	 *
	 * @param instances the instances to be saved. Must not be {@code null}.
	 * @param <T>       the type of the entity.
	 * @return the saved instances.
	 */
	<T> Flux<T> saveAll(Iterable<T> instances);

	/**
	 * Deletes a single entity including all entities related to that entity.
	 *
	 * @param id         the id of the entity to be deleted. Must not be {@code null}.
	 * @param domainType the type of the entity
	 * @param <T>        the type of the entity.
	 */
	<T> Mono<Void> deleteById(Object id, Class<T> domainType);

	/**
	 * Deletes all entities with one of the given ids, including all entities related to that entity.
	 *
	 * @param ids        the ids of the entities to be deleted. Must not be {@code null}.
	 * @param domainType the type of the entity
	 * @param <T>        the type of the entity.
	 */
	<T> Mono<Void> deleteAllById(Iterable<?> ids, Class<T> domainType);

	/**
	 * Delete all entities of a given type.
	 *
	 * @param domainType type of the entities to be deleted. Must not be {@code null}.
	 */
	Mono<Void> deleteAll(Class<?> domainType);

	/**
	 * Takes a prepared query, containing all the information about the cypher template to be used, needed parameters and
	 * an optional mapping function, and turns it into an executable query.
	 *
	 * @param preparedQuery prepared query that should get converted to an executable query
	 * @param <T>           The type of the objects returned by this query.
	 * @return An executable query
	 */
	<T> Mono<ExecutableQuery<T>> toExecutableQuery(PreparedQuery<T> preparedQuery);

	/**
	 * An interface for controlling query execution in a reactive fashion.
	 *
	 * @param <T> the type that gets returned by the query
	 * @since 1.0
	 */
	interface ExecutableQuery<T> {

		/**
		 * @return All results returned by this query.
		 */
		Flux<T> getResults();

		/**
		 * @return A single result
		 * @throws IncorrectResultSizeDataAccessException if there are more than one result
		 */
		Mono<T> getSingleResult();
	}
}
