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

import static org.springframework.data.neo4j.core.cypher.Cypher.*;

import org.apiguardian.api.API;
import org.springframework.data.neo4j.core.cypher.support.Visitable;
import org.springframework.util.Assert;

/**
 * An expression can be used in many places, i.e. in return statements, pattern elements etc.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@API(status = API.Status.INTERNAL, since = "1.0")
public interface Expression extends Visitable {

	/**
	 * Creates an expression with an alias. This expression does not track which or how many aliases have been created.
	 *
	 * @param alias The alias to use
	 * @return An aliased expression.
	 */
	default AliasedExpression as(String alias) {

		Assert.hasText(alias, "The alias may not be null or empty.");
		return new AliasedExpression(this, alias);
	}

	default Condition isEqualTo(Expression rhs) {
		return Conditions.isEqualTo(this, rhs);
	}

	default Condition isNotEqualTo(Expression rhs) {
		return Conditions.isNotEqualTo(this, rhs);
	}

	/**
	 * Creates a condition that checks whether this {@code expression} is {@literal true}.
	 *
	 * @return A new condition
	 */
	default Condition isTrue() {
		return Conditions.isEqualTo(this, literalTrue());
	}

	/**
	 * Creates a condition that checks whether this {@code expression} is {@literal false}.
	 *
	 * @return A new condition
	 */
	default Condition isFalse() {
		return Conditions.isEqualTo(this, literalFalse());
	}

	/**
	 * Creates a condition that checks whether this {@code expression} matches that {@code expression}.
	 *
	 * @param expression The expression to match against. Must evaluate into a string during runtime.
	 * @return A new condition.
	 */
	default Condition matches(Expression expression) {
		return Conditions.matches(this, expression);
	}

	/**
	 * Creates a condition that checks whether this {@code expression} matches the given {@code pattern}.
	 *
	 * @param pattern The pattern to match
	 * @return A new condition.
	 */
	default Condition matches(String pattern) {
		return Conditions.matches(this, Cypher.literalOf(pattern));
	}

	/**
	 * Creates a condition that checks whether this {@code expression} starts with that {@code expression}.
	 *
	 * @param expression The expression to match against. Must evaluate into a string during runtime.
	 * @return A new condition.
	 */
	default Condition startsWith(Expression expression) {
		return Conditions.startsWith(this, expression);
	}

	/**
	 * Creates a condition that checks whether this {@code expression} contains that {@code expression}.
	 *
	 * @param expression The expression to match against. Must evaluate into a string during runtime.
	 * @return A new condition.
	 */
	default Condition contains(Expression expression) {
		return Conditions.contains(this, expression);
	}

	/**
	 * Creates a condition that checks whether this {@code expression} ends with that {@code expression}.
	 *
	 * @param expression The expression to match against. Must evaluate into a string during runtime.
	 * @return A new condition.
	 */
	default Condition endsWith(Expression expression) {
		return Conditions.endsWith(this, expression);
	}

	/**
	 * Creates a {@code +} operation of this and that {@code expression}.
	 *
	 * @param expression The expression to add
	 * @return A new operation.
	 */
	default Operation plus(Expression expression) {
		return Operations.plus(this, expression);
	}

	// WIP
	// TODO Make longs a list of expressions or something
	// WIP

	default Condition isIn(Iterable<Long> ids) {
		return Comparison.create(
			this,
			"in",
			new ListLiteral(ids));
	}
}
