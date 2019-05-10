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
package org.springframework.data.neo4j.repository.query;

import java.util.Collections;
import java.util.Optional;

import org.springframework.data.neo4j.core.NodeManager;
import org.springframework.data.repository.query.RepositoryQuery;

/**
 * Implementation of {@link RepositoryQuery} for String based custom Cypher query.
 *
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class StringBasedNeo4jQuery extends AbstractNeo4jQuery {

	private final Neo4jQueryMethod queryMethod;
	private final NodeManager nodeManager;

	private final boolean countQuery;
	private final boolean existsQuery;
	private final boolean deleteQuery;

	public StringBasedNeo4jQuery(NodeManager nodeManager, Neo4jQueryMethod queryMethod) {

		super(nodeManager, queryMethod);

		this.queryMethod = queryMethod;
		this.nodeManager = nodeManager;

		Optional<Query> optionalQueryAnnotation = queryMethod.getQueryAnnotation();
		if (optionalQueryAnnotation.isPresent()) {
			Query queryAnnotation = optionalQueryAnnotation.get();
			countQuery = queryAnnotation.count();
			existsQuery = queryAnnotation.exists();
			deleteQuery = queryAnnotation.delete();
		} else {
			countQuery = false;
			existsQuery = false;
			deleteQuery = false;
		}
	}

	@Override
	protected ExecutableQuery createExecutableQuery(Object[] parameters) {

		boolean collectionQuery = queryMethod.isCollectionQuery();

		return new ExecutableQuery(super.domainType, collectionQuery, queryMethod.getAnnotatedQuery(), Collections.emptyMap());
	}

	@Override
	public boolean isCountQuery() {
		return countQuery;
	}

	@Override
	public boolean isExistsQuery() {
		return existsQuery;
	}

	@Override
	public boolean isDeleteQuery() {
		return deleteQuery;
	}

	@Override
	protected boolean isLimiting() {
		return false;
	}
}
