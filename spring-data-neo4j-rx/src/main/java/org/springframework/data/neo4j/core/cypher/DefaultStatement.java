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

import java.util.Collections;
import java.util.List;

/**
 * @author Michael J. Simons
 */
class DefaultStatement implements Statement {

	private final List<Match> matchList;

	private final Where where;

	private final Return returning;

	DefaultStatement(
		List<Expression<Node>> matchList,
		Condition condition,
		List<Expression<Node>> returnList) {

		this.matchList = Collections.singletonList(new Match(matchList));
		this.where = new Where(condition);
		this.returning = new Return(returnList);
	}

	@Override
	public void accept(Visitor visitor) {

		if (this.matchList != null) {
			this.matchList.forEach(segment -> segment.accept(visitor));
		}

		if (this.where != null) {
			this.where.accept(visitor);
		}

		if (this.returning != null) {
			this.returning.accept(visitor);
		}
	}
}
