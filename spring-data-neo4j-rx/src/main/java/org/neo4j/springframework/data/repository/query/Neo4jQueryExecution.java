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
package org.neo4j.springframework.data.repository.query;

import org.neo4j.springframework.data.core.Neo4jClient;
import org.neo4j.springframework.data.core.PreparedQuery;
import org.neo4j.springframework.data.core.ReactiveNeo4jClient;

/**
 * Set of classes to contain query execution strategies. Depending (mostly) on the return type of a
 * {@link org.springframework.data.repository.query.QueryMethod} a {@link AbstractNeo4jQuery} can be executed in various
 * flavors.
 *
 * @author Michael J. Simons
 * @author Gerrit Meier
 * @since 1.0
 */
@FunctionalInterface
interface Neo4jQueryExecution {

	Object execute(PreparedQuery description, boolean asCollectionQuery);

	class DefaultQueryExecution implements Neo4jQueryExecution {

		private final Neo4jClient neo4jClient;

		DefaultQueryExecution(Neo4jClient neo4jClient) {
			this.neo4jClient = neo4jClient;
		}

		@Override
		public Object execute(PreparedQuery preparedQuery, boolean asCollectionQuery) {

			Neo4jClient.ExecutableQuery executableQuery = neo4jClient.toExecutableQuery(preparedQuery);
			if (asCollectionQuery) {
				return executableQuery.getResults();
			} else {
				return executableQuery.getRequiredSingleResult();
			}
		}
	}

	class ReactiveQueryExecution implements Neo4jQueryExecution {

		private final ReactiveNeo4jClient neo4jClient;

		ReactiveQueryExecution(ReactiveNeo4jClient neo4jClient) {
			this.neo4jClient = neo4jClient;
		}

		@Override
		public Object execute(PreparedQuery preparedQuery, boolean asCollectionQuery) {

			ReactiveNeo4jClient.ExecutableQuery executableQuery = neo4jClient.toExecutableQuery(preparedQuery);
			if (asCollectionQuery) {
				return executableQuery.getResults();
			} else {
				return executableQuery.getSingleResult();
			}
		}
	}
}
