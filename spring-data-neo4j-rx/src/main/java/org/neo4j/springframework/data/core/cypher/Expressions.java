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
package org.neo4j.springframework.data.core.cypher;

import java.util.Arrays;

import org.apiguardian.api.API;

/**
 * Utility methods for dealing with expressions.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@API(status = API.Status.INTERNAL, since = "1.0")
public final class Expressions {

	/**
	 * @param expression Possibly named with a non-empty symbolic name.
	 * @return The name of the expression if the expression is named or the expression itself.
	 */
	static Expression nameOrExpression(Expression expression) {

		if (expression instanceof Named) {
			return ((Named) expression).getSymbolicName().map(Expression.class::cast).orElse(expression);
		} else {
			return expression;
		}
	}

	static Expression[] createSymbolicNames(String[] variables) {
		return Arrays.stream(variables).map(SymbolicName::create).toArray(Expression[]::new);
	}

	private Expressions() {
	}
}
