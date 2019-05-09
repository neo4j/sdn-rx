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

import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.lang.Nullable;

/**
 * Neo4j specific implementation of {@link QueryMethod}.
 *
 * @author Gerrit Meier
 * @author Michael J. Simons
 * @since 1.0
 */
public class Neo4jQueryMethod extends QueryMethod {

	private final Method method;

	/**
	 * Creates a new {@link Neo4jQueryMethod} from the given parameters. Looks up the correct query to use for following
	 * invocations of the method given.
	 *
	 * @param method must not be {@literal null}.
	 * @param metadata must not be {@literal null}.
	 * @param factory must not be {@literal null}.
	 */
	public Neo4jQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
		super(method, metadata, factory);
		this.method = method;
	}

	/**
	 * Determines if this is a custom Cypher query method.
	 *
	 * @return true, if method has {@link Query} annotation. Otherwise false.
	 */
	public boolean hasAnnotatedQuery() {
		return getQueryAnnotation().isPresent();
	}

	@Nullable
	String getAnnotatedQuery() {
		return getQueryAnnotation().map(Query::value).orElse(null);
	}

	/**
	 * @return the {@link Query} annotation that is applied to the method or an empty {@link Optional} if none available.
	 */
	Optional<Query> getQueryAnnotation() {
		return Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(method, Query.class));
	}

	@Override
	public Class<?> getDomainClass() {
		return super.getDomainClass();
	}
}
