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

import lombok.RequiredArgsConstructor;

import org.springframework.data.neo4j.core.cypher.Statement.SingleQuery;
import org.springframework.data.neo4j.core.cypher.support.Visitor;
import org.springframework.lang.Nullable;

/**
 * @author Michael J. Simonss
 */
@RequiredArgsConstructor
public class SinglePartQuery implements SingleQuery {

	private @Nullable final ReadingClause readingClause;

	private final Return aReturn;

	@Override
	public void accept(Visitor visitor) {

		if (readingClause != null) {
			readingClause.accept(visitor);
		}

		aReturn.accept(visitor);
	}
}
