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

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.neo4j.springframework.data.core.PreparedQuery;
import org.neo4j.springframework.data.core.ReactiveNeo4jOperations;
import org.neo4j.springframework.data.core.mapping.Neo4jMappingContext;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.repository.query.parser.PartTree.OrPart;
import org.springframework.lang.Nullable;

/**
 * Implementation of {@link RepositoryQuery} for derived finder methods.
 *
 * @author Gerrit Meier
 * @author Michael J. Simons
 * @since 1.0
 */
final class ReactivePartTreeNeo4jQuery extends AbstractReactiveNeo4jQuery {

	private final PartTree tree;

	public static RepositoryQuery create(ReactiveNeo4jOperations neo4jOperations, Neo4jMappingContext mappingContext,
		Neo4jQueryMethod queryMethod) {
		return new ReactivePartTreeNeo4jQuery(neo4jOperations, mappingContext, queryMethod,
			new PartTree(queryMethod.getName(), queryMethod.getDomainClass()));
	}

	private ReactivePartTreeNeo4jQuery(
		ReactiveNeo4jOperations neo4jOperations,
		Neo4jMappingContext mappingContext,
		Neo4jQueryMethod queryMethod,
		PartTree tree
	) {
		super(neo4jOperations, mappingContext, queryMethod, Neo4jQueryType.fromPartTree(tree));

		this.tree = tree;
		// Validate parts. Sort properties will be validated by Spring Data already.
		PartValidator validator = new PartValidator(queryMethod);
		this.tree.flatMap(OrPart::stream).forEach(validator::validatePart);
	}

	@Override
	protected <T extends Object> PreparedQuery<T> prepareQuery(
		Class<T> returnedType, List<String> includedProperties, Neo4jParameterAccessor parameterAccessor,
		@Nullable Neo4jQueryType queryType,
		@Nullable BiFunction<TypeSystem, Record, ?> mappingFunction) {

		CypherQueryCreator queryCreator = new CypherQueryCreator(
			mappingContext, domainType, Optional.ofNullable(queryType).orElseGet(() -> Neo4jQueryType.fromPartTree(tree)), tree, parameterAccessor,
			includedProperties,
			this::convertParameter
		);

		QueryAndParameters queryAndParameters = queryCreator.createQuery();

		return PreparedQuery.queryFor(returnedType)
			.withCypherQuery(queryAndParameters.getQuery())
			.withParameters(queryAndParameters.getParameters())
			.usingMappingFunction(mappingFunction)
			.build();
	}
}
