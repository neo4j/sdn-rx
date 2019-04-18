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

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * See <a href="https://s3.amazonaws.com/artifacts.opencypher.org/M14/railroad/NodePattern.html">NodePattern</a>.
 *
 * @author Michael J. Simons
 */
public class Node implements PatternElement, Expression {

	static Node create(@Nullable String symbolicName, String primaryLabel, String... additionalLabels) {

		Assert.hasText(primaryLabel, "A primary label is required.");

		List<String> labels = new ArrayList<>();
		labels.add(primaryLabel);
		labels.addAll(Arrays.asList(additionalLabels));

		return new Node(Optional.ofNullable(symbolicName).map(SymbolicName::new).orElse(null), labels);
	}

	private @Nullable final SymbolicName symbolicName;

	private final List<String> labels;

	Node(SymbolicName symbolicName, List<String> labels) {

		this.symbolicName = symbolicName;
		this.labels = labels;
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

	public OngoingRelationshipDefinition outgoingRelationShipTo(Node other) {
		return new DefaultOngoingRelationshipDefinition(Relationship.Direction.LTR, other);
	}

	public OngoingRelationshipDefinition incomingRelationShipFrom(Node other) {
		return new DefaultOngoingRelationshipDefinition(Relationship.Direction.RTR, other);
	}

	public OngoingRelationshipDefinition relationshipWith(Node other) {
		return new DefaultOngoingRelationshipDefinition(Relationship.Direction.UNI, other);
	}

	public interface OngoingRelationshipDefinition extends OngoingRelationshipDefinitionWithType {

		OngoingRelationshipDefinitionWithType withType(String... types);
	}

	public interface OngoingRelationshipDefinitionWithType {

		Relationship create();

		Relationship as(String symbolicName);
	}

	@RequiredArgsConstructor
	private class DefaultOngoingRelationshipDefinition
		implements OngoingRelationshipDefinition, OngoingRelationshipDefinitionWithType {
		private final Relationship.Direction direction;

		private final Node other;

		private String[] types = new String[0];

		@Override
		public OngoingRelationshipDefinitionWithType withType(String... types) {
			this.types = types;
			return this;
		}

		@Override
		public Relationship create() {
			return as(null);
		}

		@Override
		public Relationship as(String symbolicName) {
			return Relationship.create(Node.this, direction, other, symbolicName, types);
		}
	}
}
