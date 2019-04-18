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
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.neo4j.core.cypher.StatementBuilder.OngoingMatch;
import org.springframework.data.neo4j.core.cypher.StatementBuilder.OngoingMatchAndReturn;

/**
 * @author Michael J. Simonss
 */
class DefaultStatementBuilder
	implements StatementBuilder, OngoingMatch, OngoingMatchAndReturn {

	private List<PatternElement> matchList = new ArrayList<>();
	private List<ReturnItem> returnList = new ArrayList<>();
	private Condition where;

	@Override
	public OngoingMatch match(PatternElement... pattern) {

		this.matchList.addAll(Arrays.asList(pattern));
		return this;
	}

	@Override
	public OngoingMatchAndReturn returning(Expression... expressions) {

		this.returnList.addAll(Arrays.asList(expressions));
		return this;
	}

	@Override
	public OngoingMatchAndReturn returning(Node... nodes) {

		this.returnList.addAll(Arrays.asList(nodes).stream()
			.map(node -> node.getSymbolicName().map(Expression.class::cast).orElse(node))
			.collect(Collectors.toList()));
		return this;
	}

	@Override
	public OngoingMatch where(Condition condition) {

		this.where = condition;
		return this;
	}

	@Override
	public Statement build() {

		Pattern pattern = new Pattern(this.matchList);
		Match match = new Match(pattern, this.where == null ? null : new Where(this.where));
		return new SinglePartQuery(match, new Return(returnList));
	}
}
