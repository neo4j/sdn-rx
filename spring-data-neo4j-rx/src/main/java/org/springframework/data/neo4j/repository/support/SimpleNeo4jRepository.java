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
package org.springframework.data.neo4j.repository.support;

import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.core.NodeManager;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository base implementation for Neo4j.
 *
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
@Repository
@Transactional(readOnly = true)
class SimpleNeo4jRepository<T, ID> implements Neo4jRepository<T, ID> {

	private final NodeManager nodeManager;

	SimpleNeo4jRepository(NodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}

	@Override
	public Iterable<T> findAll(Sort sort) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	@Transactional
	public <S extends T> S save(S entity) {
		return this.nodeManager.save(entity);
	}

	@Override
	@Transactional
	public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	public Optional<T> findById(ID id) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	public boolean existsById(ID id) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	public Iterable<T> findAll() {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	public Iterable<T> findAllById(Iterable<ID> ids) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	public long count() {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	@Transactional
	public void deleteById(ID id) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	@Transactional
	public void delete(T entity) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	@Transactional
	public void deleteAll(Iterable<? extends T> entities) {

		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	@Transactional
	public void deleteAll() {

		for (T element : findAll()) {
			delete(element);
		}
	}

	@Override
	public <S extends T> Optional<S> findOne(Example<S> example) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	public <S extends T> Iterable<S> findAll(Example<S> example) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	public <S extends T> Iterable<S> findAll(Example<S> example, Sort sort) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	public <S extends T> long count(Example<S> example) {
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	public <S extends T> boolean exists(Example<S> example) {
		throw new UnsupportedOperationException("Not there yet.");
	}
}
