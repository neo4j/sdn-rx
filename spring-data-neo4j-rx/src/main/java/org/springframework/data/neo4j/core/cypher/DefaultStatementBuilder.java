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
package org.springframework.data.neo4j.core.cypher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.data.neo4j.core.cypher.StatementBuilder.MatchAndReturn;

/**
 * @author Michael J. Simons
 */
public class DefaultStatementBuilder implements StatementBuilder, MatchAndReturn {

	private final List<Expression<Node>> nodeList = new ArrayList<>();
	private final List<Expression<Node>> returnList = new ArrayList<>();

	@Override
	public MatchAndReturn match(Expression<Node> expression) {
		nodeList.add(expression);
		return this;
	}

	@Override
	public MatchAndReturn match(Expression<Node>... expressions) {
		nodeList.addAll(Arrays.asList(expressions));
		return this;
	}

	@Override
	public MatchAndReturn match(Collection<? extends Expression<Node>> expressions) {
		nodeList.addAll(expressions);
		return this;
	}

	@Override
	public BuildableMatch returning(Node expression) {
		this.returnList.add(expression);
		return this;
	}

	@Override
	public Statement build() {
		return new DefaultStatement(nodeList, returnList);
	}
}
