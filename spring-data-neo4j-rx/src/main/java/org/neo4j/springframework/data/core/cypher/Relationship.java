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

import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;
import org.neo4j.springframework.data.core.cypher.support.Visitor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/railroad/RelationshipPattern.html">RelationshipPattern</a>.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@API(status = API.Status.INTERNAL, since = "1.0")
public final class Relationship implements
	PatternElement, Named, Expression,
	ExposesRelationships<RelationshipChain> {

	/**
	 * While the direction in the schema package is centered around the node, the direction here is the direction between two nodes.
	 * @since 1.0
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
		@Nullable Direction direction, Node right, String... types) {

		Assert.notNull(left, "Left node is required.");
		Assert.notNull(right, "Right node is required.");

		List<String> listOfTypes = Arrays.stream(types)
			.filter(type -> !(type == null || type.isEmpty()))
			.collect(toList());

		RelationshipDetail details = new RelationshipDetail(
			Optional.ofNullable(direction).orElse(Direction.UNI),
			null, listOfTypes);
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

	/**
	 * Creates a copy of this relationship with a new symbolic name.
	 *
	 * @param newSymbolicName the new symbolic name.
	 * @return The new relationship.
	 */
	public Relationship named(String newSymbolicName) {

		// Sanity check of newSymbolicName delegated to the details.
		return new Relationship(this.left, this.details.named(newSymbolicName), this.right);
	}

	@Override
	public Optional<SymbolicName> getSymbolicName() {
		return details.getSymbolicName();
	}

	@Override
	public RelationshipChain relationshipTo(Node other, String... types) {
		return RelationshipChain
			.create(this)
			.add(this.right.relationshipTo(other, types));
	}

	@Override
	public RelationshipChain relationshipFrom(Node other, String... types) {
		return RelationshipChain
			.create(this)
			.add(this.right.relationshipFrom(other, types));
	}

	@Override
	public RelationshipChain relationshipBetween(Node other, String... types) {
		return RelationshipChain
			.create(this)
			.add(this.right.relationshipBetween(other, types));
	}

	@Override
	public void accept(Visitor visitor) {

		visitor.enter(this);

		left.accept(visitor);
		details.accept(visitor);
		right.accept(visitor);

		visitor.leave(this);
	}

	/**
	 * Creates a map projection based on this relationship. The relationship needs a symbolic name for this to work.
	 * <p>
	 * Entries of type {@code String} in {@code entries} followed by an {@link Expression} will be treated as map keys
	 * pointing to the expression in the projection, {@code String} entries alone will be treated as property lookups on the node.
	 *
	 * @param entries A list of entries for the projection
	 * @return A map projection.
	 */
	public MapProjection project(Object... entries) {
		return MapProjection.create(this.getSymbolicName().orElseThrow(() -> new IllegalStateException("Cannot project a relationship without a symbolic name.")), entries);
	}
}
