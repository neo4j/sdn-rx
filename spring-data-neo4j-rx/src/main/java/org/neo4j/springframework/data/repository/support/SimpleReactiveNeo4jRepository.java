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
package org.neo4j.springframework.data.repository.support;

import static java.util.Collections.*;
import static org.neo4j.springframework.data.core.cypher.Cypher.*;
import static org.neo4j.springframework.data.repository.query.CypherAdapterUtils.*;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.neo4j.springframework.data.core.PreparedQuery;
import org.neo4j.springframework.data.core.ReactiveNeo4jClient;
import org.neo4j.springframework.data.core.ReactiveNeo4jClient.ExecutableQuery;
import org.neo4j.springframework.data.core.cypher.Condition;
import org.neo4j.springframework.data.core.cypher.Functions;
import org.neo4j.springframework.data.core.cypher.Statement;
import org.neo4j.springframework.data.core.cypher.renderer.Renderer;
import org.neo4j.springframework.data.core.schema.NodeDescription;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Repository base implementation for Neo4j.
 *
 * @author Gerrit Meier
 * @author Michael J. Simons
 * @since 1.0
 */
@Repository
@Transactional(readOnly = true)
@Slf4j
class SimpleReactiveNeo4jRepository<T, ID> implements ReactiveSortingRepository<T, ID> {

	private static final Renderer renderer = Renderer.getDefaultRenderer();

	private final ReactiveNeo4jClient neo4jClient;

	private final Neo4jEntityInformation<T, ID> entityInformation;

	private final NodeDescription<T> entityMetaData;

	private final SchemaBasedStatementBuilder statementBuilder;

	SimpleReactiveNeo4jRepository(ReactiveNeo4jClient neo4jClient, Neo4jEntityInformation<T, ID> entityInformation,
		SchemaBasedStatementBuilder statementBuilder) {
		this.neo4jClient = neo4jClient;
		this.entityInformation = entityInformation;
		this.entityMetaData = this.entityInformation.getEntityMetaData();
		this.statementBuilder = statementBuilder;
	}

	@Override
	public Mono<T> findById(ID id) {
		Statement statement = statementBuilder
			.prepareMatchOf(entityMetaData, Optional.of(entityInformation.getIdExpression().isEqualTo(literalOf(id))))
			.returning(asterisk())
			.build();
		return createExecutableQuery(statement).getSingleResult();
	}

	@Override
	public Mono<T> findById(Publisher<ID> idPublisher) {
		return Mono.from(idPublisher).flatMap(this::findById);
	}

	@Override
	public Flux<T> findAll(Sort sort) {
		Statement statement = statementBuilder.prepareMatchOf(entityMetaData, Optional.empty())
			.returning(asterisk())
			.orderBy(toSortItems(entityMetaData, sort))
			.build();

		return createExecutableQuery(statement).getResults();
	}

	@Override
	public Mono<Boolean> existsById(ID id) {
		return findById(id).hasElement();
	}

	@Override
	public Mono<Boolean> existsById(Publisher<ID> idPublisher) {
		return Mono.from(idPublisher).flatMap(this::existsById);
	}

	@Override
	public Flux<T> findAll() {
		Statement statement = statementBuilder.prepareMatchOf(entityMetaData, Optional.empty())
			.returning(asterisk()).build();
		return createExecutableQuery(statement).getResults();
	}

	@Override
	public Flux<T> findAllById(Iterable<ID> ids) {
		Statement statement = statementBuilder
			.prepareMatchOf(entityMetaData, Optional.of(entityInformation.getIdExpression().in((parameter("ids")))))
			.returning(asterisk())
			.build();

		return createExecutableQuery(statement, singletonMap("ids", ids)).getResults();
	}

	@Override
	public Flux<T> findAllById(Publisher<ID> idStream) {
		return Flux.from(idStream).buffer().flatMap(this::findAllById);
	}

