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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.core.cypher.Relationship.Direction;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/M14/railroad/NodePattern.html">NodePattern</a>.
 *
 * @author Michael J. Simons
 */
public class Node implements PatternElement, NamedExpression {

	static Node create(String primaryLabel, String... additionalLabels) {

		Assert.hasText(primaryLabel, "A primary label is required.");

		for (String additionalLabel : additionalLabels) {
			Assert.hasText(additionalLabel, "An empty label is not allowed.");
		}

		return new Node(primaryLabel, additionalLabels);
	}

	private @Nullable final SymbolicName symbolicName;

	private final List<String> labels;

	Node(String primaryLabel, String... additionalLabels) {

		this.symbolicName = null;

		this.labels = new ArrayList<>();
		this.labels.add(primaryLabel);
		this.labels.addAll(Arrays.asList(additionalLabels));
	}

	Node(SymbolicName symbolicName, List<String> labels) {

		this.symbolicName = symbolicName;
		this.labels = new ArrayList<>(labels);
	}

	/**
	 * Creates a copy of that node with a new symbolic name.
	 *
	 * @param newSymbolicName the new symbolic name.
	 * @return The new node.
	 */
	public Node as(String newSymbolicName) {

		Assert.hasText(newSymbolicName, "Symbolic name is required.");
		return new Node(new SymbolicName(newSymbolicName), labels);
	}

	public Optional<SymbolicName> getSymbolicName() {
		return Optional.ofNullable(symbolicName);
	}

	public List<String> getLabels() {
		return Collections.unmodifiableList(labels);
	}

	public boolean isLabeled() {
		return !this.labels.isEmpty();
	}

	/**
	 * Creates a new {@link Property} associated with this property container..
	 * <p/>
	 * Note: The property container does not track property creation and there is no possibility to enumerate all
	 * properties that have been created for this node.
	 *
	 * @param name property name, must not be {@literal null} or empty.
	 * @return a new {@link Property} associated with this {@link Node}.
	 */
	public Property property(String name) {

		return Property.create(this, name);
	}

	public OngoingRelationshipDefinition<Relationship> outgoingRelationShipTo(Node other) {
		return new DefaultOngoingRelationshipDefinition(this, Direction.LTR, other);
	}

	public OngoingRelationshipDefinition<Relationship> incomingRelationShipFrom(Node other) {
		return new DefaultOngoingRelationshipDefinition(this, Direction.RTR, other);
	}

	public OngoingRelationshipDefinition<Relationship> relationshipWith(Node other) {
		return new DefaultOngoingRelationshipDefinition(this, Direction.UNI, other);
	}

	/**
	 * Exposes {@code withType) and terminal operations for creating a relationship.
	 *
	 * @param <P> The final thing to be defined
	 */
	public interface OngoingRelationshipDefinition<P>
		extends OngoingRelationshipDefinitionWithType<P>, OngoingRelationshipDefinitionWithSymbolicName<P> {

		OngoingRelationshipDefinitionWithType<P> withType(String... types);
	}

	/**
	 * Exposes {@code withType} to specify types for the relationship or the the last element of a chain of relationships,
	 * {@code as} to specify a symbolic name for the last element in the chain as well as terminal operations
	 * to create the chain or continue it with the next hop.
	 *
	 * @param <P> The final thing to be defined
	 */
	public interface OngoingRelationshipDefinitionWithType<P> extends OngoingRelationshipDefinitionWithSymbolicName<P> {

		OngoingRelationshipDefinitionWithSymbolicName<P> as(String symbolicName);
	}

	/**
	 * @param <P> The final thing to be defined
	 */
	public interface OngoingRelationshipDefinitionWithSymbolicName<P> {

		P create();

		OngoingRelationshipDefinition<Relationships> outgoingRelationShipTo(Node tripNode);
	}

	@RequiredArgsConstructor
	private static class DefaultOngoingRelationshipDefinition
		implements OngoingRelationshipDefinition<Relationship> {

		private final Node left;

		private final Direction direction;

		private final Node right;

		private String[] types = new String[0];

		private String symbolicName;

		@Override
		public OngoingRelationshipDefinitionWithType withType(@SuppressWarnings("HiddenField") String... types) {
			this.types = types;
			return this;
		}

		@Override
		public Relationship create() {
			return Relationship.create(left, direction, right, symbolicName, types);
		}

		@Override
		public OngoingRelationshipDefinitionWithSymbolicName as(@SuppressWarnings("HiddenField") String symbolicName) {
			this.symbolicName = symbolicName;
			return this;
		}

		@Override
		public OngoingRelationshipDefinition<Relationships> outgoingRelationShipTo(Node next) {
			return new DefaultOngoingRelationshipsDefinition(this.create(),
				new DefaultOngoingRelationshipDefinition(right, Direction.LTR, next));
		}
	}

	private static class DefaultOngoingRelationshipsDefinition
		implements OngoingRelationshipDefinition<Relationships> {

		private final List<Relationship> chain;

		private OngoingRelationshipDefinition<Relationship> nextElement;

		DefaultOngoingRelationshipsDefinition(
			Relationship firstElement,
			OngoingRelationshipDefinition nextElement) {
			this.chain = new ArrayList<>();
			this.chain.add(firstElement);
			this.nextElement = nextElement;
		}

		@Override
		public OngoingRelationshipDefinitionWithType<Relationships> withType(String... types) {
			this.nextElement.withType(types);
			return this;
		}

		@Override
		public OngoingRelationshipDefinitionWithSymbolicName<Relationships> as(String symbolicName) {
			this.nextElement.as(symbolicName);
			return this;
		}

		@Override
		public Relationships create() {
			this.chain.add(this.nextElement.create());
			return new Relationships(this.chain);
		}

		@Override
		public OngoingRelationshipDefinition<Relationships> outgoingRelationShipTo(Node next) {
			Relationship lastRelationship = this.nextElement.create();
			this.chain.add(lastRelationship);
			this.nextElement =
				new DefaultOngoingRelationshipDefinition(lastRelationship.getRight(), Direction.LTR, next);

			return this;
		}
	}
}
