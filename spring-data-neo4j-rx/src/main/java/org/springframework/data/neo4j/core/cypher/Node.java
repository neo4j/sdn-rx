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

import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.util.Assert;

/**
 * @author Michael J. Simons
 */
public class Node extends AbstractSegment implements Expression<Node> {

	static Node create(String alias, String primaryLabel, String... additionalLabels) {

		Assert.hasText(alias, "A node alias is required.");
		Assert.hasText(primaryLabel, "A primary label is required.");

		return new Node(alias, primaryLabel, Arrays.asList(additionalLabels));
	}

	private final String alias;

	private final String primaryLabel;

	private final List<String> additionalLabels;

	Node(String alias, String primaryLabel, List<String> additionalLabels) {
		this.alias = alias;
		this.primaryLabel = primaryLabel;
		this.additionalLabels = additionalLabels;
	}

	public String getAlias() {
		return alias;
	}

	public List<String> getLabels() {
		return Stream.concat(Stream.of(primaryLabel), additionalLabels.stream()).collect(toList());
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
}