	@Override
	public Mono<Long> count() {
		Statement statement = statementBuilder.prepareMatchOf(entityMetaData, Optional.empty())
			.returning(Functions.count(asterisk())).build();

		PreparedQuery<Long> preparedQuery = PreparedQuery.queryFor(Long.class)
			.withCypherQuery(renderer.render(statement))
			.build();
		return this.neo4jClient.toExecutableQuery(preparedQuery)
			.getSingleResult();
	}

	@Override public <S extends T> Mono<S> save(S entity) {
		return null;
	}

	@Override public <S extends T> Flux<S> saveAll(Iterable<S> entities) {
		return null;
	}

	@Override public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.reactive.ReactiveCrudRepository#deleteById(java.lang.Object)
	 */
	@Override
	@Transactional
	public Mono<Void> deleteById(ID id) {

		Assert.notNull(id, "The given id must not be null!");

		return Mono.just(id).as(this::deleteById);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.reactive.ReactiveCrudRepository#delete(java.lang.Object)
	 */
	@Override
	@Transactional
	public Mono<Void> delete(T entity) {

		Assert.notNull(entity, "The given entity must not be null!");

		return Mono.just(entity)
			.map(this.entityInformation::getId)
			.as(this::deleteById);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.reactive.ReactiveCrudRepository#deleteById(org.reactivestreams.Publisher)
	 */
	@Override
	@Transactional
	public Mono<Void> deleteById(Publisher<ID> idPublisher) {

		Assert.notNull(idPublisher, "The given Publisher of an id must not be null!");

		return Mono.from(idPublisher)
			.flatMap(id -> {
				String nameOfParameter = "id";
				Condition condition = this.entityInformation.getIdExpression().isEqualTo(parameter(nameOfParameter));

				Statement statement = statementBuilder.prepareDeleteOf(entityMetaData, Optional.of(condition));
				return this.neo4jClient.query(() -> renderer.render(statement))
					.bind(id).to(nameOfParameter).run();
			})
			.then();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.reactive.ReactiveCrudRepository#deleteAll(java.lang.Iterable)
	 */
	@Override
	@Transactional
	public Mono<Void> deleteAll(Iterable<? extends T> entities) {

		Assert.notNull(entities, "The given Iterable of entities must not be null!");

		return Flux.fromIterable(entities)
			.as(this::deleteAll);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.reactive.ReactiveCrudRepository#deleteAll(org.reactivestreams.Publisher)
	 */
	@Override
	@Transactional
	public Mono<Void> deleteAll(Publisher<? extends T> entitiesPublisher) {

		Assert.notNull(entitiesPublisher, "The given Publisher of entities must not be null!");

		return Flux.from(entitiesPublisher)
			.map(this.entityInformation::getId)
			.collect(Collectors.toList())
			.flatMap(ids -> {

				String nameOfParameter = "ids";
				Condition condition = entityInformation.getIdExpression().in(parameter(nameOfParameter));
				Statement statement = statementBuilder.prepareDeleteOf(entityMetaData, Optional.of(condition));

				return this.neo4jClient.query(() -> renderer.render(statement)).bind(ids).to(nameOfParameter).run();
			})
			.then();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.reactive.ReactiveCrudRepository#deleteAll()
	 */
	@Override
	@Transactional
	public Mono<Void> deleteAll() {

		Statement statement = statementBuilder.prepareDeleteOf(entityMetaData, Optional.empty());
		return this.neo4jClient.query(() -> renderer.render(statement)).run().then();
	}

	private ExecutableQuery<T> createExecutableQuery(Statement statement) {
		return createExecutableQuery(statement, Collections.emptyMap());
	}

	private ExecutableQuery<T> createExecutableQuery(Statement statement, Map<String, Object> parameters) {

		PreparedQuery<T> preparedQuery = PreparedQuery.queryFor(this.entityInformation.getJavaType())
			.withCypherQuery(renderer.render(statement))
			.withParameters(parameters)
			.usingMappingFunction(this.entityInformation.getMappingFunction())
			.build();
		return neo4jClient.toExecutableQuery(preparedQuery);
	}
}
