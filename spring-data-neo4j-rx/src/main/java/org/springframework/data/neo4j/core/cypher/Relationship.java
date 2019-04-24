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

import java.util.Arrays;
import java.util.Optional;

import org.springframework.data.neo4j.core.cypher.support.Visitor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/M14/railroad/RelationshipPattern.html">RelationshipPattern</a>.
 *
 * @author Michael J. Simons
 */
public class Relationship implements PatternElement, NamedExpression {

	/**
	 * While the direction in the schema package is centered around the node, the direction here is the direction between two nodes.
	 */
	public enum Direction {
		/**
		 * Left to right
		 */
		LTR("-", "->"),
		/**
		 * Right to left
		 */
		RTR("<-", "-"),
		/**
		 * None
		 */
		UNI("-", "-");

		Direction(String symbolLeft, String symbolRight) {
			this.symbolLeft = symbolLeft;
			this.symbolRight = symbolRight;
		}

		private final String symbolLeft;

		private final String symbolRight;

		public String getSymbolLeft() {
			return symbolLeft;
		}

		public String getSymbolRight() {
			return symbolRight;
		}
	}

	static Relationship create(Node left,
		@Nullable Direction direction, Node right, @Nullable String symbolicName, String... types) {

		Assert.notNull(left, "Left node is required.");
		Assert.notNull(right, "Right node is required.");

		RelationshipDetail details = new RelationshipDetail(
			Optional.ofNullable(direction).orElse(Direction.UNI),
			Optional.ofNullable(symbolicName).map(SymbolicName::new).orElse(null), Arrays.asList(types));
		return new Relationship(left, details, right);
	}

	private final Node left;

	private final Node right;

	private final RelationshipDetail details;

	Relationship(Node left, RelationshipDetail details, Node right) {
		this.left = left;
		this.right = right;
		this.details = details;
	}

	public Node getLeft() {
		return left;
	}

	public Node getRight() {
		return right;
	}

	public RelationshipDetail getDetails() {
		return details;
	}

	public Optional<SymbolicName> getSymbolicName() {
		return details.getSymbolicName();
	}

	@Override
	public void accept(Visitor visitor) {

		visitor.enter(this);

		left.accept(visitor);
		details.accept(visitor);
		right.accept(visitor);

		visitor.leave(this);
	}
}
