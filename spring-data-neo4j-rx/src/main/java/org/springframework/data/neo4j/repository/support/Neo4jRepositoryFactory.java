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

import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.data.neo4j.core.NodeManager;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Neo4jQueryMethod;
import org.springframework.data.neo4j.repository.query.PartTreeNeo4jQuery;
import org.springframework.data.neo4j.repository.query.StringBasedNeo4jQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;

/**
 * Factory to create {@link Neo4jRepository} instances.
 *
 * @author Gerrit Meier
 * @author Michael J. Simons
 * @since 1.0
 */
final class Neo4jRepositoryFactory extends RepositoryFactorySupport {

	private final NodeManager nodeManager;

	Neo4jRepositoryFactory(NodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}

	@Override
	public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		return null;
	}

	@Override
	protected Object getTargetRepository(RepositoryInformation metadata) {
		return getTargetRepositoryViaReflection(metadata, nodeManager, metadata.getDomainType());
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		return SimpleNeo4jRepository.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getQueryLookupStrategy(org.springframework.data.repository.query.QueryLookupStrategy.Key, org.springframework.data.repository.query.EvaluationContextProvider)
	 */
	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(Key key,
			QueryMethodEvaluationContextProvider evaluationContextProvider) {

		return Optional.of(new Neo4jQueryLookupStrategy(nodeManager, evaluationContextProvider));
	}

	private class Neo4jQueryLookupStrategy implements QueryLookupStrategy {

		private final NodeManager nodeManager;
		private final QueryMethodEvaluationContextProvider evaluationContextProvider;

		private Neo4jQueryLookupStrategy(NodeManager nodeManager,
				QueryMethodEvaluationContextProvider evaluationContextProvider) {

			this.nodeManager = nodeManager;
			this.evaluationContextProvider = evaluationContextProvider;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
		 */
		@Override
		public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
				NamedQueries namedQueries) {

			Neo4jQueryMethod queryMethod = new Neo4jQueryMethod(method, metadata, factory);
			if (queryMethod.hasAnnotatedQuery()) {
				return new StringBasedNeo4jQuery(nodeManager, queryMethod);
			}

			return new PartTreeNeo4jQuery(nodeManager, queryMethod);
		}
	}
}
