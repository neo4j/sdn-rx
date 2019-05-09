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

import java.util.Arrays;

import org.springframework.data.neo4j.core.NodeManager;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * Implementation of {@link RepositoryQuery} for derived finder methods.
 *
 * @author Gerrit Meier
 * @author Michael J. Simons
 * @since 1.0
 */
public class PartTreeNeo4jQuery extends AbstractNeo4jQuery {

	private final PartTree tree;
	private final ResultProcessor processor;

	public PartTreeNeo4jQuery(NodeManager nodeManager, Neo4jQueryMethod queryMethod) {
		super(nodeManager, queryMethod);

		this.processor = queryMethod.getResultProcessor();
		this.tree = new PartTree(queryMethod.getName(), processor.getReturnedType().getDomainType());

	}

	@Override
	protected ExecutableQuery createExecutableQuery(Object[] parameters) {
		queryMethod.getParameters().forEach(p -> {

			System.out.println(p + ", " + p.getName() + " " + p.getIndex() + " " + p.getPlaceholder());
		});
		Arrays.stream(parameters).forEach(System.out::println);
		throw new UnsupportedOperationException("Not there yet.");
	}

	@Override
	protected boolean isCountQuery() {
		return tree.isCountProjection();
	}

	@Override
	protected boolean isExistsQuery() {
		return tree.isExistsProjection();
	}

	@Override
	protected boolean isDeleteQuery() {
		return tree.isDelete();
	}

	@Override
	protected boolean isLimiting() {
		return tree.isLimiting();
	}
}
