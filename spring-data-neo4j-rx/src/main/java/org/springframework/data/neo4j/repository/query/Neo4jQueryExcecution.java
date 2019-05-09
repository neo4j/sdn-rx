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

import lombok.RequiredArgsConstructor;

import org.springframework.data.neo4j.core.NodeManager;

/**
 * Set of classes to contain query execution strategies. Depending (mostly) on the return type of a
 * {@link org.springframework.data.repository.query.QueryMethod} a {@link AbstractNeo4jQuery} can be executed in various
 * flavors.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@FunctionalInterface
interface Neo4jQueryExecution {

	Object execute(ExecutableQuery query);

	@RequiredArgsConstructor
	class DefaultQueryExecution implements Neo4jQueryExecution {

		private final NodeManager nodeManager;

		@Override
		public Object execute(ExecutableQuery query) {

			Class<?> returnedType = query.getResultType();
			boolean collectionQuery = query.isCollectionQuery();

			if (collectionQuery) {
				return nodeManager.executeTypedQueryForObjects(query.getQuerySupplier(), returnedType);
			} else {
				return nodeManager.executeTypedQueryForObject(query.getQuerySupplier(), returnedType);
			}

		}
	}
}
